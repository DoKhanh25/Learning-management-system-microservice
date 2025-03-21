package org.example.chatservice.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat-websocket")
                .setAllowedOrigins("http://localhost:4200")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Try to get token from native header first
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    // If no Authorization header, try to extract from query params
                    if (authHeader == null) {
                        String query = accessor.getFirstNativeHeader("simpConnectMessage");
                        if (query != null && query.contains("access_token=")) {
                            String token = extractTokenFromQuery(query);
                            accessor.setNativeHeader("Authorization", "Bearer " + token);

                            // Debug log to confirm extraction
                            log.info("Extracted token from query: " + (token != null ? "Token found" : "No token"));
                        }
                    }
                }
                return message;
            }

            private String extractTokenFromQuery(String query) {
                // More robust query parameter extraction
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("access_token=")) {
                        return param.substring("access_token=".length());
                    }
                }
                return null;
            }
        });
    }
}
