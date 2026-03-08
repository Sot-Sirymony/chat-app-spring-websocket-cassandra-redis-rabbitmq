package br.com.jorgeacetozi.ebookChat.unitTests.phase1;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import br.com.jorgeacetozi.ebookChat.authentication.domain.service.JwtTokenService;
import br.com.jorgeacetozi.ebookChat.configuration.JwtProperties;
import io.jsonwebtoken.Claims;

/**
 * Phase 1 (BR-1.1): Test cases for JWT token generation and parsing — T1.1.1–T1.1.3.
 */
public class JwtTokenServiceTest {

	private JwtTokenService jwtTokenService;

	@Before
	public void setUp() {
		JwtProperties props = new JwtProperties();
		props.setSecret("ebook-chat-internal-secret-key-min-256-bits-for-hs256");
		props.setIssuer("ebook-chat");
		props.setExpirationMs(3600000L);
		jwtTokenService = new JwtTokenService(props);
	}

	@Test
	public void shouldGenerateTokenWithUsernameAndRoles() {
		Authentication auth = new UsernamePasswordAuthenticationToken("alice",
				null,
				Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
		String token = jwtTokenService.generateToken(auth);
		assertThat(token, notNullValue());
		assertTrue(token.split("\\.").length == 3);
	}

	@Test
	public void shouldParseTokenAndReturnClaimsWithSubjectAndRoles() {
		Authentication auth = new UsernamePasswordAuthenticationToken("bob",
				null,
				Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
		String token = jwtTokenService.generateToken(auth);
		Claims claims = jwtTokenService.parseToken(token);
		assertNotNull(claims);
		assertEquals("bob", claims.getSubject());
		assertEquals("ebook-chat", claims.getIssuer());
		assertNotNull(claims.get("roles"));
		assertTrue(claims.get("roles").toString().contains("ROLE_ADMIN"));
	}

	@Test(expected = Exception.class)
	public void shouldRejectInvalidToken() {
		jwtTokenService.parseToken("invalid.jwt.token");
	}
}
