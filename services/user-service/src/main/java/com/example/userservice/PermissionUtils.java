package com.example.userservice;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class PermissionUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static boolean isAllowed(String resource, String scope, String resourceScopesHeader) {
        if (resource == null || scope == null || resourceScopesHeader == null || resourceScopesHeader.isEmpty()) {
            return false;
        }

        try {
            // Parse the JSON string from the header
            Map<String, List<String>> resourceScopes = parseResourceScopes(resourceScopesHeader);

            // Check if resource exists and has the required scope
            if (resourceScopes.containsKey(resource)) {
                List<String> allowedScopes = resourceScopes.get(resource);
                return allowedScopes != null && allowedScopes.contains(scope);
            }

            return false;
        } catch (Exception e) {
            log.error("Error parsing resource scopes: {}", e.getMessage());
            return false;
        }
    }


    @SuppressWarnings("unchecked")
    private static Map<String, List<String>> parseResourceScopes(String resourceScopesJson) throws JsonProcessingException {
        if (resourceScopesJson == null || resourceScopesJson.isEmpty()) {
            return Collections.emptyMap();
        }

        return objectMapper.readValue(resourceScopesJson, Map.class);
    }


    public static boolean canView(String resource, String resourceScopesHeader) {
        return isAllowed(resource, "VIEW", resourceScopesHeader);
    }

    public static boolean canEdit(String resource, String resourceScopesHeader) {
        return isAllowed(resource, "EDIT", resourceScopesHeader);
    }

    public static boolean canCreate(String resource, String resourceScopesHeader) {
        return isAllowed(resource, "CREATE", resourceScopesHeader);
    }

    public static boolean canDelete(String resource, String resourceScopesHeader) {
        return isAllowed(resource, "DELETE", resourceScopesHeader);
    }

    public static String getResourceScopesFromHeaders(HttpHeaders headers) {
        if (headers != null && headers.containsKey("X-Resource-Scopes")) {
            return headers.getFirst("X-Resource-Scopes");
        }
        return "";
    }
}

