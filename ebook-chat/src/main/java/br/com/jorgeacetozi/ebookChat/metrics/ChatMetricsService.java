package br.com.jorgeacetozi.ebookChat.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.stereotype.Service;

/**
 * BR-8.1 (T8.1.1): Expose metrics for blocked messages, approval latency, DLP rule hits.
 */
@Service
public class ChatMetricsService {

	private static final String COUNTER_POLICY_DENIED = "chat.messages.policy.denied";
	private static final String COUNTER_DLP_BLOCKED = "chat.messages.dlp.blocked";
	private static final String COUNTER_DLP_RULE_HITS = "dlp.rule.hits";
	private static final String COUNTER_APPROVALS = "file.approvals.total";
	private static final String GAUGE_APPROVAL_LATENCY_MS = "file.approval.latency.last_ms";
	// TP.8: Presidio
	private static final String COUNTER_PRESIDIO_CALLS = "presidio.calls.total";
	private static final String COUNTER_PRESIDIO_FAILURES = "presidio.calls.failures";
	private static final String GAUGE_PRESIDIO_LATENCY_MS = "presidio.latency.last_ms";

	private final CounterService counterService;
	private final GaugeService gaugeService;

	@Autowired
	public ChatMetricsService(CounterService counterService, GaugeService gaugeService) {
		this.counterService = counterService;
		this.gaugeService = gaugeService;
	}

	public void recordPolicyDenied() {
		counterService.increment(COUNTER_POLICY_DENIED);
	}

	public void recordDlpBlocked() {
		counterService.increment(COUNTER_DLP_BLOCKED);
	}

	public void recordDlpRuleHit(String ruleId) {
		if (ruleId != null && !ruleId.isEmpty()) {
			counterService.increment(COUNTER_DLP_RULE_HITS + "." + sanitize(ruleId));
		}
	}

	public void recordFileApproval(long latencyMs) {
		counterService.increment(COUNTER_APPROVALS);
		gaugeService.submit(GAUGE_APPROVAL_LATENCY_MS, latencyMs);
	}

	/** TP.8: Record a Presidio analyzer call (success or failure) and optional latency in ms. */
	public void recordPresidioCall(boolean success, long latencyMs) {
		counterService.increment(COUNTER_PRESIDIO_CALLS);
		if (!success) counterService.increment(COUNTER_PRESIDIO_FAILURES);
		gaugeService.submit(GAUGE_PRESIDIO_LATENCY_MS, latencyMs);
	}

	private static String sanitize(String s) {
		return s.replaceAll("[^a-zA-Z0-9_-]", "_");
	}
}
