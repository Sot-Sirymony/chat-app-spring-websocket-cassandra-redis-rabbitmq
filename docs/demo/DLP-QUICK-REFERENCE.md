# DLP quick reference: Allow, Require approval, Warning

Use these **simple messages** for manual DLP testing. Paste in chat or upload as .txt.

| DLP result | Simple message to send |
|------------|-------------------------|
| **Allow** | `Meet at 3pm for coffee. See you there.` |
| **Require approval** | `Customer SSN 123-45-6789 for verification.` |
| **Warning** | `Confidential. Internal use only.` |

## Demo files (paste content or upload file)

- **Allow** — `docs/demo/dlp_allow.txt`
- **Require approval** — `docs/demo/dlp_approval.txt` (contains SSN pattern)
- **Warning** — `docs/demo/dlp_warn.txt` (contains "confidential")

## Rule summary (application.yml)

| Rule ID | Type | Pattern | Risk | DLP action |
|---------|------|---------|------|------------|
| pii-ssn | REGEX | SSN XXX-XX-XXXX | HIGH | **Require approval** |
| keyword-confidential | KEYWORD | confidential | MEDIUM | **Warn** |
| keyword-secret | KEYWORD | secret | MEDIUM | **Warn** |
| keyword-leak | KEYWORD | leak | CRITICAL | **Block** |

No match → **Allow**.
