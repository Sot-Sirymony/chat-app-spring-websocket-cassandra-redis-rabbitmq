# Presidio DLP integration (BR-4.2, Phase 5)

Optional AI/NLP-based PII detection using [Microsoft Presidio](https://microsoft.github.io/presidio/) alongside the rule-based DLP engine.

---

## Enable/disable

- **Default:** Presidio is **disabled** (`ebook.chat.presidio.enabled: false`).
- **Enable:** Set in `application.yml` or environment:

  ```yaml
  ebook:
    chat:
      presidio:
        enabled: true
        analyzer-base-url: http://localhost:5002
        timeout-ms: 5000
        language: en
        score-threshold: 0.5
  ```

  Or env: `PRESIDIO_ANALYZER_URL=http://localhost:5002` and set `ebook.chat.presidio.enabled=true`.

- When disabled, only the existing keyword/regex DLP runs. When enabled, the app calls Presidio `/analyze` and **merges** results with rule-based (highest risk wins; matched rule IDs combined). If Presidio is unreachable or times out, the app **falls back** to rule-based only and logs a warning.

---

## Running Presidio Analyzer

### Docker (TP.6)

From the project root:

```bash
docker-compose -f docker-compose/presidio.yml up -d
```

This starts the Presidio Analyzer on **port 5002**. The app connects to `http://localhost:5002` by default (override with `PRESIDIO_ANALYZER_URL` if Presidio runs elsewhere).

### Verify

```bash
curl -s -X POST http://localhost:5002/analyze \
  -H "Content-Type: application/json" \
  -d '{"text":"My SSN is 123-45-6789","language":"en"}' | jq .
```

You should see a JSON array of detected entities (e.g. `entity_type: "US_SSN"`, `score`, `start`, `end`).

---

## Entity → risk mapping (TP.3)

Presidio returns entity types (e.g. `US_SSN`, `CREDIT_CARD`, `PERSON`). The app maps them to `DlpRiskLevel` and then to actions:

| Risk level | Action            |
|-----------|-------------------|
| CRITICAL  | BLOCK             |
| HIGH      | REQUIRE_APPROVAL  |
| MEDIUM    | WARN              |
| LOW       | ALLOW             |

**Default mapping** (in `PresidioProperties`):

- **CRITICAL:** `CREDIT_CARD`
- **HIGH:** `US_SSN`, `US_DRIVER_LICENSE`, `US_PASSPORT`, `IBAN_CODE`, `MEDICAL_LICENSE`
- **MEDIUM:** `PHONE_NUMBER`, `EMAIL_ADDRESS`, `PERSON`

You can override the mapping in config, for example:

```yaml
ebook:
  chat:
    presidio:
      entity-risk-mapping:
        US_SSN: HIGH
        CREDIT_CARD: CRITICAL
        PERSON: LOW
```

Optional: `entity-filter` — list of entity types to consider (empty = all). Example: `entity-filter: [US_SSN, CREDIT_CARD, EMAIL_ADDRESS]`.

---

## Architecture

- **DlpProvider:** Interface implemented by `RuleBasedDlpProvider` (keywords/regex) and `PresidioDlpProvider` (Presidio HTTP client).
- **DlpEngine:** Facade that always runs the rule-based provider; when Presidio is enabled, also runs `PresidioDlpProvider` and merges results (max risk, combined rule IDs). On Presidio timeout or error, returns rule-based result only.
- **Message and file DLP:** Both `scan(text)` and `scanFile(contentType, content)` use this pipeline; for text/plain files, extracted text is sent to the same pipeline (Presidio included when enabled).

---

## Metrics (TP.8)

When Presidio metrics are enabled, the app can expose:

- `presidio.calls.total` — number of Presidio analyze calls
- `presidio.calls.failures` — number of timeouts/errors
- `presidio.latency` — last call latency (ms)

See runbook or actuator `/metrics` (admin) if implemented.

---

*Document version: 1*
