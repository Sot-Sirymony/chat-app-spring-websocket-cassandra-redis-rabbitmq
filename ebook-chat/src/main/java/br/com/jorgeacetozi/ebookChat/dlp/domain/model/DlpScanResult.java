package br.com.jorgeacetozi.ebookChat.dlp.domain.model;

import java.util.Collections;
import java.util.List;

/**
 * Result of DLP scan: risk level, matched rule ids, and action (BR-4.1).
 */
public class DlpScanResult {

	private final DlpRiskLevel riskLevel;
	private final List<String> matchedRuleIds;
	private final DlpAction action;
	private final String message;

	public DlpScanResult(DlpRiskLevel riskLevel, List<String> matchedRuleIds, DlpAction action, String message) {
		this.riskLevel = riskLevel;
		this.matchedRuleIds = matchedRuleIds != null ? matchedRuleIds : Collections.<String>emptyList();
		this.action = action;
		this.message = message;
	}

	public static DlpScanResult allow() {
		return new DlpScanResult(DlpRiskLevel.LOW, Collections.<String>emptyList(), DlpAction.ALLOW, null);
	}

	public DlpRiskLevel getRiskLevel() { return riskLevel; }
	public List<String> getMatchedRuleIds() { return matchedRuleIds; }
	public DlpAction getAction() { return action; }
	public String getMessage() { return message; }
}
