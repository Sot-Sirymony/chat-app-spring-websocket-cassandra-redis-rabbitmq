package br.com.jorgeacetozi.ebookChat.unitTests.phase1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.jorgeacetozi.ebookChat.analytics.AnalyticsAggregator;
import br.com.jorgeacetozi.ebookChat.audit.domain.model.AuditEvent;
import br.com.jorgeacetozi.ebookChat.audit.domain.repository.AuditEventRepository;
import br.com.jorgeacetozi.ebookChat.audit.domain.service.AuditService;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.policy.UserRiskScoreService;

/**
 * Phase 1 (BR-5.1): Test cases for audit logging — T5.1.3, T5.1.4.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuditServiceTest {

	@Mock
	private AuditEventRepository repository;

	@Mock
	private AnalyticsAggregator analyticsAggregator;

	@Mock
	private UserRiskScoreService userRiskScoreService;

	@InjectMocks
	private AuditService auditService;

	@Test
	public void shouldSaveAuditEventOnLogEvent() {
		auditService.logEvent("alice", "SEND_MESSAGE", "room-1", "allow", "rule-1", "OK");
		ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
		verify(repository).save(captor.capture());
		AuditEvent event = captor.getValue();
		assertNotNull(event);
		assertEquals("alice", event.getUsername());
		assertEquals("SEND_MESSAGE", event.getAction());
		assertEquals("room-1", event.getResource());
		assertEquals("allow", event.getResult());
		assertEquals("rule-1", event.getRuleId());
	}

	@Test
	public void shouldRecordDenyAndCallAnalyticsAndRiskScoreOnDeny() {
		auditService.logEvent("bob", "SEND_MESSAGE", "room-2", "deny", "policy-deny", "Blocked");
		ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
		verify(repository).save(captor.capture());
		assertEquals("deny", captor.getValue().getResult());
		verify(analyticsAggregator).recordDeny("bob", "room-2");
		verify(userRiskScoreService).recordDenial("bob");
	}

	@Test
	public void shouldUseAnonymousWhenUsernameNull() {
		auditService.logEvent(null, "LOGIN", "app", "allow", null, null);
		ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
		verify(repository).save(captor.capture());
		assertEquals("anonymous", captor.getValue().getUsername());
	}
}
