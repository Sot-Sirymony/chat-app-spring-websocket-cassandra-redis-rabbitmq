# Demo .txt files for DLP testing

Use these plain-text files to test file-upload DLP. Only `text/plain` files are scanned.

| File | Expected DLP result |
|------|---------------------|
| **file_test.txt** or **ssn_only.txt** | REQUIRE_APPROVAL (SSN) — upload may be blocked or require approval depending on flow. |
| **warn_only.txt** | WARN (confidential, secret) — file allowed with warning. |
| **mixed.txt** | REQUIRE_APPROVAL (SSN + confidential/secret) — highest action wins. |

Upload via the chat app's file upload, or use in API tests.
