# Manual test examples: DLP (approval / block / warn) and analytics

Use these example messages and steps to manually test the **approval flow**, **DLP actions**, and **analytics** (risky users, rooms, message events).

---

## 1. DLP rules in this app (from `application.yml`)

| Rule ID              | Type   | Pattern              | Risk level | Action              |
|----------------------|--------|----------------------|------------|---------------------|
| `pii-ssn`            | REGEX  | US SSN `\d{3}-\d{2}-\d{4}` | **HIGH**   | **REQUIRE_APPROVAL** |
| `keyword-confidential` | KEYWORD | `confidential`     | MEDIUM     | WARN                |
| `keyword-secret`     | KEYWORD | `secret`            | MEDIUM     | WARN                |

Risk → action: **CRITICAL** = BLOCK, **HIGH** = REQUIRE_APPROVAL, **MEDIUM** = WARN, **LOW** = ALLOW.

---

## 1.1 Full list: confidential / sensitive content types the current DLP supports

**What is scanned**

| Source | Scanned? | Notes |
|--------|----------|--------|
| **Chat message text** | Yes | Every sent message; rule-based + Presidio (if enabled). |
| **File upload** | Only **plain text** | Only files with `content-type: text/plain` (e.g. `.txt`). Content is decoded as UTF-8 and run through the same rules as message text. |
| **Images (PNG, JPEG, etc.)** | No | No OCR; not scanned. |
| **PDF, Office, etc.** | No | Not scanned. |

**Rule-based DLP (always on)** — sensitive text / patterns:

| Type | Pattern / trigger | Risk | Action |
|------|-------------------|------|--------|
| **US SSN** | Regex: 3 digits, hyphen, 2 digits, hyphen, 4 digits (e.g. `123-45-6789`) | HIGH | REQUIRE_APPROVAL |
| **Keyword: confidential** | Exact word `confidential` (case-insensitive) | MEDIUM | WARN |
| **Keyword: secret** | Exact word `secret` (case-insensitive) | MEDIUM | WARN |

**Presidio DLP (when `ebook.chat.presidio.enabled: true`)** — PII entity types (in addition to rules above):

| Entity type | Risk | Action |
|-------------|------|--------|
| CREDIT_CARD | CRITICAL | BLOCK |
| US_SSN | HIGH | REQUIRE_APPROVAL |
| US_DRIVER_LICENSE | HIGH | REQUIRE_APPROVAL |
| US_PASSPORT | HIGH | REQUIRE_APPROVAL |
| IBAN_CODE | HIGH | REQUIRE_APPROVAL |
| MEDICAL_LICENSE | HIGH | REQUIRE_APPROVAL |
| PHONE_NUMBER | MEDIUM | WARN |
| EMAIL_ADDRESS | MEDIUM | WARN |
| PERSON | MEDIUM | WARN |
| (any other entity Presidio returns) | LOW | ALLOW |

**Summary:** Today, “confidential” message or file content means: (1) **rule-based:** SSN pattern, “confidential”, “secret”; (2) **if Presidio is on:** credit card, SSN, driver license, passport, IBAN, medical license, phone, email, person names. Only **message text** and **plain-text file content** are scanned; images and other binary files are not.

---

## 1.2 Copy-paste examples for easy testing (messages and .txt files)

Use these in **chat** or save as **.txt** and upload. Rule-based DLP only unless Presidio is enabled.

### REQUIRE_APPROVAL (SSN — request goes to admin Approvals)

| Use as | Example (copy-paste) |
|--------|----------------------|
| Chat message | `My SSN is 123-45-6789` |
| Chat message | `Please use SSN 987-65-4321 for verification.` |
| Chat message | `Contact: 555-00-1234. SSN 111-22-3334.` |
| .txt file content | `Customer SSN: 123-45-6789. Do not share.` |
| .txt file content | `SSN 999-88-7776 on file.` |

### WARN (message sent with DLP warning — confidential / secret)

| Use as | Example (copy-paste) |
|--------|----------------------|
| Chat message | `This is confidential information.` |
| Chat message | `Keep it secret.` |
| Chat message | `Confidential report attached.` |
| .txt file content | `Internal use only. Confidential.` |
| .txt file content | `Secret password reset link inside.` |

