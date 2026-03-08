package br.com.jorgeacetozi.ebookChat.dlp.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpRiskLevel;
import br.com.jorgeacetozi.ebookChat.dlp.domain.model.DlpRule;

/**
 * DLP rules loaded from application.yml (ebook.chat.dlp) — BR-4.1.
 */
@ConfigurationProperties(prefix = "ebook.chat.dlp")
public class DlpProperties {

	private List<RuleEntry> rules = new ArrayList<>();

	public static class RuleEntry {
		private String id;
		private String type; // KEYWORD, REGEX
		private String pattern;
		private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL

		public String getId() { return id; }
		public void setId(String id) { this.id = id; }
		public String getType() { return type; }
		public void setType(String type) { this.type = type; }
		public String getPattern() { return pattern; }
		public void setPattern(String pattern) { this.pattern = pattern; }
		public String getRiskLevel() { return riskLevel; }
		public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
	}

	public List<RuleEntry> getRules() { return rules; }
	public void setRules(List<RuleEntry> rules) { this.rules = rules != null ? rules : new ArrayList<RuleEntry>(); }

	public List<DlpRule> toDlpRules() {
		List<DlpRule> out = new ArrayList<>();
		for (RuleEntry e : rules) {
			DlpRule r = new DlpRule();
			r.setId(e.getId());
			r.setType("REGEX".equalsIgnoreCase(e.getType()) ? DlpRule.Type.REGEX : DlpRule.Type.KEYWORD);
			r.setPattern(e.getPattern());
			try {
				r.setRiskLevel(DlpRiskLevel.valueOf(e.getRiskLevel() != null ? e.getRiskLevel().toUpperCase() : "LOW"));
			} catch (Exception ex) {
				r.setRiskLevel(DlpRiskLevel.LOW);
			}
			out.add(r);
		}
		return out;
	}
}
