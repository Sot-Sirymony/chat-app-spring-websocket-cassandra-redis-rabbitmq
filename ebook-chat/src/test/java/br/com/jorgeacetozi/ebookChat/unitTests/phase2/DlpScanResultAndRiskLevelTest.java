package br.com.jorgeacetozi.ebookChat.unitTests.phase2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.Test;

import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpAction;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpRiskLevel;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpScanResult;

/**
 * Phase 2 (BR-4.1) T4.1.1: Test risk levels and actions (ALLOW, WARN, BLOCK, REQUIRE_APPROVAL).
 */
public class DlpScanResultAndRiskLevelTest {

	@Test
	public void shouldProvideAllowResult() {
		DlpScanResult r = DlpScanResult.allow();
		assertNotNull(r);
		assertEquals(DlpRiskLevel.LOW, r.getRiskLevel());
		assertEquals(DlpAction.ALLOW, r.getAction());
		assertEquals(0, r.getMatchedRuleIds().size());
	}

	@Test
	public void shouldConstructResultWithRiskAndRuleIds() {
		DlpScanResult r = new DlpScanResult(DlpRiskLevel.CRITICAL,
				Collections.singletonList("credit-card"),
				DlpAction.BLOCK,
				"Matched CREDIT_CARD");
		assertEquals(DlpRiskLevel.CRITICAL, r.getRiskLevel());
		assertEquals(DlpAction.BLOCK, r.getAction());
		assertEquals(1, r.getMatchedRuleIds().size());
		assertEquals("credit-card", r.getMatchedRuleIds().get(0));
	}

	@Test
	public void shouldHaveAllRiskLevelsInEnum() {
		assertEquals(4, DlpRiskLevel.values().length);
		assertEquals(DlpRiskLevel.LOW, DlpRiskLevel.valueOf("LOW"));
		assertEquals(DlpRiskLevel.CRITICAL, DlpRiskLevel.valueOf("CRITICAL"));
	}
}
