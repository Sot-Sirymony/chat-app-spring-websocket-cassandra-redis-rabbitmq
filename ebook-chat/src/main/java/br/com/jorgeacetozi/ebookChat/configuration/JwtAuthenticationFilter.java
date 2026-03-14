package br.com.jorgeacetozi.ebookChat.configuration;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.jorgeacetozi.ebookChat.authentication.domain.service.JwtTokenService;

/**
 * Validates JWT from Authorization: Bearer &lt;token&gt; and sets SecurityContext
 * so REST API calls from Next.js (or any client) work with JWT-only auth.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            if (!token.isEmpty() && jwtTokenService.validateToken(token)) {
                try {
                    SecurityContextHolder.getContext().setAuthentication(
                            jwtTokenService.getAuthenticationFromToken(token));
                } catch (Exception ignored) {
                    // invalid token already caught by validateToken
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
