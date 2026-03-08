# Scalability runbook (BR-6.1)

Guidance for running the chat application across multiple instances with shared session and messaging.

---

## T6.1.1 — Redis session store across instances

### Current setup

- **Spring Session** is used with **Redis** as the session store (`spring.session.store-type: redis` in `application.yml`).
- Session data (including authentication) is stored in Redis, not in the application process.

### Verification

- Start **two** application instances (different ports, e.g. 8080 and 8081), both pointing to the **same** Redis host/port.
- Log in via the first instance (browser gets a session cookie).
- Open the same app URL on the second instance (e.g. `http://localhost:8081`) and reuse the same session cookie (same domain or copy cookie for testing).
- The second instance should recognize the session and show the user as logged in.

### Conclusion

Sessions are shared across instances; no sticky session is required for HTTP login when Redis is shared.

---

## T6.1.2 — RabbitMQ broker relay for STOMP

### Current setup

- STOMP broker relay is configured in `WebSocketConfigSpringSession`: messages are forwarded to **RabbitMQ** (relay host/port, typically 61613 for STOMP).
- Subscriptions and broadcasts go through RabbitMQ, so all instances share the same broker.

### Verification

- Run two app instances (e.g. 8080 and 8081) behind a load balancer or hit them directly.
- User A connects to instance 1 and joins a room; User B connects to instance 2 and joins the same room.
- User A sends a message; User B should receive it (and vice versa).
- If both users receive each other's messages, multi-node messaging works.

### Conclusion

RabbitMQ broker relay ensures STOMP messages are distributed across app instances; no instance-local state is required for pub/sub.

---

## T6.1.4 — WebSocket / sticky session notes

- **HTTP requests** (login, REST, page load): No sticky session needed when using Redis for Spring Session.
- **WebSocket (STOMP over SockJS):** The initial connection is an HTTP upgrade. Once connected, the client holds a long-lived connection to **one** app instance. If you put a load balancer in front of multiple instances:
  - **Option A (recommended):** Enable **sticky session** (session affinity) so that the same client is always routed to the same instance for the WebSocket connection. This avoids reconnection when the load balancer forwards a request to a different instance mid-session.
  - **Option B:** Do not use sticky session; if the client is routed to a different instance on a subsequent request, the WebSocket may be to a different server than the one holding the STOMP subscription. Sticky session is simpler for WebSocket.
- **Recommendation:** Configure affinity by cookie (e.g. `JSESSIONID`) or by a custom header so that the same browser session hits the same instance for both HTTP and WebSocket.

---

## T6.1.3 — Multi-instance deployment

Use `docker-compose/dependencies.yml` together with `docker-compose/scaled-app.yml` for 2 app replicas behind nginx:

1. Start dependencies: `docker-compose -f docker-compose/dependencies.yml up -d` (optional: stop the single `ebook-chat-app` if you only want scaled instances).
2. Build the JAR: `mvn -f ebook-chat/pom.xml package -DskipTests`.
3. Run with scaled stack: `docker-compose -f docker-compose/dependencies.yml -f docker-compose/scaled-app.yml up`.

The load balancer listens on port 8080 and uses `ip_hash` for WebSocket affinity (T6.1.4).

---

*Document version: 1*
