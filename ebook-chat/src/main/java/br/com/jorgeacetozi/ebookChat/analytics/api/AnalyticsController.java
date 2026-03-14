package br.com.jorgeacetozi.ebookChat.analytics.api;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.jorgeacetozi.ebookChat.analytics.AnalyticsAggregator;
import br.com.jorgeacetozi.ebookChat.audit.domain.model.AuditEvent;
import br.com.jorgeacetozi.ebookChat.audit.domain.service.AuditService;
import br.com.jorgeacetozi.ebookChat.fileapproval.domain.service.FileTransferRequestService;

/**
 * T8.1.2: Dashboard/query for risky users and risky rooms.
 * T8.1.3: Alert-status endpoint for monitoring (approval backlog, etc.).
 */
@RestController
@RequestMapping("/api/analytics")
@Secured("ROLE_ADMIN")
public class AnalyticsController {

	private final AnalyticsAggregator analyticsAggregator;
	private final FileTransferRequestService fileTransferRequestService;
	private final AuditService auditService;

	@Autowired
	public AnalyticsController(AnalyticsAggregator analyticsAggregator,
			FileTransferRequestService fileTransferRequestService,
			AuditService auditService) {
		this.analyticsAggregator = analyticsAggregator;
		this.fileTransferRequestService = fileTransferRequestService;
		this.auditService = auditService;
	}

	@GetMapping("/risky-users")
	public List<AnalyticsAggregator.RiskyUserEntry> riskyUsers(
			@RequestParam(value = "limit", defaultValue = "20") int limit) {
		return analyticsAggregator.getRiskyUsers(limit);
	}

	@GetMapping("/risky-rooms")
	public List<AnalyticsAggregator.RiskyRoomEntry> riskyRooms(
			@RequestParam(value = "limit", defaultValue = "20") int limit) {
		return analyticsAggregator.getRiskyRooms(limit);
	}

	/**
	 * List chat message events for risky analysis: SEND_MESSAGE deny and DLP/risk-related audit events.
	 * Optional username to scope to one user; optional from/to for date range (ISO format).
	 */
	@GetMapping("/risky-message-events")
	public List<AuditEvent> riskyMessageEvents(
			@RequestParam(value = "username", required = false) String username,
			@RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
			@RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to) {
		return auditService.findMessageRiskEvents(username, from, to);
	}

	/** T8.1.3: Poll this endpoint to alert on approval backlog or other thresholds. */
	@GetMapping("/alert-status")
	public Map<String, Object> alertStatus() {
		Map<String, Object> out = new HashMap<>();
		int pending = fileTransferRequestService.findPending().size();
		out.put("approvalBacklog", pending);
		out.put("approvalBacklogAlert", pending > 20);
		return out;
	}
}
