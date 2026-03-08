package br.com.jorgeacetozi.ebookChat.chatroom.domain.policy;

/**
 * Result of policy evaluation (BR-2.1).
 */
public class PolicyDecision {

	private final boolean allowed;
	private final String reason;
	private final String ruleId;

	public PolicyDecision(boolean allowed, String reason, String ruleId) {
		this.allowed = allowed;
		this.reason = reason;
		this.ruleId = ruleId;
	}

	public static PolicyDecision allow(String ruleId) {
		return new PolicyDecision(true, null, ruleId);
	}

	public static PolicyDecision deny(String reason, String ruleId) {
		return new PolicyDecision(false, reason, ruleId);
	}

	public boolean isAllowed() { return allowed; }
	public String getReason() { return reason; }
	public String getRuleId() { return ruleId; }
}
