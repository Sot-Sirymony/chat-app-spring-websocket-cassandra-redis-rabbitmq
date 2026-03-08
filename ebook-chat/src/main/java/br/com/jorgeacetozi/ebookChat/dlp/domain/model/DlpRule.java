package br.com.jorgeacetozi.ebookChat.dlp.domain.model;

/**
 * Single DLP rule: keyword or regex pattern with risk level (BR-4.1).
 */
public class DlpRule {

	public enum Type { KEYWORD, REGEX }

	private String id;
	private Type type;
	private String pattern;
	private DlpRiskLevel riskLevel;

	public DlpRule() {}

	public DlpRule(String id, Type type, String pattern, DlpRiskLevel riskLevel) {
		this.id = id;
		this.type = type;
		this.pattern = pattern;
		this.riskLevel = riskLevel;
	}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public Type getType() { return type; }
	public void setType(Type type) { this.type = type; }
	public String getPattern() { return pattern; }
	public void setPattern(String pattern) { this.pattern = pattern; }
	public DlpRiskLevel getRiskLevel() { return riskLevel; }
	public void setRiskLevel(DlpRiskLevel riskLevel) { this.riskLevel = riskLevel; }
}
