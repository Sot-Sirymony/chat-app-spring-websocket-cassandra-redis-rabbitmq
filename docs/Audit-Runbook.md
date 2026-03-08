# Audit trail — runbook (BR-5.1)

Reference for operating and maintaining the audit logging system.

---

## Retention and archival policy (T5.1.6)

### Current storage

- **Store:** Cassandra table `audit_events` (keyspace `ebook_chat`).
- **Schema:** `username`, `timestamp`, `action`, `resource`, `result`, `rule_id`, `details`.
- **Usage:** Append-only; no updates or deletes in normal operation.

### Retention

- **Default:** Keep all audit events indefinitely (no automatic purge).
- **Recommendation for production:**
  - Define a retention window (e.g. 13 months for compliance, or 90 days for operations).
  - Run a scheduled job (e.g. Cassandra TTL, or a batch that deletes/archives rows older than the window).
  - If using TTL: add a TTL to the table or to writes (e.g. `USING TTL 395 days` for ~13 months).

### Archival

- **Option A — Export to cold storage:** Periodically export `audit_events` (e.g. by `timestamp` range) to object storage (e.g. MinIO/S3) or a data lake as CSV/Parquet. Then delete or archive in Cassandra.
- **Option B — Separate keyspace/table:** Create an `audit_events_archive` table; move rows older than N days from `audit_events` into the archive table; query both when needed for long-range reports.
- **Option C — Log aggregation:** Ship audit logs to a central SIEM or log platform (e.g. ELK, Splunk) and retain there; optionally reduce retention in Cassandra.

### Operational checklist

- [ ] Decide retention window (e.g. 13 months).
- [ ] Implement TTL or scheduled delete/archive (e.g. weekly job).
- [ ] Document archival format and location if using export.
- [ ] Ensure `GET /api/audit` (admin) and any compliance reports account for archived data if queried elsewhere.

---

## Querying audit (GET /api/audit)

- **Endpoint:** `GET /api/audit?from=&to=&user=&action=`
- **Auth:** Admin only (`ROLE_ADMIN`).
- **Parameters:** Optional filters for time range (`from`, `to`), `user`, `action`.
- **Use:** Investigations, compliance reviews, and security analytics.

---

*Document version: 1 — optional task T5.1.6*
