# Implementation Tasks v1

**Internal Real-Time Chat System — Data Leakage Prevention**

*Detailed tasks derived from [Business-Requirements-v1.md](Business-Requirements-v1.md)*

---

## Implementation tracking

**Status legend:** `⏳ Not Started` | `🔄 In Progress` | `✅ Done` | `🚫 Blocked`

| Phase   | Not Started | In Progress | Done | Blocked | Total |
|---------|-------------|-------------|------|---------|-------|
| Phase 1 | 0           | 0           | 24   | 0       | 24    |
| Phase 2 | 0           | 0           | 6    | 0       | 6     |
| Phase 3 | 2           | 0           | 10   | 0       | 12    |
| MinIO   | 0           | 0           | 8    | 0       | 8     |
| Phase 4 | 0           | 0           | 10   | 0       | 10    |
| Presidio| 0           | 0           | 8    | 0       | 8     |
| **All**  | **2**       | **0**       | **66**| **0**   | **68**|

**How to track:** When you start a task, change its `Status` to `🔄 In Progress`; when finished, set to `✅ Done`. If blocked by another task or dependency, set to `🚫 Blocked`. Update the progress table above (Not Started / In Progress / Done / Blocked counts) when you change statuses.

---

## Phase 1 — JWT, ABAC, Audit Logging

### BR-1.1 — Hybrid auth (JWT + existing login)

| Task ID | Status | Task | Type | Acceptance |
|---------|--------|------|------|------------|
| T1.1.1 | ✅ Done | Add JWT dependency (e.g. `jjwt-api`, `jjwt-impl`, `jjwt-jackson`) to `pom.xml`. | Backend | Build succeeds. |
| T1.1.2 | ✅ Done | Create `JwtProperties` (secret, issuer, expiration) and bind from `application.yml`. | Backend | Config loads. |
| T1.1.3 | ✅ Done | Create `JwtTokenService`: generate JWT after successful form login; include username, roles, optional claims. | Backend | Token returned on login. |
| T1.1.4 | ✅ Done | Add REST endpoint (e.g. `POST /api/auth/token`) that accepts credentials and returns JWT for API/WebSocket clients. | Backend | Clients can obtain token. |
| T1.1.5 | ✅ Done | Create `JwtChannelInterceptor` (or handshake handler) to validate JWT from STOMP `CONNECT` header (e.g. `Authorization: Bearer <token>`); set `Principal` and session attributes from claims. | Backend | WebSocket connects with JWT only. |
| T1.1.6 | ✅ Done | Register interceptor in WebSocket config so all STOMP endpoints validate JWT when present; fall back to session when no JWT (browser form login). | Backend | Both JWT and session work. |
| T1.1.7 | ✅ Done | Update frontend: optional JWT storage (e.g. localStorage) and send in STOMP connect headers when using token-based auth. | Frontend | WebSocket uses JWT from client. |

---

### BR-2.1 — RBAC + ABAC policy layer

| Task ID | Status | Task | Type | Acceptance |
|---------|--------|------|------|------------|
| T2.1.1 | ✅ Done | Define ABAC attribute model: user (username, roles, department), resource (roomId, roomLevel, messageType), environment (timestamp, client IP, device type if available). | Backend | Model classes/interfaces exist. |
| T2.1.2 | ✅ Done | Add `department` (or similar) to `User` entity and DB migration; seed/admin UI if needed. | Backend | User has department. |
| T2.1.3 | ✅ Done | Create `PolicyEngine` / `AbacPolicyService`: evaluate policies (e.g. “allow send if user.department == room.allowedDepartments”). | Backend | Service returns allow/deny + reason. |
| T2.1.4 | ✅ Done | Define initial policy rules in config or DB (e.g. room level, department, role combinations). | Backend | At least one rule enforced. |
| T2.1.5 | ✅ Done | Before processing `send.message`: load user, room, context; call `PolicyEngine`; if deny, return error to client and do not persist/broadcast. | Backend | Denied messages never sent. |
| T2.1.6 | ✅ Done | Expose policy denial reason to client (e.g. STOMP error or custom payload) for UX. | Backend/Frontend | User sees why message was blocked. |

---

### BR-2.2 — Policy-driven room governance

| Task ID | Status | Task | Type | Acceptance |
|---------|--------|------|------|------------|
| T2.2.1 | ✅ Done | Add `classification` (enum: `PUBLIC`, `INTERNAL`, `CONFIDENTIAL`, `RESTRICTED`) to `ChatRoom` entity and Redis schema. | Backend | Room has classification. |
| T2.2.2 | ✅ Done | Add `allowedRoles` / `allowedDepartments` (or equivalent) to `ChatRoom` for restricted rooms. | Backend | Room stores access rules. |
| T2.2.3 | ✅ Done | On room join: enforce classification vs. user role/department; reject join if not allowed. | Backend | Unauthorized users cannot join. |
| T2.2.4 | ✅ Done | On send message: enforce send permission by room classification (e.g. RESTRICTED may need extra check). | Backend | Send rules applied per room. |
| T2.2.5 | ✅ Done | Admin UI: set classification and allowed roles/departments when creating/editing room. | Frontend | Admins can configure room policy. |