### BLOCK (only if you add a CRITICAL rule, e.g. keyword "blocked-word")

| Use as | Example (copy-paste) |
|--------|----------------------|
| Chat message | `Do not share this blocked-word in chat.` |
| .txt file content | `File contains blocked-word.` |

### Combined (for one .txt file that triggers multiple rules)

Save as `dlp_test.txt` and upload, or paste in chat:

```
This document is CONFIDENTIAL.
Customer SSN: 123-45-6789. Keep it secret.
Contact: 555-00-1111. Do not share.
```

This triggers: SSN (REQUIRE_APPROVAL), confidential (WARN), secret (WARN). Highest action wins (REQUIRE_APPROVAL for the message/file).

### Plain .txt file examples (for upload testing)

**File: `ssn_only.txt`** (REQUIRE_APPROVAL)

```
My SSN is 123-45-6789
```

**File: `warn_only.txt`** (WARN)

```
Confidential - internal only. Secret draft.
```

**File: `mixed.txt`** (REQUIRE_APPROVAL because of SSN)

```
Hello,
Please verify with SSN 111-22-3334.
This is confidential.
Thanks.
```

---

## 2. Example messages for manual testing

### Approval case (REQUIRE_APPROVAL)

Triggers **pii-ssn** (HIGH) → message goes to approval; a pending request is created; user sees “Message requires approval” and a **request ID**.

**Example messages (paste in chat):**

- `My SSN is 123-45-6789`
- `Please call back to 555-12-3456 or use SSN 987-65-4321 for verification.`

**What to verify:**

1. Send one of the above in the chat (public or private).
2. You should get a policy-denial with `reason` and `requestId`.
3. **Approvals** page (or `/approvals`) shows a pending request for that message.
4. Admin approves/rejects the request; after approval the message can be delivered (per your approval flow).
5. **Analytics:**  
   - `GET /api/analytics/risky-message-events?username=<that_user>`  
   should show an event with `action=SEND_MESSAGE`, `result=pending_approval`, `ruleId=dlp-REQUIRE_APPROVAL`.

---

### Get a file request to stay in the admin dashboard

To see a **pending file/message request** in the admin Approvals page (file request list):

1. Log in as a **normal user** (not admin).
2. Join a chat room.
3. Send one of these messages in the chat (they trigger **REQUIRE_APPROVAL** so a request is created and stays **PENDING** until admin acts):

**Example messages (copy-paste):**

- `My SSN is 123-45-6789`
- `Please use SSN 987-65-4321 for verification.`
- `Contact back: 555-00-1234. SSN 111-22-3334.`

4. The user will see “Message requires approval” and a request ID. The message will **not** appear in the chat yet.
5. Log in as **admin** (or open Approvals in an admin session).
6. Open **Approvals** (or `/approvals`). Under **“Pending requests (admin)”** you should see one row: **Requester** = that user, **Recipient**, **Room**, **Requested** time, **Approve** / **Reject**.
7. The request **stays** in that list until the admin clicks **Approve** or **Reject**. After approval, the message is delivered (if the room still exists) and the request moves to **APPROVED** and disappears from pending.

**With a file attachment (optional):** Upload a file, then send a message that contains an SSN (e.g. `My SSN is 123-45-6789`) and attach that file. The pending request will have both message text and the file reference, and it will appear the same way in the admin dashboard.

---

### Why image files (e.g. credit card, passport, bank screen) are not blocked or flagged

DLP in this app **only scans text**:

- **Chat messages:** Only the **message text** is scanned (e.g. SSN regex, keywords). Attached files are not read for DLP when sending the message; the message text is.
- **File upload:** Only **plain-text files** (`text/plain`) are scanned. **Images** (PNG, JPEG, etc.) and PDFs are **not** analyzed—there is no OCR or image content scanning. They are allowed through.

So if you upload or send **images** of a credit card, passport, or bank verification screen, the app will **not** detect the sensitive data inside the image and will **not** block or put them in approval. To test DLP with that kind of data:

- **Option A:** Type the sensitive data as **text** in the chat (e.g. `My SSN is 123-45-6789` or add a rule for card numbers and type `1234 5678 9102 3456`).
- **Option B:** Put the same data in a **.txt** file and upload it; plain-text files are scanned.
- **Option C:** Send a message that triggers approval (e.g. SSN in text) and **attach** the image; the request will be pending because of the message text, and the image will be linked to that request.

