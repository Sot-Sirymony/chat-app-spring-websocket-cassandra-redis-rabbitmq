package br.com.jorgeacetozi.ebookChat.unitTests.phase2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import br.com.jorgeacetozi.ebookChat.dlp.configuration.DlpProperties;
import br.com.jorgeacetozi.ebookChat.dlp.configuration.PresidioProperties;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpAction;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpRiskLevel;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpScanResult;
import br.com.jorgeacetozi.ebookChat.dlp.domain.service.DlpEngine;
import br.com.jorgeacetozi.ebookChat.dlp.domain.service.RuleBasedDlpProvider;

/**
 * Phase 2 (BR-4.1): Test cases for DLP engine — T4.1.1–T4.1.4 (rule-based scan, risk levels, file scan).
 */
public class DlpEngineAndRuleBasedProviderTest {

	private DlpProperties dlpProperties;
	private PresidioProperties presidioProperties;
	private RuleBasedDlpProvider ruleBasedDlpProvider;
	private DlpEngine dlpEngine;

	@Before
	public void setUp() {
		dlpProperties = new DlpProperties();
		List<DlpProperties.RuleEntry> rules = new ArrayList<>();
		DlpProperties.RuleEntry r1 = new DlpProperties.RuleEntry();
		r1.setId("pii-ssn");
		r1.setType("REGEX");
		r1.setPattern("\\b\\d{3}-\\d{2}-\\d{4}\\b");
		r1.setRiskLevel("HIGH");
		rules.add(r1);
		DlpProperties.RuleEntry r2 = new DlpProperties.RuleEntry();
		r2.setId("keyword-secret");
		r2.setType("KEYWORD");
		r2.setPattern("secret");
		r2.setRiskLevel("MEDIUM");
		rules.add(r2);
		dlpProperties.setRules(rules);

		presidioProperties = new PresidioProperties();
		presidioProperties.setEnabled(false);

		ruleBasedDlpProvider = new RuleBasedDlpProvider(dlpProperties);
		ruleBasedDlpProvider.loadRules();

		dlpEngine = new DlpEngine(ruleBasedDlpProvider, presidioProperties, Optional.empty());
	}

	@Test
	public void shouldAllowWhenNoRuleMatches() {
		DlpScanResult result = dlpEngine.scan("Hello world, nothing sensitive here.");
		assertNotNull(result);
		assertEquals(DlpAction.ALLOW, result.getAction());
		assertEquals(DlpRiskLevel.LOW, result.getRiskLevel());
		assertTrue(result.getMatchedRuleIds().isEmpty());
	}

	@Test
	public void shouldDetectKeywordAndReturnWarn() {
		DlpScanResult result = dlpEngine.scan("This is a secret document.");
		assertNotNull(result);
		assertEquals(DlpAction.WARN, result.getAction());
		assertEquals(DlpRiskLevel.MEDIUM, result.getRiskLevel());
		assertTrue(result.getMatchedRuleIds().contains("keyword-secret"));
	}

	@Test
	public void shouldDetectSsnRegexAndReturnRequireApproval() {
		DlpScanResult result = dlpEngine.scan("My SSN is 123-45-6789.");
		assertNotNull(result);
		assertEquals(DlpAction.REQUIRE_APPROVAL, result.getAction());
		assertEquals(DlpRiskLevel.HIGH, result.getRiskLevel());
		assertTrue(result.getMatchedRuleIds().contains("pii-ssn"));
	}

	@Test
	public void shouldAllowNullOrEmptyText() {
		assertEquals(DlpAction.ALLOW, dlpEngine.scan(null).getAction());
		assertEquals(DlpAction.ALLOW, dlpEngine.scan("").getAction());
	}

	@Test
	public void shouldScanFilePlainTextContent() {
		byte[] content = "Contains secret data.".getBytes(java.nio.charset.StandardCharsets.UTF_8);
		DlpScanResult result = dlpEngine.scanFile("text/plain", content);
		assertNotNull(result);
		assertEquals(DlpAction.WARN, result.getAction());
		assertTrue(result.getMatchedRuleIds().contains("keyword-secret"));
	}

	@Test
	public void shouldAllowNonPlainTextFileContent() {
		byte[] content = "secret".getBytes(java.nio.charset.StandardCharsets.UTF_8);
		DlpScanResult result = dlpEngine.scanFile("application/pdf", content);
		assertEquals(DlpAction.ALLOW, result.getAction());
	}

	@Test
	public void shouldAllowEmptyOrNullFileContent() {
		assertEquals(DlpAction.ALLOW, dlpEngine.scanFile("text/plain", null).getAction());
		assertEquals(DlpAction.ALLOW, dlpEngine.scanFile("text/plain", new byte[0]).getAction());
	}
}
