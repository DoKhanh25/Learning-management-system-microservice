package com.example.gatewayservice.configuration;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
@Order(1)
public class JwtHeaderFilter implements GlobalFilter {

    private final WebClient webClient;
    private final ReactiveJwtDecoder jwtDecoder;
    private static final Logger logger = LoggerFactory.getLogger(JwtHeaderFilter.class);

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}") // Thêm vào application.yml
    private String clientSecret;

    @PostConstruct
    public void setup() {
        logger.info("Setting up JwtHeaderFilter with: authServerUrl={}, realm={}, clientId={}",
                authServerUrl, realm, clientId);

        // Test the token endpoint
        String tokenEndpoint = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        logger.info("Token endpoint URL: {}", tokenEndpoint);
    }

    public JwtHeaderFilter(WebClient.Builder webClientBuilder, ReactiveJwtDecoder jwtDecoder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
        this.jwtDecoder = jwtDecoder; // Inject từ Spring Context
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        logger.info("Request Path: {}", path);

        // First, explicitly check SockJS info and websocket endpoints
        if (path.contains("/chat-websocket/info") ||
                path.contains("/chat-websocket/websocket")) {
            logger.info("Allowing WebSocket handshake: " + path);
            return chain.filter(exchange);
        }


        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String accessToken;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
            logger.info("Processing token from auth header");
        } else if (path.contains("/chat-websocket")) {
            String query = request.getURI().getQuery();
            if (query != null && query.contains("access_token=")) {
                int tokenStart = query.indexOf("access_token=") + 13;
                int tokenEnd = query.indexOf("&", tokenStart);
                if (tokenEnd == -1) {
                    accessToken = query.substring(tokenStart);
                } else {
                    accessToken = query.substring(tokenStart, tokenEnd);
                }
                logger.info("Processing token from query parameter");
            } else {
                accessToken = null;
            }
        } else {
            accessToken = null;
        }

        if(accessToken != null) {
            // Skip the JWT verification using decoder since we'll use the token directly with Keycloak
            String resource = extractResource(request.getPath().value());
            String scope = extractScope(request.getMethod().name().toLowerCase());

            // Log detailed information about the UMA request
            logger.info("UMA request - Path: {}, Method: {}", request.getPath().value(), request.getMethod());
            logger.info("Extracted - Resource: {}, Scope: {}, ClientId: {}", resource, scope, clientId);

            return requestRpt(accessToken, resource, scope)
                    .flatMap(rpt -> {
                        if (rpt == null || rpt.isEmpty()) {
                            logger.error("Empty RPT received");
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        }

                        logger.info("RPT received successfully");

                        // Get user info from the original token to add the X-User-Id header
                        return getUserInfo(accessToken)
                                .flatMap(userId -> {
                                    ServerHttpRequest modifiedRequest = request.mutate()
                                            .header("X-User-Id", userId)
                                            .header("X-Roles", "user")
                                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + rpt)
                                            .build();

                                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                                })
                                .onErrorResume(e -> {
                                    logger.error("Failed to get user info: {}", e.getMessage());
                                    ServerHttpRequest modifiedRequest = request.mutate()
                                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + rpt)
                                            .build();
                                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                                });
                    })
                    .onErrorResume(e -> {
                        logger.error("Failed in token processing: {}", e.getMessage());
                        if (e instanceof WebClientResponseException) {
                            WebClientResponseException wcre = (WebClientResponseException) e;
                            logger.error("Response status: {}, body: {}",
                                    wcre.getStatusCode(), wcre.getResponseBodyAsString());
                        }
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        }

        logger.warn("No Authorization header found");
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<String> getUserInfo(String accessToken) {
        String userInfoEndpoint = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo";

        return webClient.get()
                .uri(userInfoEndpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(userInfo -> (String) userInfo.get("sub"))
                .doOnSuccess(userId -> logger.debug("Retrieved user ID: {}", userId))
                .onErrorResume(e -> {
                    logger.error("Failed to get user info: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    private Mono<String> requestRpt(String accessToken, String resource, String scope) {
        String tokenEndpoint = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        logger.info("Requesting RPT from endpoint: {}", tokenEndpoint);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "urn:ietf:params:oauth:grant-type:uma-ticket");
        formData.add("audience", clientId);

        // Add the permission parameter - try both formats
        if (!resource.isEmpty() && !scope.isEmpty()) {
            formData.add("permission", resource + "#" + scope);
            logger.info("Permission format: {}", resource + "#" + scope);
        }

        // Print full request details for debugging
        formData.forEach((key, values) ->
                logger.info("Form param: {} = {}", key, values));

        return webClient.post()
                .uri(tokenEndpoint)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnNext(response -> logger.info("RPT response received: {}", response))
                .map(response -> (String) response.get("access_token"))
                .doOnSuccess(rpt -> logger.info("RPT token obtained, length: {}",
                        rpt != null ? rpt.length() : 0))
                .onErrorResume(e -> {
                    logger.error("Failed to obtain RPT: {}", e.getMessage());
                    if (e instanceof WebClientResponseException) {
                        WebClientResponseException wcre = (WebClientResponseException) e;
                        logger.error("Status: {}, Response: {}",
                                wcre.getStatusCode(), wcre.getResponseBodyAsString());
                    }
                    return Mono.empty();
                });
    }

    private String extractResource(String path) {
        if (path.startsWith("/api/users")) return "api/users";
        if (path.startsWith("/api/admin")) return "api/admin";
        return "";
    }

    private String extractScope(String method) {
        switch (method.toLowerCase()) {
            case "get": return "VIEW";
            case "post": return "CREATE";
            case "put": return "EDIT";
            case "delete": return "DELETE";
            default: return "read";
        }
    }

//    private List<String> extractRoles(Jwt jwt) {
//        if (jwt.getClaim("realm_access") instanceof Map) {
//            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
//            if (realmAccess.get("roles") instanceof List) {
//                return (List<String>) realmAccess.get("roles");
//            }
//        }
//        return null;
//    }
//
//    private String extractScopes(Jwt jwt) {
//        String scopes = jwt.getClaimAsString("scope");
//        if (scopes != null) return scopes;
//        Map<String, Map<String, List<String>>> resourceAccess = jwt.getClaim("resource_access");
//        if (resourceAccess != null && resourceAccess.containsKey(clientId)) {
//            Map<String, List<String>> resource = resourceAccess.get(clientId);
//            if (resource != null && resource.containsKey("scopes")) {
//                return String.join(",", resource.get("scopes"));
//            }
//        }
//        return "";
//    }
}
