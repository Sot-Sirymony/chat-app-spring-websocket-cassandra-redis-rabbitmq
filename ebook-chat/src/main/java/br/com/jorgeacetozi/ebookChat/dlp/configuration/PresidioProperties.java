package br.com.jorgeacetozi.ebookChat.dlp.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpRiskLevel;

/**
 * BR-4.2 (TP.1): Presidio Analyzer integration — base URL, enabled, timeout, entity→risk mapping.
 */
@ConfigurationProperties(prefix = "ebook.chat.presidio")
public class PresidioProperties {

	/** Base URL of Presidio Analyzer (e.g. http://localhost:5002). */
	private String analyzerBaseUrl = "http://localhost:5002";
	/** Enable Presidio as additional DLP provider. */
	private boolean enabled = false;
	/** HTTP timeout in milliseconds. */
	private int timeoutMs = 5000;
	/** Language code sent to Presidio (e.g. en). */
	private String language = "en";
	/** Minimum score (0–1) to consider a Presidio entity a hit. */
	private double scoreThreshold = 0.5;
	/** Optional: only these entity types (empty = all). */
	private List<String> entityFilter = null;
	/** Map Presidio entity_type to our DlpRiskLevel (e.g. US_SSN -> HIGH). */
	private Map<String, String> entityRiskMapping = defaultEntityRiskMapping();

	private static Map<String, String> defaultEntityRiskMapping() {
		Map<String, String> m = new HashMap<>();
		m.put("US_SSN", "HIGH");
		m.put("CREDIT_CARD", "CRITICAL");
		m.put("PHONE_NUMBER", "MEDIUM");
		m.put("EMAIL_ADDRESS", "MEDIUM");
		m.put("PERSON", "MEDIUM");
		m.put("US_DRIVER_LICENSE", "HIGH");
		m.put("US_PASSPORT", "HIGH");
		m.put("IBAN_CODE", "HIGH");
		m.put("MEDICAL_LICENSE", "HIGH");
		return m;
	}

	public String getAnalyzerBaseUrl() { return analyzerBaseUrl; }
	public void setAnalyzerBaseUrl(String analyzerBaseUrl) { this.analyzerBaseUrl = analyzerBaseUrl; }
	public boolean isEnabled() { return enabled; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	public int getTimeoutMs() { return timeoutMs; }
	public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
	public String getLanguage() { return language; }
	public void setLanguage(String language) { this.language = language; }
	public double getScoreThreshold() { return scoreThreshold; }
	public void setScoreThreshold(double scoreThreshold) { this.scoreThreshold = scoreThreshold; }
	public List<String> getEntityFilter() { return entityFilter; }
	public void setEntityFilter(List<String> entityFilter) { this.entityFilter = entityFilter; }
	public Map<String, String> getEntityRiskMapping() { return entityRiskMapping != null ? entityRiskMapping : defaultEntityRiskMapping(); }
	public void setEntityRiskMapping(Map<String, String> entityRiskMapping) { this.entityRiskMapping = entityRiskMapping; }

	/** Resolve Presidio entity type to DlpRiskLevel. */
	public DlpRiskLevel riskLevelForEntity(String entityType) {
		String level = getEntityRiskMapping().get(entityType);
		if (level == null) level = "LOW";
		try {
			return DlpRiskLevel.valueOf(level.toUpperCase());
		} catch (Exception e) {
			return DlpRiskLevel.LOW;
		}
	}
}
