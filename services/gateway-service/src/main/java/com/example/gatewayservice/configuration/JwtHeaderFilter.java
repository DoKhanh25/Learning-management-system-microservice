package com.example.gatewayservice.configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


import java.util.List;
import java.util.Map;

@Component
@Order(1)
public class JwtHeaderFilter implements GlobalFilter {

    private JwtDecoder jwtDecoder;

    @PostConstruct
    public void init() {
        this.jwtDecoder = NimbusJwtDecoder.withJwkSetUri("http://localhost:8080/realms/CDTN-IT/protocol/openid-connect/certs").build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Jwt jwt = jwtDecoder.decode(token);
                String userId = jwt.getSubject();

                // Extract roles from realm_access claim
                List<String> roles = null;
                if (jwt.getClaim("realm_access") instanceof Map) {
                    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
                    if (realmAccess.get("roles") instanceof List) {
                        roles = (List<String>) realmAccess.get("roles");
                    }
                }

                // Extract email
                String email = jwt.getClaimAsString("email");

                // Extract additional useful information
                String name = jwt.getClaimAsString("name");
                String preferredUsername = jwt.getClaimAsString("preferred_username");

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-Roles", roles != null ? String.join(",", roles) : "")
                        .header("X-Email", email != null ? email : "")
                        .header("X-Name", name != null ? name : "")
                        .header("X-Username", preferredUsername != null ? preferredUsername : "")
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                // Log the exception for better debugging
                e.printStackTrace();
                exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }

        return chain.filter(exchange);
    }
}
