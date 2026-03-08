package br.com.jorgeacetozi.ebookChat.dlp.domain.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import br.com.jorgeacetozi.ebookChat.dlp.configuration.PresidioProperties;
import br.com.jorgeacetozi.ebookChat.metrics.ChatMetricsService;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpAction;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpRiskLevel;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpScanResult;
import br.com.jorgeacetozi.ebookChat.dlp.presidio.PresidioAnalyzerClient;
import br.com.jorgeacetozi.ebookChat.dlp.presidio.PresidioAnalyzerClient.PresidioEntity;

/**
 * TP.4: DLP provider that calls Presidio Analyzer and maps entity types to DlpScanResult.
 */
@Component
@ConditionalOnProperty(name = "ebook.chat.presidio.enabled", havingValue = "true")
public class PresidioDlpProvider implements DlpProvider {

	private final PresidioProperties properties;
	private final PresidioAnalyzerClient client;
	private final ChatMetricsService metricsService;

	@Autowired
	public PresidioDlpProvider(PresidioProperties properties, PresidioAnalyzerClient client,
			@Autowired(required = false) ChatMetricsService metricsService) {
		this.properties = properties;
		this.client = client;
		this.metricsService = metricsService;
	}

	@Override
	public DlpScanResult scan(String text) {
		if (text == null || text.isEmpty()) return DlpScanResult.allow();
		long start = System.currentTimeMillis();
		List<PresidioEntity> entities;
		try {
			entities = client.analyze(text);
			if (metricsService != null) metricsService.recordPresidioCall(true, System.currentTimeMillis() - start);
		} catch (Exception e) {
			if (metricsService != null) metricsService.recordPresidioCall(false, System.currentTimeMillis() - start);
			return DlpScanResult.allow();
		}
		List<String> entityFilter = properties.getEntityFilter();
		double threshold = properties.getScoreThreshold();
		List<String> matched = new ArrayList<>();
		DlpRiskLevel maxLevel = DlpRiskLevel.LOW;

		for (PresidioEntity e : entities) {
			if (e.getScore() < threshold) continue;
			String et = e.getEntity_type();
			if (et == null) continue;
			if (entityFilter != null && !entityFilter.isEmpty() && !entityFilter.contains(et)) continue;
			DlpRiskLevel level = properties.riskLevelForEntity(et);
			matched.add("presidio:" + et);
			if (level.ordinal() > maxLevel.ordinal()) maxLevel = level;
		}

		if (matched.isEmpty()) return DlpScanResult.allow();
		DlpAction action = actionFor(maxLevel);
		String msg = "Presidio PII: " + String.join(", ", matched) + " (risk: " + maxLevel + ")";
		return new DlpScanResult(maxLevel, matched, action, msg);
	}

	private DlpAction actionFor(DlpRiskLevel level) {
		switch (level) {
			case CRITICAL: return DlpAction.BLOCK;
			case HIGH:    return DlpAction.REQUIRE_APPROVAL;
			case MEDIUM:  return DlpAction.WARN;
			case LOW:
			default:      return DlpAction.ALLOW;
		}
	}
}
