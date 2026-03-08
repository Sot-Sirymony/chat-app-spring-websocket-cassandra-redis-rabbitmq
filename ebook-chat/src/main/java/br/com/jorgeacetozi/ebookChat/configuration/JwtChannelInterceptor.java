package br.com.jorgeacetozi.ebookChat.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import br.com.jorgeacetozi.ebookChat.authentication.domain.service.JwtTokenService;

/**
 * Intercepts STOMP CONNECT and sets Principal from JWT when Authorization header is present.
 * BR-1.1: Validate JWT during STOMP CONNECT; fall back to session when no JWT.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class JwtChannelInterceptor extends ChannelInterceptorAdapter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;

    @Autowired
    public JwtChannelInterceptor(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String DEVICE_TYPE_HEADER = "device-type";
    private static final int MAX_DEVICE_TYPE_LEN = 200;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }
        // T2.1.7: Capture device type / client identifier for ABAC context
        String deviceType = null;
        List<String> deviceHeader = accessor.getNativeHeader(DEVICE_TYPE_HEADER);
        if (deviceHeader != null && !deviceHeader.isEmpty() && deviceHeader.get(0) != null) {
            deviceType = deviceHeader.get(0).trim();
        }
        if (deviceType == null || deviceType.isEmpty()) {
            List<String> ua = accessor.getNativeHeader(USER_AGENT_HEADER);
            if (ua != null && !ua.isEmpty() && ua.get(0) != null) {
                deviceType = ua.get(0).trim();
            }
        }
        if (deviceType != null && !deviceType.isEmpty()) {
            if (deviceType.length() > MAX_DEVICE_TYPE_LEN) {
                deviceType = deviceType.substring(0, MAX_DEVICE_TYPE_LEN);
            }
            accessor.getSessionAttributes().put("deviceType", deviceType);
        }
        List<String> authHeader = accessor.getNativeHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || authHeader.isEmpty()) {
            return message; // session-based auth will provide user
        }
        String value = authHeader.get(0);
        if (value == null || !value.startsWith(BEARER_PREFIX)) {
            return message;
        }
        String token = value.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            return message;
        }
        try {
            Authentication auth = jwtTokenService.getAuthenticationFromToken(token);
            accessor.setUser(auth);
        } catch (Exception ignored) {
            // invalid token; leave user unset so connection may fail auth
        }
        return message;
    }
}
