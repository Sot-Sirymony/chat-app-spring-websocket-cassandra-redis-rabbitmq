package br.com.jorgeacetozi.ebookChat.chatroom.domain.policy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.jorgeacetozi.ebookChat.configuration.AbacProperties;

/**
 * T2.1.9: Simple risk score per user (e.g. recent denials). Used in ABAC.
 */
@Service
public class UserRiskScoreService {

	private final ConcurrentHashMap<String, AtomicInteger> denialCountByUser = new ConcurrentHashMap<>();
	private final int maxScore;
	private final int denialIncrement;

	@Autowired
	public UserRiskScoreService(AbacProperties abacProperties) {
		// Risk score 0-100; each denial adds points; cap at 100
		this.denialIncrement = 20;
		this.maxScore = 100;
	}

	public void recordDenial(String username) {
		if (username == null || username.isEmpty()) return;
		denialCountByUser.computeIfAbsent(username, k -> new AtomicInteger(0)).addAndGet(1);
	}

	/**
	 * Returns risk score 0-100 based on denial count (simple: count * increment, capped).
	 */
	public int getScore(String username) {
		if (username == null || username.isEmpty()) return 0;
		AtomicInteger c = denialCountByUser.get(username);
		if (c == null) return 0;
		int score = Math.min(maxScore, c.get() * denialIncrement);
		return score;
	}
}
