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

        if (accessToken != null) {
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

                        // Extract permissions from RPT token
                        Map<String, Object> permissions = extractPermissionsFromRpt(rpt);

                        // Get user info from the original token to add the X-User-Id header
                        return getUserInfo(accessToken)
                                .flatMap(userId -> {
                                    ServerHttpRequest.Builder requestBuilder = request.mutate()
                                            .header("X-User-Id", userId)
                                            .header("X-Roles", "user")
                                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + rpt);

                                    // Add permission headers from RPT
                                    if (permissions != null) {
                                        if (permissions.containsKey("resources")) {
                                            requestBuilder.header("X-Resources", permissions.get("resources").toString());
                                        }
                                        if (permissions.containsKey("scopes")) {
                                            requestBuilder.header("X-Scopes", permissions.get("scopes").toString());
                                        }
                                        if (permissions.containsKey("resourceScopes")) {
                                            requestBuilder.header("X-Resource-Scopes", permissions.get("resourceScopes").toString());
                                        }

                                        // Check if user has access to the current resource with appropriate scope
                                        String currentResource = extractResource(request.getPath().value());
                                        String currentScope = extractScope(request.getMethod().name().toLowerCase());

                                        if (!currentResource.isEmpty() && !currentScope.isEmpty()) {
                                            boolean hasAccess = hasResourceScopeAccess(
                                                    permissions,
                                                    currentResource,
                                                    currentScope
                                            );
                                            requestBuilder.header("X-Has-Access", String.valueOf(hasAccess));
                                            logger.info("Access check for {}/{}: {}", currentResource, currentScope, hasAccess);
                                        }
                                    }

                                    ServerHttpRequest modifiedRequest = requestBuilder.build();
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

    /**
     * Extracts permission information from the RPT token
     *
     * @param rptToken The RPT token from Keycloak
     * @return Map containing permission details including resources and scopes
     */
    private Map<String, Object> extractPermissionsFromRpt(String rptToken) {
        try {
            // Split the JWT token
            String[] parts = rptToken.split("\\.");
            if (parts.length < 2) {
                logger.error("Invalid JWT token format");
                return null;
            }

            // Decode the payload
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            logger.debug("RPT payload: {}", payload);

            // Parse the JSON
            Map<String, Object> claims = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                    payload, Map.class);

            // Extract permissions
            Map<String, Object> result = new java.util.HashMap<>();

            // Check for authorization field which contains permissions in Keycloak RPT
            if (claims.containsKey("authorization")) {
                Map<String, Object> authorization = (Map<String, Object>) claims.get("authorization");

                if (authorization.containsKey("permissions")) {
                    List<Map<String, Object>> permissions = (List<Map<String, Object>>) authorization.get("permissions");

                    // Create resource to scope mapping
                    Map<String, List<String>> resourceScopeMap = new java.util.HashMap<>();
                    List<String> allResources = new java.util.ArrayList<>();
                    List<String> allScopes = new java.util.ArrayList<>();

                    for (Map<String, Object> permission : permissions) {
                        String resourceName = null;
                        String resourceId = null;

                        if (permission.containsKey("rsname")) {
                            resourceName = permission.get("rsname").toString();
                            allResources.add(resourceName);
                        }

                        if (permission.containsKey("resource_id")) {
                            resourceId = permission.get("resource_id").toString();
                            if (resourceName == null) {
                                allResources.add(resourceId);
                            }
                        }

                        // Use either resource name or id as the key
                        String resourceKey = resourceName != null ? resourceName : resourceId;

                        if (resourceKey != null && permission.containsKey("scopes")) {
                            List<String> permScopes = (List<String>) permission.get("scopes");

                            // Add to resource-scope mapping
                            resourceScopeMap.computeIfAbsent(resourceKey, k -> new java.util.ArrayList<>())
                                    .addAll(permScopes);

                            // Add to overall scope list
                            allScopes.addAll(permScopes);
                        }
                    }

                    // Deduplicate scopes and resources
                    allScopes = allScopes.stream().distinct().collect(java.util.stream.Collectors.toList());
                    allResources = allResources.stream().distinct().collect(java.util.stream.Collectors.toList());

                    // Create a JSON representation of resource-scope mappings
                    StringBuilder resourceScopeJson = new StringBuilder("{");
                    boolean first = true;
                    for (Map.Entry<String, List<String>> entry : resourceScopeMap.entrySet()) {
                        if (!first) {
                            resourceScopeJson.append(",");
                        }
                        resourceScopeJson.append("\"").append(entry.getKey()).append("\":[");
                        resourceScopeJson.append(entry.getValue().stream()
                                .map(s -> "\"" + s + "\"")
                                .collect(java.util.stream.Collectors.joining(",")));
                        resourceScopeJson.append("]");
                        first = false;
                    }
                    resourceScopeJson.append("}");

                    result.put("resources", String.join(",", allResources));
                    result.put("scopes", String.join(",", allScopes));
                    result.put("resourceScopes", resourceScopeJson.toString());
                    result.put("resourceScopeMap", resourceScopeMap);
                    result.put("permissions", permissions);

                    logger.info("Extracted permissions - Resources: {}, Scopes: {}",
                            result.get("resources"), result.get("scopes"));
                    logger.debug("Resource-Scope mapping: {}", resourceScopeJson);

                    logger.info("Extracted resource-scope mapping: {}", resourceScopeJson);
                }
            }

            return result;
        } catch (Exception e) {
            logger.error("Error decoding RPT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Checks if user has access to specific resource with specific scope
     *
     * @param permissions The permissions extracted from RPT
     * @param resource The resource to check
     * @param requiredScope The required scope
     * @return true if user has the required scope for the resource
     */
    private boolean hasResourceScopeAccess(Map<String, Object> permissions, String resource, String requiredScope) {
        if (permissions == null || !permissions.containsKey("resourceScopeMap")) {
            return false;
        }

        Map<String, List<String>> resourceScopeMap = (Map<String, List<String>>) permissions.get("resourceScopeMap");

        // Check if resource exists in the map
        if (!resourceScopeMap.containsKey(resource)) {
            return false;
        }

        // Check if required scope is in the list of allowed scopes
        List<String> allowedScopes = resourceScopeMap.get(resource);
        return allowedScopes.contains(requiredScope);
    }
}