---

### BR-5.1 — Audit trail and compliance logs

| Task ID | Status | Task | Type | Acceptance |
|---------|--------|------|------|------------|
| T5.1.1 | ✅ Done | Design audit schema: who, action, resource (room/message/file), timestamp, result (allow/deny), rule/policy id, optional approver. | Backend | Schema documented. |
| T5.1.2 | ✅ Done | Create Cassandra (or chosen store) table/entity for audit events; ensure append-only / immutable usage. | Backend | Table exists. |
| T5.1.3 | ✅ Done | Create `AuditService.logEvent(...)` and call it from: login, message send (allow/deny), room join, file request (later). | Backend | Events written. |
| T5.1.4 | ✅ Done | Add audit logging to `PolicyEngine` decision (deny + reason). | Backend | Every policy decision logged. |
| T5.1.5 | ✅ Done | Provide read-only API (e.g. `GET /api/audit?from=&to=&user=&action=`) for admins; secure with role (e.g. ADMIN). | Backend | Auditors can query. |
| T5.1.6 | ✅ Done | Optional: retention/archival policy and doc in runbook. | Ops | Documented. |

---

## Phase 2 — DLP, risk scoring, policy actions

### BR-4.1 — DLP engine for messages/files

| Task ID | Status | Task | Type | Acceptance |
|---------|--------|------|------|------------|
| T4.1.1 | ✅ Done | Define risk levels (e.g. LOW, MEDIUM, HIGH, CRITICAL) and actions (ALLOW, WARN, BLOCK, REQUIRE_APPROVAL). | Backend | Enum/config. |
| T4.1.2 | ✅ Done | Create `DlpEngine`: scan text (keywords, regex for PII); return risk level and matched rule ids. | Backend | Text scan returns level. |
| T4.1.3 | ✅ Done | Add configurable rules (keyword lists, regex patterns) in DB or config; load at startup or on demand. | Backend | Rules configurable. |
| T4.1.4 | ✅ Done | For file uploads: extract text (or metadata) and run through `DlpEngine`; support at least one format (e.g. PDF/plain). | Backend | File content scanned. |
| T4.1.5 | ✅ Done | Integrate DLP into message send flow: call `DlpEngine`; if BLOCK, deny and audit; if WARN, add flag to payload and optionally require confirmation; if REQUIRE_APPROVAL, create approval request (Phase 3). | Backend | Send flow respects DLP. |
| T4.1.6 | ✅ Done | Frontend: show warning/block message when DLP blocks or warns. | Frontend | User sees DLP result. |

---

## Phase 3 — File approval workflow

### BR-3.1 — Workflow-based file approval

| Task ID | Status | Task | Type | Acceptance |
|---------|--------|------|------|------------|
| T3.1.1 | ✅ Done | Create `FileTransferRequest` entity: id, requester, recipient(s), file ref, roomId, status (PENDING, APPROVED, REJECTED), requestedAt, decidedAt, approver. | Backend | Entity and table exist. |
| T3.1.2 | ✅ Done | Persist file metadata (and optionally content) in secure store; link to `FileTransferRequest`. | Backend | Files stored and referenced. |
| T3.1.3 | ✅ Done | When DLP or room policy requires approval: create `FileTransferRequest` (PENDING); notify supervisor(s) via WebSocket or REST. | Backend | Request created and notified. |
| T3.1.4 | ✅ Done | Implement approve/reject endpoints (e.g. `POST /api/file-requests/{id}/approve`, `.../reject`) with role/supervisor check. | Backend | Approver can decide. |
| T3.1.5 | ✅ Done | On approve: mark request APPROVED; deliver file to recipient(s) (e.g. push message with download link); audit. | Backend | File delivered after approval. |
| T3.1.6 | ✅ Done | On reject: mark REJECTED; notify requester; audit. | Backend | Requester informed. |
| T3.1.7 | ⏳ Not Started | Optional: multi-step approval (e.g. two approvers); extend status and workflow. | Backend | Configurable steps. |
| T3.1.8 | ✅ Done | Frontend: UI for approver to list pending requests and approve/reject; requester sees status. | Frontend | Workflow usable in UI. |

---

### BR-3.2 — Human-error prevention UX

