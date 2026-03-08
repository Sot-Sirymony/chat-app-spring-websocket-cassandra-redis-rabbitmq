package br.com.jorgeacetozi.ebookChat.chatroom.websocket;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import br.com.jorgeacetozi.ebookChat.authentication.domain.model.User;
import br.com.jorgeacetozi.ebookChat.authentication.domain.repository.UserRepository;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.model.ChatRoom;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.model.ChatRoomUser;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.model.RoomClassification;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.policy.AbacContext;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.policy.AbacPolicyService;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.policy.UserRiskScoreService;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.policy.PolicyDecision;
import br.com.jorgeacetozi.ebookChat.audit.domain.service.AuditService;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.service.ChatRoomService;

@Component
public class WebSocketEvents {

	@Autowired
	private ChatRoomService chatRoomService;

	@Autowired
	private AbacPolicyService abacPolicyService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private AuditService auditService;

	@Autowired
	private UserRiskScoreService userRiskScoreService;

	@EventListener
	private void handleSessionConnected(SessionConnectEvent event) {
		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
		String chatRoomId = headers.getNativeHeader("chatRoomId").get(0);
		headers.getSessionAttributes().put("chatRoomId", chatRoomId);
		ChatRoomUser joiningUser = new ChatRoomUser(event.getUser().getName());
		ChatRoom room = chatRoomService.findById(chatRoomId);

		AbacContext ctx = new AbacContext();
		ctx.setUsername(event.getUser().getName());
		ctx.setRoomId(chatRoomId);
		ctx.setRoomLevel(room.getClassification() != null ? room.getClassification() : RoomClassification.PUBLIC);
		ctx.setRiskScore(userRiskScoreService.getScore(event.getUser().getName()));
		if (headers.getSessionAttributes() != null) {
			Object dt = headers.getSessionAttributes().get("deviceType");
			if (dt != null) ctx.setDeviceType(dt.toString());
		}
		User user = userRepository.findOne(event.getUser().getName());
		if (user != null) {
			ctx.setDepartment(user.getDepartment());
			Set<String> roles = user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet());
			ctx.setRoles(roles);
		}
		PolicyDecision decision = abacPolicyService.evaluateJoinRoom(ctx);
		if (!decision.isAllowed()) {
			auditService.logEvent(event.getUser().getName(), "JOIN_ROOM", chatRoomId, "deny", decision.getRuleId(), decision.getReason());
			messagingTemplate.convertAndSendToUser(event.getUser().getName(), "/queue/policy-denial",
					java.util.Collections.singletonMap("reason", decision.getReason()));
			return;
		}
		auditService.logEvent(event.getUser().getName(), "JOIN_ROOM", chatRoomId, "allow", decision.getRuleId(), null);
		chatRoomService.join(joiningUser, room);
	}

	@EventListener
	private void handleSessionDisconnect(SessionDisconnectEvent event) {
		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
		String chatRoomId = headers.getSessionAttributes().get("chatRoomId").toString();
		ChatRoomUser leavingUser = new ChatRoomUser(event.getUser().getName());

		chatRoomService.leave(leavingUser, chatRoomService.findById(chatRoomId));
	}
}
