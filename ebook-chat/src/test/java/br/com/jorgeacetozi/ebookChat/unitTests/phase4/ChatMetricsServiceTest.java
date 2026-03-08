package br.com.jorgeacetozi.ebookChat.unitTests.phase4;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;

import br.com.jorgeacetozi.ebookChat.metrics.ChatMetricsService;

/**
 * Phase 4 (BR-8.1) T8.1.1: Test metrics recording — policy denied, DLP blocked, approval latency, Presidio.
 */
@RunWith(MockitoJUnitRunner.class)
public class ChatMetricsServiceTest {

	@Mock
	private CounterService counterService;

	@Mock
	private GaugeService gaugeService;

	@InjectMocks
	private ChatMetricsService chatMetricsService;

	@Test
	public void shouldRecordPolicyDenied() {
		chatMetricsService.recordPolicyDenied();
		verify(counterService, times(1)).increment("chat.messages.policy.denied");
	}

	@Test
	public void shouldRecordDlpBlocked() {
		chatMetricsService.recordDlpBlocked();
		verify(counterService, times(1)).increment("chat.messages.dlp.blocked");
	}

	@Test
	public void shouldRecordDlpRuleHit() {
		chatMetricsService.recordDlpRuleHit("pii-ssn");
		verify(counterService, times(1)).increment("dlp.rule.hits.pii-ssn");
	}

	@Test
	public void shouldRecordFileApprovalAndLatency() {
		chatMetricsService.recordFileApproval(5000L);
		verify(counterService, times(1)).increment("file.approvals.total");
		verify(gaugeService, times(1)).submit("file.approval.latency.last_ms", 5000.0);
	}

	@Test
	public void shouldRecordPresidioCall() {
		chatMetricsService.recordPresidioCall(true, 100L);
		verify(counterService, times(1)).increment("presidio.calls.total");
		verify(gaugeService, times(1)).submit("presidio.latency.last_ms", 100.0);
	}

	@Test
	public void shouldRecordPresidioFailure() {
		chatMetricsService.recordPresidioCall(false, 5000L);
		verify(counterService, times(1)).increment("presidio.calls.total");
		verify(counterService, times(1)).increment("presidio.calls.failures");
	}
}
