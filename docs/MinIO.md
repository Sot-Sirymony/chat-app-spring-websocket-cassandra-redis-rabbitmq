# MinIO file storage (BR-3.3)

File uploads are stored in MinIO (S3-compatible object storage). The app uses it for attachments in chat and for file transfer approval workflow.

## Configuration

Set in `application.yml` or environment variables:

| Property | Env var | Default |
|----------|---------|---------|
| `ebook.chat.minio.endpoint` | `MINIO_ENDPOINT` | `http://localhost:9000` |
| `ebook.chat.minio.bucket` | `MINIO_BUCKET` | `ebook-chat-files` |
| `ebook.chat.minio.access-key` | `MINIO_ACCESS_KEY` | `minioadmin` |
| `ebook.chat.minio.secret-key` | `MINIO_SECRET_KEY` | `minioadmin` |

## Running MinIO locally (Docker)

```bash
docker run -d -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  --name minio \
  minio/minio server /data --console-address ":9001"
```

- API: http://localhost:9000  
- Console: http://localhost:9001 (optional)

The application creates the bucket `ebook-chat-files` on startup if it does not exist.

## API

- **POST /api/files/upload** — multipart file upload. Returns `{ fileId, filename, dlpWarning, dlpRequireApproval }`. DLP blocks (403) or allows with flags.
- **GET /api/files/{id}/download** — secure download; allowed for uploader or (when linked to an approved request) requester/recipient.

## Task reference

See [Implementation-Tasks-v1.md](Implementation-Tasks-v1.md) — MinIO block TM.1–TM.8.
