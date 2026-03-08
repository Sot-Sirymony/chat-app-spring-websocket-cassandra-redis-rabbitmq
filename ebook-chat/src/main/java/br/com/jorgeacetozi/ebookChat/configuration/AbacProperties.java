package br.com.jorgeacetozi.ebookChat.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ABAC config (T2.1.8): trusted device patterns for RESTRICTED rooms.
 */
@ConfigurationProperties(prefix = "ebook.chat.abac")
public class AbacProperties {

	private List<String> trustedDevicePatterns = new ArrayList<>();
	/** T2.1.9: Deny if user risk score >= this (0 = disabled). */
	private int riskScoreThreshold = 0;

	public List<String> getTrustedDevicePatterns() {
		return trustedDevicePatterns;
	}

	public void setTrustedDevicePatterns(List<String> trustedDevicePatterns) {
		this.trustedDevicePatterns = trustedDevicePatterns != null ? trustedDevicePatterns : new ArrayList<>();
	}

	public int getRiskScoreThreshold() {
		return riskScoreThreshold;
	}

	public void setRiskScoreThreshold(int riskScoreThreshold) {
		this.riskScoreThreshold = riskScoreThreshold;
	}
}