---

### Warn case (WARN – message sent with DLP warning)

Triggers **keyword-confidential** or **keyword-secret** (MEDIUM) → message is **sent**, but the client gets a DLP warning.

**Example messages:**

- `This is confidential information.`
- `Keep it secret.`
- `Confidential report attached.`

**What to verify:**

1. Send one of the above.
2. Message appears in the chat.
3. Client receives a DLP warning (e.g. `dlpWarning` in the message or a separate warning channel).
4. **Analytics:**  
   - `GET /api/analytics/risky-message-events?username=<that_user>`  
   shows an event with `ruleId=dlp-WARN`, `result=allow`, and `details` with the DLP message (so admin can see which messages triggered a warning).  
   - Risky-users/risky-rooms are based on **deny** events only; WARN does not add to deny count.

---

### Block case (BLOCK)

Default rules have **no CRITICAL** rule, so nothing is blocked by rule-based DLP unless you add one.

**Option A – Add a BLOCK rule for testing**

In `application.yml` under `ebook.chat.dlp.rules` add:

```yaml
- id: keyword-blocked
  type: KEYWORD
  pattern: "blocked-word"
  riskLevel: CRITICAL
```

Then use this **example message:**

- `Do not share this blocked-word in chat.`

**What to verify:**

1. Send the message.
2. Message is **not** sent; user gets a policy-denial (e.g. “Message blocked by policy”).
3. **Analytics:**  
   - `GET /api/analytics/risky-message-events?username=<that_user>`  
   shows an event with `result=deny`, `ruleId=dlp-BLOCK`, and `details` with the DLP message.

**Option B – Without changing config**

If you don’t add a CRITICAL rule, you can only test BLOCK when **Presidio** is enabled and returns an entity mapped to CRITICAL (e.g. `CREDIT_CARD` in default mapping). Example: a message containing a credit card number that Presidio detects.

---

## 3. Analytics endpoints (manual checks)

All under `/api/analytics`, **admin only** (ROLE_ADMIN + valid JWT).

### Risky users (denial count per user)

```http
GET /api/analytics/risky-users?limit=20
```

After sending messages that are **denied** (e.g. BLOCK or policy/risk-score denial), the user’s deny count increases and appears here.

### Risky rooms (denial count per room)

```http
GET /api/analytics/risky-rooms?limit=20
```

Rooms where many denials occurred (resource = chat room id).

### Risky message events (for approval / analytics)

```http
GET /api/analytics/risky-message-events?username=<username>
GET /api/analytics/risky-message-events?username=<username>&from=2026-03-01T00:00:00&to=2026-03-12T23:59:59
```

Returns audit events for **SEND_MESSAGE** that are either **deny** or have a DLP/risk **ruleId** (e.g. `dlp-BLOCK`, `dlp-REQUIRE_APPROVAL`, `user-risk-score`). Use this to confirm:

- **Approval case:** events with `result=pending_approval`, `ruleId=dlp-REQUIRE_APPROVAL`.
- **Block case:** events with `result=deny`, `ruleId=dlp-BLOCK`.

### Alert status (approval backlog)

```http
GET /api/analytics/alert-status
```

Shows `approvalBacklog` (count of pending approval requests) and `approvalBacklogAlert` (e.g. true when backlog > 20). Useful after creating several REQUIRE_APPROVAL messages.

---

## 4. Quick checklist

| Scenario              | Example message (rule-based)        | Expected result                    | Analytics check |
|-----------------------|--------------------------------------|------------------------------------|------------------|
| **Approval**          | `My SSN is 123-45-6789`              | Policy-denial + requestId; pending on Approvals | `risky-message-events` has `pending_approval` / `dlp-REQUIRE_APPROVAL` |
| **Warn**              | `This is confidential`              | Message sent + DLP warning          | (optional)       |
| **Block**             | Add CRITICAL rule, then e.g. `blocked-word` | Message blocked; denial to user   | `risky-message-events` has `deny` / `dlp-BLOCK` |
| **Risky users/rooms** | Trigger several denials (block or policy) | Counts increase                    | `risky-users`, `risky-rooms` |

Use these example messages and endpoints to manually verify the approval case and analytics behaviour end-to-end.
