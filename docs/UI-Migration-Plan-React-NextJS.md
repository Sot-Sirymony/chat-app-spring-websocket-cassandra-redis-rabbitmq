# UI Migration Plan: Thymeleaf → React + Next.js

This document outlines a phased plan to replace the current Thymeleaf + jQuery + Bootstrap UI with a React frontend built with Next.js, while keeping the existing Spring Boot backend (REST APIs, WebSocket, security).

---

## Current State Summary

| Area | Current stack |
|------|----------------|
| **Templates** | Thymeleaf (login, new-account, chat, chatroom, approvals, analytics, layout) |
| **Scripts** | jQuery, SockJS, STOMP, Noty, Bootstrap JS |
| **Styles** | Bootstrap 3, Webjars |
| **Auth (browser)** | Form POST to `/login` (session); JWT from `/api/auth/token` for WebSocket & API calls in chatroom |
| **Backend** | Spring MVC (HTML views) + REST (`/api/auth`, `/api/analytics`, `/api/files`, `/api/file-requests`, `/api/audit`) + STOMP over SockJS at `/ws` |

**Existing REST APIs (usable from React):**

- `POST /api/auth/token` — get JWT (body: `username`, `password`)
- `GET /api/analytics/risky-users`, `.../risky-rooms`, `.../alert-status` (admin)
- `GET/POST /api/files` — upload, download
- `GET /api/file-requests/pending`, `.../my`, `POST .../approve`, `.../reject`
- `GET /api/audit`

**Missing for a full SPA (to be added in backend):**

- `GET /api/chatrooms` — list all chat rooms (today only in server-rendered `/chat`)
- `GET /api/chatrooms/{id}` — single room details for join page (name, classification, etc.)
- User registration API (optional): today `POST /new-account` is form; could add `POST /api/auth/register` for React)

---

## Target State

- **Frontend:** Next.js (App Router or Pages Router) + React.
- **Auth:** JWT-only for Next.js app: login page calls `POST /api/auth/token`, store token (e.g. httpOnly cookie or memory + optional refresh), send `Authorization: Bearer <token>` on all API and WebSocket requests.
- **Backend:** Unchanged behavior; add CORS for Next.js origin and the new REST endpoints above. Thymeleaf routes can remain for a transition period or be removed once React is default.

---

## Phase 1 — Foundation

| # | Task | Details |
|---|------|---------|
| 1.1 | Create Next.js app | `npx create-next-app@latest` (e.g. `frontend` or `ebook-chat-ui`). Choose TypeScript, App Router (or Pages), ESLint, Tailwind (or keep Bootstrap via npm). |
| 1.2 | Configure API base URL | Env var `NEXT_PUBLIC_API_URL` (e.g. `http://localhost:8080`) and use it in a small `api/client.ts` (or axios/fetch wrapper) for all backend calls. |
| 1.3 | Enable CORS on Spring Boot | In backend, allow origin of Next.js dev (e.g. `http://localhost:3000`) and credentials if using cookies. |
| 1.4 | Add backend REST: GET /api/chatrooms | New controller (or extend existing) returning `List<ChatRoom>` from `ChatRoomService.findAll()`. Secure with `authenticated()`. |
| 1.5 | Add backend REST: GET /api/chatrooms/{id} | Return single `ChatRoom` by id (or 404). Used by React when opening a room. |

**Exit criteria:** Next.js runs; can call `GET /api/chatrooms` with JWT from Postman/curl; CORS works from browser.

---

## Phase 2 — Authentication & Layout

| # | Task | Details |
|---|------|---------|
| 2.1 | Login page | Page that posts `username`/`password` to `POST /api/auth/token`, stores JWT (e.g. in memory + localStorage, or cookie). Redirect to `/chat` on success. |
| 2.2 | Auth context / state | React context (or Zustand/Redux) holding token and user (e.g. decode JWT for username/roles). Expose login, logout, and “isAuthenticated”. |
| 2.3 | Protected route wrapper | HOC or layout that redirects unauthenticated users to `/login`. Use for `/chat`, `/chatroom/[id]`, `/approvals`, `/analytics`. |
| 2.4 | Registration page | Form that submits to `POST /new-account` (or new `POST /api/auth/register` if you add it). Then redirect to login or auto-login with token. |
| 2.5 | App layout & nav | Shared layout: header with user name, “Leave” (logout), optional language switcher. Match current main links: Chat, Approvals, Analytics (admin-only). |

