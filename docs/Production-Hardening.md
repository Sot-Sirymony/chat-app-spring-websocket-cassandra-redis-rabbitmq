# Production hardening (BR-7.1)

Notes for securing the application in production.

---

## T7.1.1 — Enforce TLS (HTTPS)

### Option A: Spring Boot server.ssl

Add to `application.yml` (or use a `production` profile):

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: tomcat
```

Generate a keystore (e.g. for development):

```bash
keytool -genkeypair -alias tomcat -storetype PKCS12 -keyalg RSA -keysize 2048 \
  -keystore keystore.p12 -validity 3650 -storepass changeit -dname "CN=localhost"
```

Do not commit the keystore or password; use environment variables or a secret manager.

### Option B: Reverse proxy (recommended)

Run the app on HTTP (e.g. 8080) behind nginx or another reverse proxy that terminates TLS:

- Proxy listens on 443 (HTTPS); forwards to `http://localhost:8080`.
- Redirect HTTP (80) to HTTPS (301/302).
- Set headers such as `X-Forwarded-Proto: https` so the app can generate correct URLs.

Example nginx snippet:

```nginx
server {
    listen 443 ssl;
    server_name your-domain.com;
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
server {
    listen 80;
    return 301 https://$host$request_uri;
}
```

### Checklist

- [ ] TLS enabled (server.ssl or reverse proxy).
- [ ] HTTP redirected to HTTPS.
- [ ] No plain HTTP in production.

---

## T7.1.2 — Encrypt sensitive file content at rest

**Implemented.** File content can be encrypted with AES-256-GCM before storage in MinIO.

- **Config:** `ebook.chat.file-encryption.enabled=true` and `ebook.chat.file-encryption.key-base64=<base64 of 32-byte key>` (or env `FILE_ENCRYPTION_KEY_BASE64`).
- **Flow:** `MinioFileService` uses `FileEncryptionService`: on upload, content is encrypted; on download, decrypted. Metadata (e.g. original size) is stored unencrypted.
- **Key:** Generate a 32-byte key (e.g. `openssl rand -base64 32`) and store in a secret manager; do not commit.

---

## T7.1.3 — Room-level or E2E encryption (design)

Optional design for highly sensitive channels:

- **Room-level:** Derive a symmetric key per room (e.g. from a master key + roomId via KDF). Encrypt message payloads (and optionally file content) with that key before persisting to Cassandra/MinIO. Only members who know the room key can decrypt. Key distribution can be out-of-band or via a key server that authorizes members.
- **E2E:** Each user has a key pair; room keys are encrypted for each member’s public key. Clients decrypt with their private key (never sent to server). Server only sees ciphertext. Requires client-side crypto and key exchange protocol (e.g. Signal-style).
- **Trade-offs:** Room-level reduces server-side plaintext; E2E minimizes trust in the server but complicates search, DLP, and compliance. Document chosen approach and key lifecycle in a separate design doc.

---

*Document version: 1*
