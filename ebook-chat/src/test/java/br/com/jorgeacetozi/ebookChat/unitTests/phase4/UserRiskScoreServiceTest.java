package br.com.jorgeacetozi.ebookChat.unitTests.phase4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import br.com.jorgeacetozi.ebookChat.chatroom.domain.policy.UserRiskScoreService;
import br.com.jorgeacetozi.ebookChat.configuration.AbacProperties;

/**
 * Phase 4 (BR-2.1) T2.1.9: Test user risk score — denial count and score 0–100.
 */
public class UserRiskScoreServiceTest {

	private UserRiskScoreService userRiskScoreService;

	@Before
	public void setUp() {
		userRiskScoreService = new UserRiskScoreService(new AbacProperties());
	}

	@Test
	public void shouldReturnZeroScoreForNewUser() {
		int score = userRiskScoreService.getScore("newuser");
		assertEquals(0, score);
	}

	@Test
	public void shouldIncreaseScoreAfterDenials() {
		userRiskScoreService.recordDenial("alice");
		userRiskScoreService.recordDenial("alice");
		int score = userRiskScoreService.getScore("alice");
		assertTrue(score >= 0 && score <= 100);
		assertTrue(score > userRiskScoreService.getScore("newuser"));
	}

	@Test
	public void shouldCapScoreAt100() {
		for (int i = 0; i < 200; i++) {
			userRiskScoreService.recordDenial("bob");
		}
		int score = userRiskScoreService.getScore("bob");
		assertEquals(100, score);
	}
}
