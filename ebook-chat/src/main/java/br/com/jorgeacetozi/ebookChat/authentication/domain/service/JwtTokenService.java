package br.com.jorgeacetozi.ebookChat.authentication.domain.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import br.com.jorgeacetozi.ebookChat.configuration.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Generates and validates JWT for API/WebSocket clients.
 * BR-1.1: JWT after successful auth; include username, roles.
 */
@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    @Autowired
    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpirationMs());

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        if (!jwtProperties.getIssuer().equals(claims.getIssuer())) {
            throw new io.jsonwebtoken.security.SecurityException("Invalid issuer");
        }
        return claims;
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Build Spring Authentication from valid JWT for WebSocket/STOMP.
     */
    public Authentication getAuthenticationFromToken(String token) {
        Claims claims = parseToken(token);
        String username = claims.getSubject();
        String rolesClaim = claims.get("roles", String.class);
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (rolesClaim != null && !rolesClaim.isEmpty()) {
            for (String role : rolesClaim.split(",")) {
                authorities.add(new SimpleGrantedAuthority(role.trim()));
            }
        }
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
}
