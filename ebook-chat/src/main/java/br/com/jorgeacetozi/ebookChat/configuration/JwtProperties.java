package br.com.jorgeacetozi.ebookChat.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT configuration bound from application.yml (ebook.chat.jwt).
 * BR-1.1: Hybrid auth (JWT + existing login).
 */
@ConfigurationProperties(prefix = "ebook.chat.jwt")
public class JwtProperties {

    private String secret = "ebook-chat-internal-secret-key-min-256-bits-for-hs256";
    private String issuer = "ebook-chat";
    private long expirationMs = 86400000L; // 24 hours

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }
}
