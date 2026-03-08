package br.com.jorgeacetozi.ebookChat.analytics.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.jorgeacetozi.ebookChat.analytics.AnalyticsAggregator;
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

	@Autowired
	public AnalyticsController(AnalyticsAggregator analyticsAggregator,
			FileTransferRequestService fileTransferRequestService) {
		this.analyticsAggregator = analyticsAggregator;
		this.fileTransferRequestService = fileTransferRequestService;
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
