package br.com.jorgeacetozi.ebookChat.unitTests.phase1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import br.com.jorgeacetozi.ebookChat.chatroom.domain.model.RoomClassification;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.policy.AbacContext;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.policy.AbacPolicyService;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.policy.PolicyDecision;
import br.com.jorgeacetozi.ebookChat.configuration.AbacProperties;

/**
 * Phase 1 (BR-2.1, BR-2.2): Test cases for ABAC policy — T2.1.3–T2.1.5, T2.2.3–T2.2.4.
 */
public class AbacPolicyServiceTest {

	private AbacPolicyService abacPolicyService;

	@Before
	public void setUp() {
		AbacProperties props = new AbacProperties();
		props.setTrustedDevicePatterns(Collections.singletonList("corp-"));
		props.setRiskScoreThreshold(0);
		abacPolicyService = new AbacPolicyService(props);
	}

	@Test
	public void shouldDenyWhenUsernameIsNull() {
		AbacContext ctx = new AbacContext();
		ctx.setUsername(null);
		ctx.setRoles(Collections.singleton("ROLE_USER"));
		PolicyDecision d = abacPolicyService.evaluateSendMessage(ctx);
		assertFalse(d.isAllowed());
		assertTrue(d.getReason().contains("Not authenticated"));
	}

	@Test
	public void shouldDenyWhenUserHasNoRole() {
		AbacContext ctx = new AbacContext();
		ctx.setUsername("alice");
		ctx.setRoles(Collections.emptySet());
		PolicyDecision d = abacPolicyService.evaluateSendMessage(ctx);
		assertFalse(d.isAllowed());
		assertTrue(d.getReason().contains("no role"));
	}

	@Test
	public void shouldAllowWhenUserHasRoleAndPublicRoom() {
		AbacContext ctx = new AbacContext();
		ctx.setUsername("alice");
		ctx.setRoles(Collections.singleton("ROLE_USER"));
		ctx.setRoomLevel(RoomClassification.PUBLIC);
		PolicyDecision d = abacPolicyService.evaluateSendMessage(ctx);
		assertTrue(d.isAllowed());
	}

	@Test
	public void shouldDenyRestrictedRoomWhenDepartmentMissing() {
		AbacContext ctx = new AbacContext();
		ctx.setUsername("alice");
		ctx.setRoles(Collections.singleton("ROLE_USER"));
		ctx.setRoomLevel(RoomClassification.RESTRICTED);
		ctx.setDepartment(null);
		PolicyDecision d = abacPolicyService.evaluateSendMessage(ctx);
		assertFalse(d.isAllowed());
		assertTrue(d.getReason().contains("department"));
	}

	@Test
	public void shouldDenyRestrictedRoomWhenDeviceNotTrusted() {
		AbacProperties props = new AbacProperties();
		props.setTrustedDevicePatterns(Collections.singletonList("corp-"));
		props.setRiskScoreThreshold(0);
		AbacPolicyService service = new AbacPolicyService(props);
		AbacContext ctx = new AbacContext();
		ctx.setUsername("alice");
		ctx.setRoles(Collections.singleton("ROLE_USER"));
		ctx.setRoomLevel(RoomClassification.RESTRICTED);
		ctx.setDepartment("Engineering");
		ctx.setDeviceType("home-laptop");
		PolicyDecision d = service.evaluateSendMessage(ctx);
		assertFalse(d.isAllowed());
		assertTrue(d.getReason() != null && d.getReason().contains("device"));
	}

	@Test
	public void shouldAllowRestrictedRoomWhenDepartmentAndTrustedDevice() {
		AbacContext ctx = new AbacContext();
		ctx.setUsername("alice");
		ctx.setRoles(Collections.singleton("ROLE_USER"));
		ctx.setRoomLevel(RoomClassification.RESTRICTED);
		ctx.setDepartment("Engineering");
		ctx.setDeviceType("corp-laptop-01");
		PolicyDecision d = abacPolicyService.evaluateSendMessage(ctx);
		assertTrue(d.isAllowed());
	}

	@Test
	public void shouldDenyJoinWhenRiskScoreAboveThreshold() {
		AbacProperties props = new AbacProperties();
		props.setRiskScoreThreshold(80);
		AbacPolicyService service = new AbacPolicyService(props);
		AbacContext ctx = new AbacContext();
		ctx.setUsername("alice");
		ctx.setRoles(Collections.singleton("ROLE_USER"));
		ctx.setRoomLevel(RoomClassification.PUBLIC);
		ctx.setRiskScore(90);
		PolicyDecision d = service.evaluateJoinRoom(ctx);
		assertFalse(d.isAllowed());
		assertTrue(d.getReason().contains("risk score"));
	}
}