**Exit criteria:** User can log in, see layout, and be redirected to login when not authenticated.

---

## Phase 3 — Chat List & Room Creation

| # | Task | Details |
|---|------|---------|
| 3.1 | Chat list page | Page at `/chat`: fetch `GET /api/chatrooms` with Bearer token, render table (name, description, connected users count, “Join” link). |
| 3.2 | Create room (admin) | Button “New Chat Room” opens modal/form: name, description, classification (PUBLIC/INTERNAL/CONFIDENTIAL/RESTRICTED), optional allowed departments. Submit to `POST /chatroom` with Bearer token. Hide or disable for non-admin (optional: GET /api/me or role from JWT). |
| 3.3 | Join room navigation | “Join” links to `/chatroom/[id]` (dynamic route). |

**Exit criteria:** User sees room list and can open a room; admin can create rooms.

---

## Phase 4 — Chat Room Page & WebSocket

| # | Task | Details |
|---|------|---------|
| 4.1 | Chat room page shell | Page at `/chatroom/[id]`: load room with `GET /api/chatrooms/[id]` (name, classification). Show title and a “Leave” link back to `/chat`. |
| 4.2 | WebSocket connection | Use SockJS + STOMP (e.g. `sockjs-client` + `@stomp/stompjs` or `stompjs`). Connect to `NEXT_PUBLIC_WS_URL` (e.g. `http://localhost:8080/ws`). Send JWT in handshake (e.g. header `Authorization: Bearer <token>` or as query/header per your backend). |
| 4.3 | Subscriptions | Subscribe to: `/topic/{chatRoomId}.public.messages`, `/user/queue/{chatRoomId}.private.messages`, `/topic/{chatRoomId}.connected.users`, `/user/queue/policy-denial`, `/user/queue/recipient-warning`. On connect, send room id (e.g. in header) so backend can associate session with room. |
| 4.4 | Load old messages | Backend may push old messages on subscribe, or expose `GET /api/chatrooms/[id]/messages` if you add it; otherwise reuse existing WebSocket flow that sends history. Render messages in a scrollable list. |
| 4.5 | Send message | Input + Send button: build payload `{ text, toUser? }`, send via STOMP to `/chatroom/send.message` with room context. Handle policy-denial and recipient-warning in UI (e.g. toast). |
| 4.6 | Connected users list | On `/topic/{chatRoomId}.connected.users` (and similar), update local state and render list; allow clicking a user to set “send to” for private messages. |
| 4.7 | “Public” toggle | Button to switch back to “public” (clear private recipient). |

**Exit criteria:** User can join a room, see history, send/receive public and private messages, see connected users.

---

## Phase 5 — File Upload & Download

| # | Task | Details |
|---|------|---------|
| 5.1 | Attach file | “Attach” button, file input; upload via `POST /api/files/upload` (multipart) with Bearer token. Store returned `fileId` (and optional `filename`, `sizeBytes`, `dlpWarning`, `dlpRequireApproval`). |
| 5.2 | Send with file ref | When sending next message, include `fileRef: fileId` in payload. Backend will append download link in message. |
| 5.3 | Download links | In message body, detect `[Download attachment](...)` or render link from API; download via `GET /api/files/{id}/download` with `Authorization: Bearer <token>` (or use a blob fetch and trigger download in browser). |
| 5.4 | DLP warnings | Show `dlpWarning` / `dlpRequireApproval` in UI (e.g. under input or as badge). |

**Exit criteria:** User can attach a file, send it in chat, and download from message link.

---

## Phase 6 — Approvals & Analytics

| # | Task | Details |
|---|------|---------|
| 6.1 | Approvals page | Page at `/approvals`: fetch `GET /api/file-requests/pending` and `GET /api/file-requests/my`. List pending requests with Approve/Reject; list “my” requests and status. Call `POST /api/file-requests/{id}/approve` and `.../reject` with Bearer token. |
| 6.2 | Analytics page (admin) | Page at `/analytics`: fetch `GET /api/analytics/risky-users`, `.../risky-rooms`, `.../alert-status`. Render tables/cards. Restrict route to admin (role from JWT or 403 handling). |

**Exit criteria:** User can approve/reject file requests; admin can view analytics.

---

## Phase 7 — Polish & Cutover

