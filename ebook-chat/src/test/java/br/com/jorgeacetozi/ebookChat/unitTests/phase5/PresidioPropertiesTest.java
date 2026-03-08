package br.com.jorgeacetozi.ebookChat.unitTests.phase5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import br.com.jorgeacetozi.ebookChat.dlp.configuration.PresidioProperties;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpRiskLevel;

/**
 * Phase 5 (BR-4.2) TP.1, TP.3: Test Presidio config and entity→risk mapping.
 */
public class PresidioPropertiesTest {

	private PresidioProperties properties;

	@Before
	public void setUp() {
		properties = new PresidioProperties();
	}

	@Test
	public void shouldDefaultDisabled() {
		assertFalse(properties.isEnabled());
	}

	@Test
	public void shouldHaveDefaultAnalyzerUrl() {
		assertTrue(properties.getAnalyzerBaseUrl().contains("localhost"));
		assertTrue(properties.getAnalyzerBaseUrl().contains("5002"));
	}

	@Test
	public void shouldMapUsSsnToHighByDefault() {
		assertEquals(DlpRiskLevel.HIGH, properties.riskLevelForEntity("US_SSN"));
	}

	@Test
	public void shouldMapCreditCardToCriticalByDefault() {
		assertEquals(DlpRiskLevel.CRITICAL, properties.riskLevelForEntity("CREDIT_CARD"));
	}

	@Test
	public void shouldMapPersonToMediumByDefault() {
		assertEquals(DlpRiskLevel.MEDIUM, properties.riskLevelForEntity("PERSON"));
	}

	@Test
	public void shouldUseCustomMappingWhenSet() {
		Map<String, String> custom = new HashMap<>();
		custom.put("US_SSN", "CRITICAL");
		custom.put("UNKNOWN_ENTITY", "LOW");
		properties.setEntityRiskMapping(custom);
		assertEquals(DlpRiskLevel.CRITICAL, properties.riskLevelForEntity("US_SSN"));
		assertEquals(DlpRiskLevel.LOW, properties.riskLevelForEntity("UNKNOWN_ENTITY"));
	}

	@Test
	public void shouldReturnLowForUnknownEntity() {
		assertEquals(DlpRiskLevel.LOW, properties.riskLevelForEntity("SOME_UNKNOWN_TYPE"));
	}
}
