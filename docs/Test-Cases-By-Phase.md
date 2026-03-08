# Test cases by phase

Unit tests covering implementation tasks across all phases. Run with: `mvn test -Dtest=UnitTestsSuite`.

**Note:** The suite uses Mockito and JUnit 4. For Java 17+, you may need to run with Java 8 (project default) or add JVM args for module access (e.g. `--add-opens java.base/java.lang=ALL-UNNAMED`) if using a newer JDK.

---

## Phase 1 — JWT, ABAC, Audit (BR-1.1, BR-2.1, BR-2.2, BR-5.1)

| Test class | Test method | Covers |
|------------|-------------|--------|
| **JwtTokenServiceTest** | shouldGenerateTokenWithUsernameAndRoles | T1.1.3 — token includes username, roles |
| | shouldParseTokenAndReturnClaimsWithSubjectAndRoles | T1.1.3 — parse and read claims |
| | shouldRejectInvalidToken | Token validation |
| **AbacPolicyServiceTest** | shouldDenyWhenUsernameIsNull | T2.1.3 — deny when not authenticated |
| | shouldDenyWhenUserHasNoRole | T2.1.3 — require role |
| | shouldAllowWhenUserHasRoleAndPublicRoom | T2.1.4 — allow PUBLIC room |
| | shouldDenyRestrictedRoomWhenDepartmentMissing | T2.2.3 — RESTRICTED requires department |
| | shouldDenyRestrictedRoomWhenDeviceNotTrusted | T2.1.8 — trusted device for RESTRICTED |
| | shouldAllowRestrictedRoomWhenDepartmentAndTrustedDevice | T2.2.4 — allow with department + device |
| | shouldDenyJoinWhenRiskScoreAboveThreshold | T2.1.9 — risk score threshold |
| **AuditServiceTest** | shouldSaveAuditEventOnLogEvent | T5.1.3 — logEvent saves and repository called |
| | shouldRecordDenyAndCallAnalyticsAndRiskScoreOnDeny | T5.1.4 — deny path: analytics + risk |
| | shouldUseAnonymousWhenUsernameNull | Anonymous username handling |
| **RoomClassificationAndChatRoomTest** | shouldHaveAllClassificationLevels | T2.2.1 — enum values |
| | shouldAllowSettingClassificationOnChatRoom | T2.2.1 — classification on ChatRoom |
| | shouldAllowSettingAllowedDepartments | T2.2.2 — allowed departments |

---

## Phase 2 — DLP (BR-4.1)

| Test class | Test method | Covers |
|------------|-------------|--------|
| **DlpEngineAndRuleBasedProviderTest** | shouldAllowWhenNoRuleMatches | T4.1.2 — ALLOW when no match |
| | shouldDetectKeywordAndReturnWarn | T4.1.2, T4.1.3 — keyword rule, WARN |
| | shouldDetectSsnRegexAndReturnRequireApproval | T4.1.2 — regex, REQUIRE_APPROVAL |
| | shouldAllowNullOrEmptyText | Edge cases |
| | shouldScanFilePlainTextContent | T4.1.4 — file scan text/plain |
| | shouldAllowNonPlainTextFileContent | Non–text/plain returns ALLOW |
| | shouldAllowEmptyOrNullFileContent | Edge cases |
| **DlpScanResultAndRiskLevelTest** | shouldProvideAllowResult | T4.1.1 — allow() |
| | shouldConstructResultWithRiskAndRuleIds | Risk + rule IDs |
| | shouldHaveAllRiskLevelsInEnum | T4.1.1 — LOW, MEDIUM, HIGH, CRITICAL |

---

## Phase 3 — File approval (BR-3.1)

| Test class | Test method | Covers |
|------------|-------------|--------|
| **FileTransferRequestServiceTest** | shouldCreatePendingRequestWithRequesterRecipientRoomAndFileRef | T3.1.1, T3.1.3 — create PENDING |
| | shouldFindPendingRequests | T3.1.4 — list pending |
| | shouldApprovePendingRequestAndUpdateStatusAndAudit | T3.1.4, T3.1.5 — approve + audit + metrics |
| | shouldNotApproveWhenRequestNotFound | Idempotent when missing |
| | shouldRejectPendingRequestAndAudit | T3.1.6 — reject + audit |

---

## Phase 4 — Monitoring, risk (BR-8.1, T2.1.9)

| Test class | Test method | Covers |
|------------|-------------|--------|
| **ChatMetricsServiceTest** | shouldRecordPolicyDenied | T8.1.1 — policy denied counter |
| | shouldRecordDlpBlocked | T8.1.1 — DLP blocked |
| | shouldRecordDlpRuleHit | T8.1.1 — rule hits |
| | shouldRecordFileApprovalAndLatency | T8.1.1 — approval + latency |
| | shouldRecordPresidioCall | TP.8 — Presidio success |
| | shouldRecordPresidioFailure | TP.8 — Presidio failure counter |
| **UserRiskScoreServiceTest** | shouldReturnZeroScoreForNewUser | T2.1.9 — initial score |
| | shouldIncreaseScoreAfterDenials | T2.1.9 — recordDenial increases score |
| | shouldCapScoreAt100 | T2.1.9 — cap at 100 |

---

## Phase 5 — Presidio DLP (BR-4.2)

| Test class | Test method | Covers |
|------------|-------------|--------|
| **PresidioPropertiesTest** | shouldDefaultDisabled | TP.1 — enabled false by default |
| | shouldHaveDefaultAnalyzerUrl | TP.1 — default URL |
| | shouldMapUsSsnToHighByDefault | TP.3 — entity→risk mapping |
| | shouldMapCreditCardToCriticalByDefault | TP.3 |
| | shouldMapPersonToMediumByDefault | TP.3 |
| | shouldUseCustomMappingWhenSet | TP.3 — configurable mapping |
| | shouldReturnLowForUnknownEntity | Unknown entity → LOW |
| **DlpEnginePresidioMergeAndFallbackTest** | whenPresidioDisabled_shouldUseOnlyRuleBasedResult | TP.5 — Presidio off |
| | whenPresidioEnabledButProviderNull_shouldUseOnlyRuleBasedResult | TP.5 — fallback |
| | whenPresidioProviderReturnsAllow_mergeShouldKeepRuleBasedResult | TP.5 — merge keeps rule result |
| | whenBothMatch_mergeShouldTakeMaxRiskAndCombineRuleIds | TP.5 — merge max risk + rule IDs |

---

## Existing tests (pre-phase)

- **InstantMessageBuilderTest** — chat message builder
- **DestinationsTest** — WebSocket destinations
- **SystemMessagesTest** — system messages
- **RedisChatRoomServiceTest** — join, leave, send public/private, save/find room

---

*Document version: 1*