| # | Task | Details |
|---|------|---------|
| 7.1 | Error handling | Global or per-page handling for 401 (redirect to login), 403 (forbidden message), 5xx (retry or error message). |
| 7.2 | i18n (optional) | If you need EN/PT like current app: use `next-intl` or `react-i18next` and replace Thymeleaf message keys with JSON/translations. |
| 7.3 | Styling | Align with current look (Bootstrap 3) or redesign with Tailwind. Ensure responsive layout for chat and room. |
| 7.4 | E2E / smoke tests | Basic Playwright or Cypress: login → open chat list → join room → send message. |
| 7.5 | Backend cleanup (optional) | Remove or redirect Thymeleaf controllers/views for `/`, `/login`, `/new-account`, `/chat`, `/chatroom/*`, `/approvals`, `/analytics` once React is default. Keep REST and WebSocket unchanged. |

**Exit criteria:** App is usable end-to-end; docs updated; optional Thymeleaf removal.

---

## Suggested Order of Work

1. **Phase 1** — Next.js + CORS + new REST endpoints.
2. **Phase 2** — Login, auth context, protected routes, layout.
3. **Phase 3** — Chat list + create room.
4. **Phase 4** — Chat room + WebSocket (biggest piece).
5. **Phase 5** — Files in chat.
6. **Phase 6** — Approvals and analytics.
7. **Phase 7** — Polish and cutover.

---

## Tech Suggestions (Next.js + React)

- **Next.js:** App Router (`app/`) for simplicity and future RSC use; or Pages Router if you prefer.
- **HTTP client:** `fetch` with a thin wrapper, or `axios`, using `NEXT_PUBLIC_API_URL` and attaching the JWT from context.
- **WebSocket:** `sockjs-client` + `@stomp/stompjs` (or `stompjs`) in a hook or context so one connection per room (or per app with room in headers).
- **State:** React Context for auth; local state or React Query/SWR for server data (chat list, room, analytics).
- **Styling:** Tailwind CSS (default with create-next-app) or add Bootstrap 5 for a familiar look.

---

## Libraries for Next.js (recommended list)

| Category        | Library              | Purpose |
|-----------------|----------------------|--------|
| **Framework**   | `next`               | Next.js (use version 14+ with App Router). |
| **Language**    | `react`, `react-dom` | Core React (included with Next.js). |
| **Language**    | `typescript`         | Type safety (optional but recommended). |
| **HTTP**        | `axios` (optional)   | API calls with interceptors for JWT; or use native `fetch`. |
| **WebSocket**   | `sockjs-client`      | SockJS transport to backend `/ws`. |
| **WebSocket**   | `@stomp/stompjs`     | STOMP over SockJS for chat messages and subscriptions. |
| **Auth/state**  | React Context (built-in) | Token and user state; or `zustand` for global state. |
| **Server state**| `swr` or `@tanstack/react-query` (optional) | Caching and refetch for chat list, analytics, etc. |
| **Styling**     | `tailwindcss`        | Utility CSS (default with `create-next-app`). |
| **UI components** | (optional) `shadcn/ui`, `@radix-ui/*`, or `react-bootstrap` | Buttons, modals, forms if you don’t build from scratch. |
| **Forms**       | (optional) `react-hook-form` + `zod` | Validation for login, registration, new room. |
| **Notifications** | (optional) `sonner` or `react-hot-toast` | Toasts for success/error (replacing Noty). |
| **i18n**        | (optional) `next-intl` or `react-i18next` | EN/PT if you keep the current language feature. |

**Minimal install (core only):**

```bash
npx create-next-app@latest frontend --typescript --tailwind --eslint --app
cd frontend
npm install sockjs-client @stomp/stompjs
# optional: npm install axios zustand swr
```

Use native `fetch` + React Context if you want to avoid extra dependencies at first; add axios, SWR, and UI libraries as needed.

---

## Backend Checklist (Spring Boot)

- [x] CORS enabled for Next.js origin (e.g. `http://localhost:3000`).
- [x] `GET /api/chatrooms` (list) — implemented and secured.
- [x] `GET /api/chatrooms/{id}` (single room) — implemented and secured.
- [x] (Optional) `POST /api/auth/register` for registration from React.
- [x] WebSocket handshake accepts JWT (e.g. from header or query); same as current chatroom.html if already using token.
- [x] Optional redirect: set `app.redirect-to-frontend: true` and `app.frontend-url: http://localhost:3000` to redirect Thymeleaf paths to the Next.js app.

---

## Document Info

- **Created:** 2026-03-08  
- **Scope:** Replace Thymeleaf UI with React + Next.js; backend remains Spring Boot.
