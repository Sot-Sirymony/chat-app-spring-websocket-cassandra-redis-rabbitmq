# Business Requirements v1

**Internal Real-Time Chat System — Data Leakage Prevention**

*Aligned with research paper: ICS Paper Assessment II (Internal Real-Time Chat System to Prevent Data Leakage)*

---

## 1. Business Requirements Overview

The following requirements support a secure, governed internal chat platform that reduces data-leakage risks while maintaining speed, usability, and operational efficiency. They are mapped to the current project stack (Spring Boot, WebSocket/STOMP, Cassandra, Redis, RabbitMQ).

---

## 2. Authentication & Identity

| ID | Requirement | Description |
|----|-------------|-------------|
| BR-1.1 | **Hybrid auth (JWT + existing login)** | Keep current form login for web UI; add JWT for API/WebSocket clients. Validate token during STOMP `CONNECT` and bind claims into session context. |

---

## 3. Authorization & Access Control

| ID | Requirement | Description |
|----|-------------|-------------|
| BR-2.1 | **RBAC + ABAC policy layer** | Retain current role checks (e.g. admin-only room creation). Add ABAC rules (department, room sensitivity, device trust, time, location, risk score) before allowing `send.message` or file-share events. |
| BR-2.2 | **Policy-driven room governance** | Introduce room classification levels (e.g. `PUBLIC`, `INTERNAL`, `CONFIDENTIAL`, `RESTRICTED`) and enforce different send/upload/download permissions per level. |

---

## 4. Workflow & File Sharing

| ID | Requirement | Description |
|----|-------------|-------------|
| BR-3.1 | **Workflow-based file approval** | Introduce `file_transfer_request` with states (e.g. `PENDING`, `APPROVED`, `REJECTED`). Route sensitive files through supervisor approval before delivery. |
| BR-3.2 | **Human-error prevention UX** | Add pre-send warnings for risky recipients/files, confirmation dialogs, and “wrong recipient” detection to reduce accidental leakage. |
| BR-3.3 | **File storage with MinIO** | Use MinIO (open-source, S3-compatible object storage) for storing uploaded files. Persist file metadata; link file objects to `FileTransferRequest`; support secure upload, DLP scan on content, and download after approval. |

---

## 5. Data Loss Prevention (DLP)

| ID | Requirement | Description |
|----|-------------|-------------|
| BR-4.1 | **DLP engine for messages/files** | Scan content (keywords, regex for PII, document classification); assign risk level; then auto-allow, warn, block, or require approval. |
| BR-4.2 | **Optional: AI/NLP-based PII (Presidio)** | Optionally integrate Microsoft Presidio (open-source) as a second DLP provider: call Presidio Analyzer REST API for PII/NER detection; map entity types to risk levels; combine with existing rule-based engine or use as fallback. |

---

## 6. Audit & Compliance

| ID | Requirement | Description |
|----|-------------|-------------|
| BR-5.1 | **Audit trail and compliance logs** | Record immutable audit entries for every access decision and file action (who, what, when, rule matched, decision, approver). Support querying for investigations and compliance. |

---

## 7. Infrastructure & Scalability

| ID | Requirement | Description |
|----|-------------|-------------|
| BR-6.1 | **Scalable monolith deployment** | Run multiple Spring app instances behind a load balancer; use Redis for shared session/state and RabbitMQ for cross-node message distribution. |

---

## 8. Security Hardening

| ID | Requirement | Description |
|----|-------------|-------------|
| BR-7.1 | **Encryption hardening** | Enforce TLS everywhere; encrypt storage for sensitive file metadata/content; optional room-level key encryption for highly sensitive channels. |

---

## 9. Monitoring & Operations

| ID | Requirement | Description |
|----|-------------|-------------|
| BR-8.1 | **Monitoring and security analytics** | Track blocked attempts, approval latency, risky users/rooms, and policy hit rates for continuous tuning and incident response. |

---

## 10. Suggested Implementation Phasing

| Phase | Focus |
|-------|--------|
| **Phase 1** | JWT for WebSocket; ABAC checks on chat send; audit logging. |
| **Phase 2** | DLP scanner; risk scoring; soft/hard policy actions. |
| **Phase 3** | Full approval workflow for sensitive file sharing (single-step and multi-step); file management with MinIO. |
| **Phase 4** | Advanced governance (device trust, department constraints, anomaly/risk behavior rules). |

---

## 11. References

- Research paper: *Internal Real-Time Chat System to Prevent Data Leakage* (ICS Paper Assessment II).
- Project: ebook-chat-app-spring-websocket-cassandra-redis-rabbitmq.

---

*Document version: 1*  
*Last updated: 2025-03-08*
