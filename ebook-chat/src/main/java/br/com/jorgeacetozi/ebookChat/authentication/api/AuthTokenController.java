package br.com.jorgeacetozi.ebookChat.authentication.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.jorgeacetozi.ebookChat.audit.domain.service.AuditService;
import br.com.jorgeacetozi.ebookChat.authentication.domain.model.User;
import br.com.jorgeacetozi.ebookChat.authentication.domain.repository.UserRepository;
import br.com.jorgeacetozi.ebookChat.authentication.domain.service.JwtTokenService;
import br.com.jorgeacetozi.ebookChat.authentication.domain.service.UserService;

import java.util.Collections;
import java.util.Map;

/**
 * REST endpoint for API/WebSocket clients to obtain JWT and register.
 * BR-1.1: POST /api/auth/token returns JWT.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthTokenController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final AuditService auditService;
    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public AuthTokenController(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService,
            AuditService auditService, UserService userService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.auditService = auditService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> token(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "username and password required"));
        }
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            auditService.logEvent(username, "LOGIN", "api/auth/token", "allow", null, "JWT issued");
            String token = jwtTokenService.generateToken(auth);
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "Invalid credentials"));
        }
    }

    /** Optional: register from Next.js/React. Body: username, password (name/email optional). */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body != null ? body.get("username") : null;
        String password = body != null ? body.get("password") : null;
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "username required"));
        }
        if (password == null || password.length() < 5) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "password must be at least 5 characters"));
        }
        if (username.length() < 5 || username.length() > 15) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "username must be 5–15 characters"));
        }
        if (userRepository.exists(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", "username already exists"));
        }
        String name = body.get("name");
        if (name == null || name.trim().isEmpty()) name = username;
        String email = body.get("email");
        if (email == null || email.trim().isEmpty()) email = username + "@local";
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setName(name);
        user.setEmail(email);
        userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonMap("message", "Account created"));
    }
}
