package br.com.jorgeacetozi.ebookChat.dlp.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.jorgeacetozi.ebookChat.dlp.configuration.DlpProperties;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpAction;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpRiskLevel;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpRule;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpScanResult;

import javax.annotation.PostConstruct;

/**
 * TP.4: Rule-based DLP provider (keywords/regex from config). Extracted from original DlpEngine.
 */
@Component
public class RuleBasedDlpProvider implements DlpProvider {

	private final DlpProperties dlpProperties;
	private List<DlpRule> rules = new ArrayList<>();

	@Autowired
	public RuleBasedDlpProvider(DlpProperties dlpProperties) {
		this.dlpProperties = dlpProperties;
	}

	@PostConstruct
	public void loadRules() {
		this.rules = dlpProperties.toDlpRules();
	}

	@Override
	public DlpScanResult scan(String text) {
		if (text == null || text.isEmpty()) {
			return DlpScanResult.allow();
		}
		String normalized = text.toLowerCase();
		List<String> matched = new ArrayList<>();
		DlpRiskLevel maxLevel = DlpRiskLevel.LOW;

		for (DlpRule rule : rules) {
			boolean match = false;
			if (rule.getType() == DlpRule.Type.KEYWORD) {
				String kw = rule.getPattern() == null ? "" : rule.getPattern().toLowerCase();
				match = normalized.contains(kw);
			} else {
				try {
					Pattern p = Pattern.compile(rule.getPattern(), Pattern.CASE_INSENSITIVE);
					match = p.matcher(text).find();
				} catch (Exception e) {
					// invalid regex skip
				}
			}
			if (match && rule.getId() != null) {
				matched.add(rule.getId());
				if (rule.getRiskLevel() != null && rule.getRiskLevel().ordinal() > maxLevel.ordinal()) {
					maxLevel = rule.getRiskLevel();
				}
			}
		}

		if (matched.isEmpty()) {
			return DlpScanResult.allow();
		}

		DlpAction action = actionFor(maxLevel);
		String msg = "Matched DLP rules: " + String.join(", ", matched) + " (risk: " + maxLevel + ")";
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