| Task ID | Status | Task | Type | Acceptance |
|---------|--------|------|------|------------|
| T3.2.1 | ✅ Done | Before send: if recipient is external or high-risk room, show confirmation dialog (“Send to X? This room is CONFIDENTIAL.”). | Frontend | Confirmation shown. |
| T3.2.2 | ✅ Done | Implement “wrong recipient” check: e.g. compare recipient to recent conversations or department; show warning if unusual. | Backend/Frontend | Warning when risky recipient. |
| T3.2.3 | ✅ Done | For file attach: show file name and size; if DLP risk known, show “Sensitive content – approval may be required.” | Frontend | User informed before send. |
| T3.2.4 | ✅ Done | Optional: “undo send” within N seconds for non-delivered or non-approved messages. | Frontend | UX improvement. |

---

## BR-3.3 — MinIO file storage

| Task ID | Status | Task | Type | Acceptance |
|---------|--------|------|------|------------|
| TM.1 | ✅ Done | Add MinIO dependency and config (endpoint, bucket, credentials in application.yml and MinioProperties). | Backend | Build and config load. |
| TM.2 | ✅ Done | Ensure bucket exists on startup; create MinioFileService (upload bytes, get by id); persist file metadata (FileMetadata entity). | Backend | Files stored in MinIO and metadata in DB. |
| TM.3 | ✅ Done | Add POST /api/files/upload (multipart); return file id and optional DLP flags. | Backend | Client can upload and get fileId. |
| TM.4 | ✅ Done | Link FileTransferRequest.fileRef to MinIO object id; link FileMetadata.requestId when creating approval request. | Backend | Approval flow uses MinIO file. |
| TM.5 | ✅ Done | Secure GET /api/files/{id}/download: auth + allow uploader or (approved request) requester/recipient. | Backend | Only authorized users download. |
| TM.6 | ✅ Done | Run DLP on upload (scanFile); BLOCK -> 403; WARN/REQUIRE_APPROVAL -> 200 with flags. | Backend | Upload respects DLP. |
| TM.7 | ✅ Done | Frontend: file attach button, upload to /api/files/upload, include fileRef in send; render download link in messages. | Frontend | Users can attach and download. |
| TM.8 | ✅ Done | Optional: Docker run example and MinIO config/docs (docs/MinIO.md). | Ops/Docs | Documented. |

---

## Phase 4 — Advanced governance & hardening

### BR-6.1 — Scalable monolith deployment

| Task ID | Status | Task | Type | Acceptance |
|---------|--------|------|------|------------|
| T6.1.1 | ✅ Done | Document and verify Redis session store works across instances (already in project). | Ops/Docs | Sessions shared. |
| T6.1.2 | ✅ Done | Verify RabbitMQ broker relay for STOMP distributes messages across app instances. | Backend/Ops | Multi-node messaging works. |
| T6.1.3 | ✅ Done | Add/update Docker Compose or K8s manifest for 2+ app replicas behind load balancer. | Ops | Easy to run multiple instances. |
| T6.1.4 | ✅ Done | Optional: sticky session or connection affinity notes for WebSocket if required. | Ops | Documented. |

---

### BR-7.1 — Encryption hardening

| Task ID | Status | Task | Type | Acceptance |
|---------|--------|------|------|------------|
| T7.1.1 | ✅ Done | Enforce TLS for all endpoints (server.ssl or reverse proxy); redirect HTTP to HTTPS. | Backend/Ops | No plain HTTP in production. |
| T7.1.2 | ✅ Done | Encrypt sensitive file content at rest (e.g. AES); store key in secure config/secret manager. | Backend | Files encrypted at rest. |
| T7.1.3 | ✅ Done | Optional: room-level or E2E key derivation for highly sensitive channels; document design. | Backend | Design doc or spike. |

---

### BR-8.1 — Monitoring and security analytics

| Task ID | Status | Task | Type | Acceptance |
|---------|--------|------|------|------------|
| T8.1.1 | ✅ Done | Expose metrics: blocked message count, approval latency histogram, DLP rule hit count per rule. | Backend | Metrics available (e.g. Micrometer). |
| T8.1.2 | ✅ Done | Dashboard or query: risky users (high deny rate), risky rooms (high DLP blocks). | Ops/Frontend | Auditors can review. |
| T8.1.3 | ✅ Done | Alert on threshold: e.g. spike in blocked attempts, approval backlog. | Ops | Alerts configured. |

---

### BR-2.1 (extended) — Device / anomaly rules

| Task ID | Status | Task | Type | Acceptance |
|---------|--------|------|------|------------|
| T2.1.7 | ✅ Done | Capture device type or client identifier in WebSocket handshake; store in ABAC context. | Backend | Device in context. |
| T2.1.8 | ✅ Done | Add policy: e.g. “RESTRICTED room only from trusted device list or corporate network.” | Backend | Policy enforced. |
| T2.1.9 | ✅ Done | Optional: simple risk score per user (e.g. recent denials, failed logins); use in ABAC. | Backend | Risk score influences decision. |

