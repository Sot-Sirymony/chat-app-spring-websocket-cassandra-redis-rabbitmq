# DLP manual test samples

Use these messages and files to manually test DLP (allow / warn / block / require approval).

## Messages (chat text)

| File | Expected DLP result | Purpose |
|------|---------------------|--------|
| message_1_allow.txt | Allow | Normal text, no PII or risky keywords — should pass with no warning. |
| message_2_allow.txt | Allow | Neutral operational message — no sensitive content. |
| message_3_allow.txt | Allow | Casual, non-sensitive — should pass. |
| message_2_warn.txt | Warn | Contains SSN-like content — should trigger a warning but still be sendable (with confirmation). |
| message_4_warn.txt | Warn | Applicant/ID-style number — warning, confirm to send. |
| message_5_warn.txt | Warn | “Confidential” keyword — warning, confirm to send. |
| message_3_block.txt | Block | External sharing + SSN/credit card/health + “send ASAP” — should be blocked by DLP. |
| message_4_block.txt | Block | Forward PII to external email — should be blocked. |
| message_5_block.txt | Block | Explicit leak intent + payroll/SSN — should be blocked. |

## Files (uploads)

| File | Expected DLP result | Purpose |
|------|---------------------|--------|
| file_1_allow.txt | Allow | Meeting notes, no PII — should pass. |
| file_2_allow.txt | Allow | Internal process doc — no sensitive data. |
| file_1_warn.txt | Warn | Tax ID–like format — warning, confirm to send. |
| file_2_warn.txt | Warn | Support log with SSN — warning, confirm to send. |
| file_1_block.txt | Block | Full PII export to external — should be blocked. |
| file_2_block.txt | Block | Health records to external — should be blocked. |
| file_1_required_approve.txt | Require approval | Confidential HR summary — route through approval workflow. |
| file_2_required_approve.txt | Require approval | Internal contract draft — requires approval. |
| file_3_required_approve.txt | Require approval | Board/financial restricted — requires C-level approval. |
