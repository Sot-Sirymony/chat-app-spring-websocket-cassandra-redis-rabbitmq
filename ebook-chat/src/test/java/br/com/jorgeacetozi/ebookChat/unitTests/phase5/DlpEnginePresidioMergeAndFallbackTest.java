package br.com.jorgeacetozi.ebookChat.unitTests.phase5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import br.com.jorgeacetozi.ebookChat.dlp.configuration.DlpProperties;
import br.com.jorgeacetozi.ebookChat.dlp.configuration.PresidioProperties;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpAction;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpRiskLevel;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpScanResult;
import br.com.jorgeacetozi.ebookChat.dlp.domain.service.DlpEngine;
import br.com.jorgeacetozi.ebookChat.dlp.domain.service.PresidioDlpProvider;
import br.com.jorgeacetozi.ebookChat.dlp.domain.service.RuleBasedDlpProvider;

/**
 * Phase 5 (BR-4.2) TP.5: Test DlpEngine merge (rule-based + Presidio) and fallback when Presidio unavailable.
 */
public class DlpEnginePresidioMergeAndFallbackTest {

	private DlpProperties dlpProperties;
	private PresidioProperties presidioProperties;
	private RuleBasedDlpProvider ruleBasedDlpProvider;

	@Before
	public void setUp() {
		dlpProperties = new DlpProperties();
		DlpProperties.RuleEntry r = new DlpProperties.RuleEntry();
		r.setId("keyword-secret");
		r.setType("KEYWORD");
		r.setPattern("secret");
		r.setRiskLevel("MEDIUM");
		dlpProperties.setRules(Collections.singletonList(r));

		presidioProperties = new PresidioProperties();
		presidioProperties.setEnabled(false);

		ruleBasedDlpProvider = new RuleBasedDlpProvider(dlpProperties);
		ruleBasedDlpProvider.loadRules();
	}

	@Test
	public void whenPresidioDisabled_shouldUseOnlyRuleBasedResult() {
		DlpEngine engine = new DlpEngine(ruleBasedDlpProvider, presidioProperties, Optional.empty());
		DlpScanResult result = engine.scan("This is secret.");
		assertEquals(DlpAction.WARN, result.getAction());
		assertTrue(result.getMatchedRuleIds().contains("keyword-secret"));
	}

	@Test
	public void whenPresidioEnabledButProviderNull_shouldUseOnlyRuleBasedResult() {
		presidioProperties.setEnabled(true);
		DlpEngine engine = new DlpEngine(ruleBasedDlpProvider, presidioProperties, Optional.empty());
		DlpScanResult result = engine.scan("This is secret.");
		assertEquals(DlpAction.WARN, result.getAction());
	}

	@Test
	public void whenPresidioProviderReturnsAllow_mergeShouldKeepRuleBasedResult() {
		presidioProperties.setEnabled(true);
		PresidioDlpProvider mockPresidio = mock(PresidioDlpProvider.class);
		when(mockPresidio.scan(anyString())).thenReturn(DlpScanResult.allow());
		DlpEngine engine = new DlpEngine(ruleBasedDlpProvider, presidioProperties, Optional.of(mockPresidio));
		DlpScanResult result = engine.scan("This is secret.");
		assertEquals(DlpAction.WARN, result.getAction());
		assertTrue(result.getMatchedRuleIds().contains("keyword-secret"));
	}

	@Test
	public void whenBothMatch_mergeShouldTakeMaxRiskAndCombineRuleIds() {
		presidioProperties.setEnabled(true);
		PresidioDlpProvider mockPresidio = mock(PresidioDlpProvider.class);
		when(mockPresidio.scan(anyString())).thenReturn(new DlpScanResult(
				DlpRiskLevel.HIGH,
				Collections.singletonList("presidio:US_SSN"),
				DlpAction.REQUIRE_APPROVAL,
				"Presidio PII"));
		DlpEngine engine = new DlpEngine(ruleBasedDlpProvider, presidioProperties, Optional.of(mockPresidio));
		DlpScanResult result = engine.scan("secret and 123-45-6789");
		assertEquals(DlpAction.REQUIRE_APPROVAL, result.getAction());
		assertEquals(DlpRiskLevel.HIGH, result.getRiskLevel());
		assertTrue(result.getMatchedRuleIds().contains("keyword-secret"));
		assertTrue(result.getMatchedRuleIds().contains("presidio:US_SSN"));
	}
}
