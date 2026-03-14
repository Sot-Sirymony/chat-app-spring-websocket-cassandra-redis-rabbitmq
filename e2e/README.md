# E2E UI Tests (Playwright)

End-to-end tests for the Thymeleaf chat UI. Test cases align with `docs/UI-Test-Cases.md`.

## Prerequisites

- Node.js 18+
- **Backend running** at http://localhost:8080 (MySQL, Redis, Cassandra, RabbitMQ + Spring Boot)

```bash
# From repo root: start dependencies (if using Docker)
docker-compose -f docker-compose/dependencies.yml up -d

# Start Spring Boot
cd ebook-chat && mvn spring-boot:run
```

## Install and run

```bash
cd e2e
npm install
npx playwright install chromium
npm test
```

Optional:

- `BASE_URL=http://localhost:8080 npm test` — override base URL
- `npm run test:headed` — run with browser visible
- `npm run report` — open last HTML report

## Next.js frontend smoke test

To run the smoke test against the Next.js app (login → chat list → join room → send message):

1. Start the **backend** at http://localhost:8080.
2. Start the **Next.js frontend** at http://localhost:3000 (`cd frontend && npm run dev`).
3. From the `e2e` folder:

```bash
npx playwright test tests/nextjs-smoke.spec.ts --config=playwright.nextjs.config.ts
```

## Test files

| File | Coverage |
|------|----------|
| `tests/auth.spec.ts` | Login, logout, registration (A1.x, A2.x, A3.x) |
| `tests/navigation.spec.ts` | Navbar, access control (N1.x, N2.x) |
| `tests/chat-list.spec.ts` | Chat list, create room, join (C1.x, C2.x, C3.x) |
| `tests/chatroom.spec.ts` | Chat room load, WebSocket, leave (R1.x, R6.x, R2.3) |
| `tests/approvals-analytics.spec.ts` | Approvals, analytics (P1.x, P2.x, X1.x) |

## Test users

- **admin** / **admin** (ROLE_ADMIN)
- **user** / **password** (ROLE_USER)
