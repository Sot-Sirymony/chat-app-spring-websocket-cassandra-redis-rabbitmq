# Credentials reference

All credentials and secrets used by the application. **Use environment variables or a secret manager in production; do not rely on defaults.**

---

## 1. MySQL (datasource)

| Purpose    | Location | Default (dev/test) | Env override |
|-----------|----------|--------------------|-------------|
| Username  | `application.yml` → `spring.datasource.username` | `root` | `SPRING_DATASOURCE_USERNAME` or `--spring.datasource.username=` |
| Password  | `application.yml` → `spring.datasource.password` | `root` | `SPRING_DATASOURCE_PASSWORD` or `--spring.datasource.password=` |
| URL       | `application.yml` → `spring.datasource.url` | `jdbc:mysql://127.0.0.1:3306/ebook_chat?useSSL=false` | `SPRING_DATASOURCE_URL` |

**Docker (dependencies.yml):** MySQL service uses `MYSQL_ROOT_PASSWORD: root`, `MYSQL_DATABASE: ebook_chat`. App container passes `--spring.datasource.username=root` and `--spring.datasource.password=root`.

**Tests:** `AbstractIntegrationTest` uses Testcontainers MySQL with `MYSQL_ROOT_PASSWORD=root`.

---

## 2. JWT (ebook.chat.jwt)

| Purpose   | Location | Default | Env override |
|-----------|----------|---------|--------------|
| Secret key (HS256) | `application.yml` → `ebook.chat.jwt.secret` and `JwtProperties.java` | `ebook-chat-internal-secret-key-min-256-bits-for-hs256` | **`JWT_SECRET`** |
| Issuer    | `application.yml` → `ebook.chat.jwt.issuer` | `ebook-chat` | — |
| Expiration (ms) | `application.yml` → `ebook.chat.jwt.expiration-ms` | `86400000` (24h) | — |

**Important:** In production set `JWT_SECRET` to a long, random value (e.g. 256+ bits). The default is for development only.

---

## 3. MinIO (ebook.chat.minio)

| Purpose   | Location | Default | Env override |
|-----------|----------|---------|--------------|
| Endpoint  | `application.yml` → `ebook.chat.minio.endpoint` and `MinioProperties.java` | `http://localhost:9000` | **`MINIO_ENDPOINT`** |
| Bucket   | `application.yml` → `ebook.chat.minio.bucket` | `ebook-chat-files` | **`MINIO_BUCKET`** |
| Access key | `application.yml` → `ebook.chat.minio.access-key` and `MinioProperties.java` | `minioadmin` | **`MINIO_ACCESS_KEY`** |
| Secret key | `application.yml` → `ebook.chat.minio.secret-key` and `MinioProperties.java` | `minioadmin` | **`MINIO_SECRET_KEY`** |

**Docker (MinIO):** Default MinIO image uses `MINIO_ROOT_USER` / `MINIO_ROOT_PASSWORD` (e.g. `minioadmin` / `minioadmin`). Match these with `MINIO_ACCESS_KEY` and `MINIO_SECRET_KEY` in the app.

---

## 4. RabbitMQ (STOMP relay)

| Purpose   | Location | Default (from app) | Docker (dependencies.yml) |
|-----------|----------|--------------------|----------------------------|
| Host      | `application.yml` → `ebook.chat.relay.host` | `localhost` | Passed as `--spring.rabbitmq.host=rabbitmq-stomp` |
| Port      | `application.yml` → `ebook.chat.relay.port` | `61613` (STOMP) | `61613` exposed |
| Username  | Not in application.yml; set when using Spring RabbitMQ | — | **`guest`** (in docker-compose command: `--spring.rabbitmq.username=guest`) |
| Password  | Not in application.yml | — | **`guest`** (`--spring.rabbitmq.password=guest`) |

RabbitMQ default credentials are `guest`/`guest` (only for localhost by default).

---

## 5. Redis

| Purpose | Location | Default |
|---------|----------|--------|
| Host    | `application.yml` → `spring.redis.host` | `localhost` |
| Port    | `application.yml` → `spring.redis.port` | `6379` |

No password in current config. For production, configure Redis auth and `spring.redis.password` if required.

---

## 6. Application users (MySQL – ebook_chat.user)

Seeded by Flyway in **V1__init.sql**:

| Username | Password (plain) | Bcrypt hash in DB | Role |
|----------|-------------------|--------------------|------|
| **admin** | **admin** | `$2a$06$WfXHoFhYT/cXcyNOZQsjMuXRyydgcUTMJcMweF0m8RMub2HS1rCHu` | ROLE_ADMIN |

Other users are created via “New account” (e.g. tests use `jorge_acetozi` / `123456`). **Change the admin password in production** (e.g. via a migration or admin UI).

---

## 7. Summary table (defaults only)

| System    | Username / Key  | Password / Secret (default) |
|-----------|-----------------|-----------------------------|
| MySQL     | `root`          | `root`                      |
| JWT       | —               | `ebook-chat-internal-secret-key-min-256-bits-for-hs256` (set `JWT_SECRET` in prod) |
| MinIO     | `minioadmin`    | `minioadmin`                |
| RabbitMQ  | `guest`         | `guest`                    |
| App admin | `admin`         | `admin`                    |

---

## 8. File encryption at rest (T7.1.2)

| Purpose | Location | Env override |
|---------|----------|--------------|
| Enable encryption | `ebook.chat.file-encryption.enabled` | — |
| AES-256 key (base64) | `ebook.chat.file-encryption.key-base64` | **`FILE_ENCRYPTION_KEY_BASE64`** |

Generate key: `openssl rand -base64 32`. Do not commit the key.

---

## Production checklist

- [ ] Set **JWT_SECRET** (long random secret).
- [ ] Set **MINIO_ACCESS_KEY** and **MINIO_SECRET_KEY** (and use strong MinIO root credentials).
- [ ] Use strong **MySQL** and **RabbitMQ** passwords; pass via env or secrets.
- [ ] Change default **admin** user password (DB or migration).
- [ ] Do not commit real secrets to the repo; use env vars or a secret manager.
