package br.com.jorgeacetozi.ebookChat.chatroom.domain.policy;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.jorgeacetozi.ebookChat.chatroom.domain.model.RoomClassification;
import br.com.jorgeacetozi.ebookChat.configuration.AbacProperties;

/**
 * Policy engine: evaluates ABAC rules for send message / room join (BR-2.1).
 * T2.1.8: RESTRICTED room can require trusted device list.
 */
@Service
public class AbacPolicyService {

	private static final String RULE_DEFAULT_ALLOW = "default-allow";
	private static final String RULE_REQUIRE_USER = "require-user-role";
	private static final String RULE_RESTRICTED_ROOM = "restricted-room-department";
	private static final String RULE_RESTRICTED_DEVICE = "restricted-room-trusted-device";
	private static final String RULE_RISK_SCORE = "user-risk-score";

	private final AbacProperties abacProperties;

	@Autowired
	public AbacPolicyService(AbacProperties abacProperties) {
		this.abacProperties = abacProperties != null ? abacProperties : new AbacProperties();
	}

	public PolicyDecision evaluateSendMessage(AbacContext ctx) {
		if (ctx.getUsername() == null) {
			return PolicyDecision.deny("Not authenticated", "no-user");
		}
		int threshold = abacProperties.getRiskScoreThreshold();
		if (threshold > 0 && ctx.getRiskScore() >= threshold) {
			return PolicyDecision.deny("Access denied due to risk score (" + ctx.getRiskScore() + "). Contact admin.", RULE_RISK_SCORE);
		}
		Set<String> roles = ctx.getRoles();
		if (roles == null || roles.isEmpty()) {
			return PolicyDecision.deny("User has no role", RULE_REQUIRE_USER);
		}
		if (!roles.contains("ROLE_USER") && !roles.contains("ROLE_ADMIN")) {
			return PolicyDecision.deny("Insufficient role to send messages", RULE_REQUIRE_USER);
		}
		RoomClassification level = ctx.getRoomLevel();
		if (level == RoomClassification.RESTRICTED) {
			String dept = ctx.getDepartment();
			if (dept == null || dept.isEmpty()) {
				return PolicyDecision.deny("RESTRICTED room requires user department", RULE_RESTRICTED_ROOM);
			}
			PolicyDecision deviceCheck = checkTrustedDevice(ctx);
			if (deviceCheck != null) return deviceCheck;
		}
		return PolicyDecision.allow(RULE_DEFAULT_ALLOW);
	}

	public PolicyDecision evaluateJoinRoom(AbacContext ctx) {
		if (ctx.getUsername() == null) {
			return PolicyDecision.deny("Not authenticated", "no-user");
		}
		int threshold = abacProperties.getRiskScoreThreshold();
		if (threshold > 0 && ctx.getRiskScore() >= threshold) {
			return PolicyDecision.deny("Access denied due to risk score (" + ctx.getRiskScore() + "). Contact admin.", RULE_RISK_SCORE);
		}
		Set<String> roles = ctx.getRoles();
		if (roles == null || roles.isEmpty()) {
			return PolicyDecision.deny("User has no role", RULE_REQUIRE_USER);
		}
		RoomClassification level = ctx.getRoomLevel();
		if (level == RoomClassification.RESTRICTED) {
			String dept = ctx.getDepartment();
			if (dept == null || dept.isEmpty()) {
				return PolicyDecision.deny("RESTRICTED room requires user department", RULE_RESTRICTED_ROOM);
			}
			PolicyDecision deviceCheck = checkTrustedDevice(ctx);
			if (deviceCheck != null) return deviceCheck;
		}
		return PolicyDecision.allow(RULE_DEFAULT_ALLOW);
	}

	private PolicyDecision checkTrustedDevice(AbacContext ctx) {
		List<String> patterns = abacProperties.getTrustedDevicePatterns();
		if (patterns == null || patterns.isEmpty()) {
			return null;
		}
		String deviceType = ctx.getDeviceType();
		if (deviceType == null || deviceType.isEmpty()) {
			return PolicyDecision.deny("RESTRICTED room requires a trusted device; device type unknown.", RULE_RESTRICTED_DEVICE);
		}
		String lower = deviceType.toLowerCase();
		for (String pattern : patterns) {
			if (pattern != null && !pattern.isEmpty() && lower.contains(pattern.toLowerCase())) {
				return null;
			}
		}
		return PolicyDecision.deny("RESTRICTED room only allows trusted devices. Your device is not in the allowed list.", RULE_RESTRICTED_DEVICE);
	}
}
