package br.com.jorgeacetozi.ebookChat.audit.domain.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.jorgeacetozi.ebookChat.analytics.AnalyticsAggregator;
import br.com.jorgeacetozi.ebookChat.audit.domain.model.AuditEvent;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.policy.UserRiskScoreService;
import br.com.jorgeacetozi.ebookChat.audit.domain.repository.AuditEventRepository;

/**
 * Append-only audit logging (BR-5.1).
 */
@Service
public class AuditService {

	private final AuditEventRepository repository;
	private final AnalyticsAggregator analyticsAggregator;
	private final UserRiskScoreService userRiskScoreService;

	@Autowired
	public AuditService(AuditEventRepository repository, AnalyticsAggregator analyticsAggregator,
			UserRiskScoreService userRiskScoreService) {
		this.repository = repository;
		this.analyticsAggregator = analyticsAggregator;
		this.userRiskScoreService = userRiskScoreService;
	}

	public void logEvent(String username, String action, String resource, String result, String ruleId, String details) {
		AuditEvent event = new AuditEvent();
		event.setUsername(username != null ? username : "anonymous");
		event.setTimestamp(new Date());
		event.setAction(action);
		event.setResource(resource);
		event.setResult(result);
		event.setRuleId(ruleId);
		event.setDetails(details);
		repository.save(event);
		if ("deny".equals(result)) {
			analyticsAggregator.recordDeny(username, resource);
			userRiskScoreService.recordDenial(username);
		}
	}

	public List<AuditEvent> findEvents(String user, Date from, Date to) {
		if (user != null && !user.isEmpty()) {
			List<AuditEvent> list = repository.findByUsername(user);
			return filterByDateRange(list, from, to);
		}
		// No user filter: would need a different table/query for "all events"; for now return empty or require user
		return java.util.Collections.emptyList();
	}

	/**
	 * Events for risky message analysis: SEND_MESSAGE with result=deny or DLP/risk-related ruleId.
	 * Use for dashboards and auditing blocked/warned message attempts.
	 */
	public List<AuditEvent> findMessageRiskEvents(String username, Date from, Date to) {
		List<AuditEvent> events = findEvents(username, from, to);
		return events.stream()
				.filter(e -> "SEND_MESSAGE".equals(e.getAction()))
				.filter(e -> "deny".equals(e.getResult()) || isDlpOrRiskRule(e.getRuleId()))
				.collect(Collectors.toList());
	}

	private static boolean isDlpOrRiskRule(String ruleId) {
		if (ruleId == null) return false;
		String r = ruleId.toLowerCase();
		return r.startsWith("dlp") || r.contains("risk");
	}

	private List<AuditEvent> filterByDateRange(List<AuditEvent> list, Date from, Date to) {
		return list.stream()
				.filter(e -> (from == null || !e.getTimestamp().before(from)) && (to == null || !e.getTimestamp().after(to)))
				.collect(Collectors.toList());
	}
}
