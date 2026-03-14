# API health check – when the API keeps loading / not working

Use these steps to see why the API seems stuck or not responding.

---

## Quick copy-paste commands (run in order)

From project root. Run in your own terminal (e.g. Mac Terminal or VS Code integrated terminal).

**1. See if containers are running**
```bash
docker ps -a
```

**2. Start the app with Docker** (from project root)
```bash
cd "/Users/sotsirymony/Desktop/Chat System asesstment II/chat-app-spring-websocket-cassandra-redis-rabbitmq"
docker-compose -f docker-compose/dependencies.yml up -d ebook-chat-app
```

**3. Or run the backend locally** (with dependencies already up)
```bash
cd "/Users/sotsirymony/Desktop/Chat System asesstment II/chat-app-spring-websocket-cassandra-redis-rabbitmq/ebook-chat"
mvn spring-boot:run
```

**4. After the app is up – test the API**
```bash
curl http://localhost:8080/
```
```bash
curl -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

**5. If using Docker and something’s wrong – check logs**
```bash
docker logs ebook-chat-app --tail 100
```

---

## 1. Is the backend running?

**If using Docker (ebook-chat-app container):**

```bash
docker ps --filter name=ebook-chat-app
```

- If **Up** or **Up (healthy)** → container is running. Go to step 2.
- If **Exited** or missing → start or restart:
  ```bash
  docker-compose -f docker-compose/dependencies.yml up -d ebook-chat-app
  ```

**If running locally (`mvn spring-boot:run` or `java -jar`):**

- Check the terminal where you started it. Look for `Started Application in ...` (success) or stack traces (failure).
- Dependencies (MySQL, Redis, Cassandra, RabbitMQ, MinIO) must be up. Easiest: start them with Docker:
  ```bash
  docker-compose -f docker-compose/dependencies.yml up -d mysql redis cassandra rabbitmq-stomp minio
  ```
  Wait 30–60 seconds, then start the app.

---

## 2. Does the API respond on port 8080?

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/
```

- **200 or 302** → backend is up; the “loading” may be frontend or auth. Try step 3.
- **000 or connection refused** → nothing is listening on 8080 or the app is still starting/crashed. Check logs (step 4).

---

## 3. Test a real endpoint

```bash
curl -s -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

- JSON with `token` → API is working; use that token or log in via the UI.
- 401/403 → API is up but credentials wrong or not allowed.
- No response / timeout → app may be stuck (e.g. waiting for DB). Check logs.

---

## 4. Check backend logs

**Docker:**

```bash
docker logs ebook-chat-app --tail 100
```

**Local run:**  
Read the terminal where you ran `mvn spring-boot:run` or `java -jar`.

**Look for:**

- `Started Application in ...` → started OK; if API still doesn’t respond, check port or firewall.
- `Communications link failure` / `Unable to obtain Jdbc connection` → **MySQL not reachable**. Start dependencies (see step 1) and wait.
- `Connection refused` to Cassandra/Redis/RabbitMQ → start those services and ensure host/port match `application.yml` or Docker args.
- `Address already in use` / `Bind for 0.0.0.0:8080 failed` → something else is using 8080. Stop it or change `server.port`.

---

## 5. Frontend still “loading”

- **Backend URL:** Frontend uses `NEXT_PUBLIC_API_URL` or default `http://localhost:8080`. Ensure it matches where the backend runs.
- **CORS:** Backend allows `http://localhost:3000`. If the frontend runs on another origin, add it in `WebConfig` (CORS) and, for WebSocket, in `WebSocketConfigSpringSession` (`setAllowedOrigins`).
- **Browser:** Open DevTools → Network. Reload and see if the request to `localhost:8080` is pending, failed, or 4xx/5xx.

---

## Quick checklist

| Check | Command / action |
|-------|-------------------|
| Dependencies up (Docker) | `docker-compose -f docker-compose/dependencies.yml ps` |
| App container up | `docker ps --filter name=ebook-chat-app` |
| Port 8080 response | `curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/` |
| Auth works | `curl -X POST http://localhost:8080/api/auth/token -H "Content-Type: application/json" -d '{"username":"admin","password":"admin"}'` |
| App logs | `docker logs ebook-chat-app --tail 100` |

After fixing the failing step, reload the app and test again.
