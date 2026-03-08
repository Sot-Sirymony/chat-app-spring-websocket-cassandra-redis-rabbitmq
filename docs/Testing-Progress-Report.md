# Testing Progress Report

Quick reference for test status and how to run tests. Update the **Last run** and **Status** rows when you run the suite.

---

## Summary

| Suite              | Tests | Status   | Last run   | Notes                          |
|--------------------|-------|----------|------------|--------------------------------|
| **Unit**           | 66    | ✅ Pass  | 2026-03-08 | All phases                     |
| **Integration**    | 5 classes | ⏸ Skipped | —        | Requires Docker (Testcontainers) |

**Commands**

| Goal                    | Command                          |
|-------------------------|----------------------------------|
| Unit tests only         | `mvn test`                       |
| Unit + integration      | `mvn verify`                     |
| Verify without ITs       | `mvn verify -DskipITs=true`      |

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

## Changelog (tracking)

| Date       | Change |
|------------|--------|
| 2026-03-08 | Report added. Unit: 66 pass. Integration: skipped (Docker/Testcontainers). Added `-DskipITs` and Failsafe Docker socket config in pom. |

---

*Update the Summary table “Last run” and any Status cells when you run tests.*