---

## Phase 5 — DLP + Microsoft Presidio (BR-4.2)

*Optional: integrate open-source Presidio Analyzer for AI/NLP-based PII detection alongside existing rule-based DLP.*

### BR-4.2 — Optional: AI/NLP-based PII (Presidio)

| Task ID | Status | Task | Type | Acceptance |
|---------|--------|------|------|------------|
| TP.1 | ✅ Done | Add `PresidioProperties`: analyzer base URL, enabled flag, timeout, optional entity filter. Bind from `application.yml` (e.g. `ebook.chat.presidio.*`). | Backend | Config loads; app starts with Presidio disabled by default. |
| TP.2 | ✅ Done | Create `PresidioAnalyzerClient`: HTTP client that POSTs to Presidio `/analyze` with `{"text","language"}`; parse response (entity_type, score, start, end). | Backend | Unit test or manual call returns detected entities. |
| TP.3 | ✅ Done | Define mapping from Presidio entity types to `DlpRiskLevel` (e.g. US_SSN, CREDIT_CARD → HIGH/CRITICAL; PERSON, EMAIL → MEDIUM; configurable in config). | Backend | Mapping documented and configurable. |
| TP.4 | ✅ Done | Introduce `DlpProvider` interface (e.g. `DlpScanResult scan(String text)`) and make existing `DlpEngine` implement it; add `PresidioDlpProvider` that calls `PresidioAnalyzerClient` and maps results to `DlpScanResult`. | Backend | Both rule-based and Presidio implement same interface. |
| TP.5 | ✅ Done | In `DlpEngine` (or a facade): when Presidio is enabled, call Presidio provider and merge with rule-based result (e.g. take max risk, combine matched rule IDs); on Presidio timeout/unavailable, fall back to rule-based only and log. | Backend | Message/file scan uses Presidio when enabled; fallback works. |
| TP.6 | ✅ Done | Run Presidio Analyzer via Docker in dev; add `docker-compose/presidio.yml` or extend existing compose with `presidio-analyzer` service (port 5002). Document in `docs/Presidio-DLP.md`. | Ops/Docs | One command starts Presidio; doc explains enable/disable and entity mapping. |
| TP.7 | ✅ Done | For file upload DLP: when Presidio enabled, send extracted text (e.g. plain text from upload) to Presidio `/analyze` and merge with rule-based file scan result. | Backend | File content benefits from Presidio when enabled. |
| TP.8 | ✅ Done | Optional: expose metrics for Presidio (e.g. presidio.calls.total, presidio.calls.failures, presidio.latency) and document in runbook. | Backend/Ops | Metrics available when Presidio enabled. |

---

## Task summary by phase

| Phase | Task IDs | Focus |
|-------|----------|--------|
| Phase 1 | T1.1.x, T2.1.1–T2.1.6, T2.2.1–T2.2.5, T5.1.1–T5.1.6 | JWT, ABAC, room governance, audit |
| Phase 2 | T4.1.1–T4.1.6 | DLP engine, risk levels, policy actions |
| Phase 3 | T3.1.1–T3.1.8, T3.2.1–T3.2.4 | File approval workflow, UX safeguards |
| MinIO | TM.1–TM.8 | File storage with MinIO for transfer/approval |
| Phase 4 | T6.1.1–T6.1.4, T7.1.1–T7.1.3, T8.1.1–T8.1.3, T2.1.7–T2.1.9 | Scalability, encryption, monitoring, device/anomaly |
| Presidio | TP.1–TP.8 | DLP integration with Microsoft Presidio (AI/NLP PII) |

---

## Requirement → task mapping

| BR ID | Task IDs |
|-------|----------|
| BR-1.1 | T1.1.1 – T1.1.7 |
| BR-2.1 | T2.1.1 – T2.1.9 |
| BR-2.2 | T2.2.1 – T2.2.5 |
| BR-3.1 | T3.1.1 – T3.1.8 |
| BR-3.2 | T3.2.1 – T3.2.4 |
| BR-3.3 | TM.1 – TM.8 (MinIO file storage) |
| BR-4.1 | T4.1.1 – T4.1.6 |
| BR-5.1 | T5.1.1 – T5.1.6 |
| BR-6.1 | T6.1.1 – T6.1.4 |
| BR-7.1 | T7.1.1 – T7.1.3 |
| BR-8.1 | T8.1.1 – T8.1.3 |
| BR-4.2 | TP.1 – TP.8 (Presidio DLP) |

---

*Document version: 1*  
*Last updated: 2025-03-08*
