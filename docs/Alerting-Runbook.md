# Alerting runbook (T8.1.3 — BR-8.1)

How to alert on security and workflow thresholds.

---

## Metrics available

The application exposes actuator metrics (admin-only at `/metrics`). Use these for alerting:

- **chat.messages.policy.denied** — total policy denials since startup
- **chat.messages.dlp.blocked** — total DLP blocks since startup
- **dlp.rule.hits.\*** — per-rule DLP hit count
- **file.approvals.total** — total file approvals
- **file.approval.latency.last_ms** — last approval latency (ms)

Risky users/rooms (deny counts) are available via **GET /api/analytics/risky-users** and **/api/analytics/risky-rooms** (admin).

---

## Recommended alerts

1. **Spike in blocked attempts:** Configure your monitoring (e.g. Prometheus) to scrape `/metrics` and alert when the *rate* of `chat.messages.policy.denied` or `chat.messages.dlp.blocked` over the last 5–15 minutes exceeds a threshold (e.g. 10/min).
2. **Approval backlog:** Poll **GET /api/file-requests/pending** (as admin) and alert when `pending count > N` (e.g. 20).
3. **High approval latency:** Alert when `file.approval.latency.last_ms` exceeds a threshold (e.g. 24 hours = 86400000 ms).

---

## Example: Prometheus

Scrape the app (with admin credentials or a dedicated metrics endpoint):

```yaml
scrape_configs:
  - job_name: 'ebook-chat'
    metrics_path: /metrics
    static_configs:
      - targets: ['localhost:8080']
    basic_auth:
      username: admin
      password: <from-secret>
```

Alert rules (example):

```yaml
groups:
  - name: ebook-chat
    rules:
      - alert: HighPolicyDenialRate
        expr: rate(chat_messages_policy_denied_total[5m]) > 0.5
        for: 2m
        annotations:
          summary: "High policy denial rate"
      - alert: ApprovalBacklog
        expr: ebook_chat_pending_approvals > 20
        annotations:
          summary: "File approval backlog high"
```

*(Note: metric names may be normalized with underscores; adjust to your actuator output.)*

---

## Optional: alert-status endpoint

**GET /api/analytics/alert-status** (admin) returns current counters and pending approval count so a simple script or monitor can poll and alert when thresholds are exceeded. See `AnalyticsController` if implemented.

---

*Document version: 1*
