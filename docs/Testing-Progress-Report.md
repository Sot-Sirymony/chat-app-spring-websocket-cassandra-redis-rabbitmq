# Testing Progress Report

Quick reference for test status and how to run tests. Update the **Last run** and **Status** rows when you run the suite.

---

## Summary

| Suite              | Tests | Status   | Last run   | Notes                          |
|--------------------|-------|----------|------------|--------------------------------|
| **Unit**           | 66    | ✅ Pass  | 2026-03-08 | All phases                     |
| **Integration**    | 5 classes | ⏸ Skipped | —        | Requires Docker (Testcontainers) |
| **E2E (Playwright)** | 29  | ⏸ Backend required | —   | `cd e2e && npm test`; backend at :8080 |

**Commands**

| Goal                    | Command                          |
|-------------------------|----------------------------------|
| Unit tests only         | `mvn test`                       |
| Unit + integration      | `mvn verify`                     |
| Verify without ITs       | `mvn verify -DskipITs=true`      |
| E2E UI tests            | Start backend, then `cd e2e && npm test` |

---

## Login credentials (UI testing)

**Admin (seeded in `V1__init.sql`):**

| Field      | Value   |
|-----------|---------|
| Username  | `admin` |
| Password  | `admin` |

**Test user – non-admin (seeded in `V5__add_test_user.sql`):**

| Field      | Value       |
|-----------|-------------|
| Username  | `user`      |
| Password  | `password`  |

- **UI:** http://localhost:8080 — use the form or create a new account via “Or create an account”.
- **Admin** has `ROLE_ADMIN` (access to `/analytics`, create chat rooms). **Test user** has `ROLE_USER` only. New accounts get `ROLE_USER`.

---

## Unit tests (66 total)

Run: `mvn test` (runs `UnitTestsSuite`).

| Phase   | Test class                         | Tests | Status   |
|---------|------------------------------------|-------|----------|
| Pre     | InstantMessageBuilderTest          | —     | ✅       |
| Pre     | DestinationsTest                   | —     | ✅       |
| Pre     | SystemMessagesTest                 | —     | ✅       |
| Pre     | RedisChatRoomServiceTest           | —     | ✅       |
| **1**   | JwtTokenServiceTest                | 3     | ✅       |
| **1**   | AbacPolicyServiceTest              | 7     | ✅       |
| **1**   | AuditServiceTest                   | 3     | ✅       |
| **1**   | RoomClassificationAndChatRoomTest  | 3     | ✅       |
| **2**   | DlpEngineAndRuleBasedProviderTest  | 7     | ✅       |
| **2**   | DlpScanResultAndRiskLevelTest      | 3     | ✅       |
| **3**   | FileTransferRequestServiceTest     | 5     | ✅       |
| **4**   | ChatMetricsServiceTest             | 6     | ✅       |
| **4**   | UserRiskScoreServiceTest           | 3     | ✅       |
| **5**   | PresidioPropertiesTest             | 7     | ✅       |
| **5**   | DlpEnginePresidioMergeAndFallbackTest | 4  | ✅       |

Phase 1: JWT, ABAC, Audit (BR-1.1, BR-2.1, BR-2.2, BR-5.1)  
Phase 2: DLP (BR-4.1)  
Phase 3: File approval (BR-3.1)  
Phase 4: Monitoring, risk (BR-8.1, T2.1.9)  
Phase 5: Presidio DLP (BR-4.2)

Details: see [Test-Cases-By-Phase.md](Test-Cases-By-Phase.md).

---

## Integration tests (5 test classes)

Run: `mvn verify` (no `-DskipITs=true`). **Requires Docker** for Testcontainers (Cassandra, MySQL, Redis, RabbitMQ).

| Test class                    | Status   | Blocker / note                    |
|-------------------------------|----------|-----------------------------------|
| CassandraInstantMessageServiceTest | ⏸ Blocked | Testcontainers Docker connection |
| RedisChatRoomServiceTest      | ⏸ Blocked | Same                              |
| DefaultUserServiceTest        | ⏸ Blocked | Same                              |
| AuthenticationControllerTest  | ⏸ Blocked | Same                              |
| ChatRoomControllerTest        | ⏸ Blocked | Same                              |

**To run integration tests:** Ensure Docker Desktop is running and the daemon is reachable; then run `mvn verify` (or upgrade Testcontainers if still failing on Mac).

**To skip integration tests:** `mvn verify -DskipITs=true` — unit tests run, integration tests skipped, build passes.

---

## E2E UI tests (Playwright)

Run from repo root after starting the backend (and dependencies):

```bash
cd e2e && npm install && npx playwright install chromium && npm test
```

- **Requires:** Backend at http://localhost:8080 (Spring Boot + MySQL, Redis, Cassandra, RabbitMQ).
- **Tests:** Auth (login, logout, registration), navigation, chat list, create room, join room, chat room (WebSocket), approvals, analytics. See `e2e/README.md` and `docs/UI-Test-Cases.md`.

---

## Changelog (tracking)

| Date       | Change |
|------------|--------|
| 2026-03-08 | Report added. Unit: 66 pass. Integration: skipped (Docker/Testcontainers). Added `-DskipITs` and Failsafe Docker socket config in pom. |

---

*Update the Summary table “Last run” and any Status cells when you run tests.*
