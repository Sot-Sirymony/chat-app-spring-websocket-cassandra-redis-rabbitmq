# UI Test Cases — Current Thymeleaf Chat Application

Test cases for all UI functionalities. Use for manual testing or as a specification for automated E2E tests (e.g. Playwright, Cypress). Base URL: **http://localhost:8080** (or your deployed URL).

**Test users:** `admin` / `admin` (ROLE_ADMIN), `user` / `password` (ROLE_USER).

---

## 1. Authentication

### 1.1 Login (form)

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| A1.1 | Successful login | 1. Go to `/` or `/login`. 2. Enter valid username and password. 3. Click Sign In. | Redirect to `/chat`; navbar shows "Chat Rooms" and username dropdown. |
| A1.2 | Invalid credentials | 1. Go to `/login`. 2. Enter wrong username or password. 3. Click Sign In. | Error message (e.g. "Bad credentials"); stay on login page. |
| A1.3 | Empty credentials | 1. Leave username or password blank. 2. Click Sign In. | Validation/error or no submit; stay on login page. |
| A1.4 | Link to registration | 1. On login page, click "Or create an account". | Navigate to `/new-account`. |

### 1.2 Logout

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| A2.1 | Logout from app | 1. Log in. 2. Open user dropdown (top right). 3. Click Logout. | Redirect to login; session ended; protected pages require login again. |
| A2.2 | Leave chatroom (chatroom page) | 1. Be in a chat room. 2. Click "Logout" in dropdown. | WebSocket disconnects; redirect to `/chat`. |

### 1.3 New account (registration)

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| A3.1 | Successful registration | 1. Go to `/new-account`. 2. Fill Name, Email, Username, Password (valid). 3. Click Create. | Account created; redirect or success message; can log in with new credentials. |
| A3.2 | Validation errors | 1. Submit with empty required fields or invalid email/username. | Validation errors shown per field; account not created. |
| A3.3 | Duplicate username | 1. Submit with username that already exists. | Error (e.g. username already taken); stay on form. |

---

## 2. Layout and navigation

### 2.1 Navbar (authenticated)

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| N1.1 | Chat Rooms link | 1. Log in. 2. Click "Chat Rooms" in navbar. | Navigate to `/chat`. |
| N1.2 | User dropdown | 1. Log in. 2. Click username (top right). | Dropdown shows: New Chat Room (admin only), Pending approvals (admin), Security analytics (admin), My approval requests, Logout. |
| N1.3 | Admin-only menu items | 1. Log in as **admin**. | Dropdown includes "New Chat Room", "Pending approvals", "Security analytics". |
| N1.4 | Non-admin menu | 1. Log in as **user** (ROLE_USER). | Dropdown does NOT show "New Chat Room", "Pending approvals", "Security analytics"; shows "My approval requests" and Logout. |
| N1.5 | Language switcher | 1. Click "Language" dropdown. 2. Select English or Portuguese. | Page reloads with selected locale (e.g. `?lang=en` or `?lang=pt`); labels use chosen language. |

### 2.2 Access control

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| N2.1 | Unauthenticated access to /chat | 1. Log out. 2. Open `/chat`. | Redirect to login. |
| N2.2 | Unauthenticated access to /chatroom/{id} | 1. Log out. 2. Open `/chatroom/any-id`. | Redirect to login. |
| N2.3 | Non-admin access to /analytics | 1. Log in as **user**. 2. Open `/analytics`. | 403 Forbidden or redirect. |
| N2.4 | Admin access to /analytics | 1. Log in as **admin**. 2. Click "Security analytics" or open `/analytics`. | Analytics page loads; risky users and risky rooms tables visible. |

---

## 3. Chat list (/chat)

### 3.1 View chat rooms

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| C1.1 | List rooms | 1. Log in. 2. Go to `/chat`. | Page title "Available Chat Rooms"; table with columns: Name, Description, Connected Users, action. |
| C1.2 | Empty list | 1. With no rooms created, go to `/chat`. | Table body empty or "No chat rooms" (if implemented). |
| C1.3 | Room count | 1. Create at least one room. 2. Open `/chat`. | Each row shows room name, description, connected users count (e.g. 0), and "Join" link. |

### 3.2 Create chat room (admin only)

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| C2.1 | Open New Chat Room modal | 1. Log in as **admin**. 2. Click username dropdown → "New Chat Room". | Modal opens with fields: Name, Description, Classification (dropdown), Allowed departments (optional). |
| C2.2 | Create room — success | 1. In modal, enter name and description. 2. Select classification (e.g. PUBLIC). 3. Click Create. | Modal closes; new row appears in table with name, description, 0 users, and Join link. |
| C2.3 | Create room — all classifications | 1. Create rooms with PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED. | Each room created and listed; RESTRICTED can have allowed departments. |
| C2.4 | Create room — close without saving | 1. Open modal. 2. Enter data. 3. Click Close. | Modal closes; no new room. |
| C2.5 | Non-admin cannot create | 1. Log in as **user**. | "New Chat Room" not in dropdown (or disabled); POST /chatroom returns 403 if attempted. |

### 3.3 Join room

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| C3.1 | Join via link | 1. On `/chat`, click "Join" for a room. | Navigate to `/chatroom/{id}`; chat room page loads. |
| C3.2 | Join invalid room id | 1. Open `/chatroom/non-existent-id`. | 404 or error page. |

---

## 4. Chat room (/chatroom/{id})

**Prerequisite for WebSocket:** If the app expects JWT for the chat room, obtain token via `POST /api/auth/token` (username, password) and store in `localStorage` as `ebookChatToken` before joining (or ensure your environment does this).

### 4.1 Page load and WebSocket

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| R1.1 | Room page loads | 1. From `/chat`, click Join on a room. | Page shows room title, Users panel (left), messages panel (center), message input and Send/Attach. |
| R1.2 | WebSocket connects | 1. Open chat room with valid session (and JWT if required). | Success notification (e.g. "WebSocket connection successfully established"); message input enabled. |
| R1.3 | Old messages loaded | 1. Join a room that has existing messages. | Past messages (public and private) appear in messages panel in order. |
| R1.4 | Connected users list | 1. Join room; have another user join same room. | "Users" panel lists connected usernames. |

### 4.2 Send public message

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| R2.1 | Send text | 1. Ensure "public" is selected. 2. Type message. 3. Click Send. | Message appears in panel with format "username: text"; other users in room see it. |
| R2.2 | Send with Enter | 1. Type message. 2. Press Enter. | Same as R2.1. |
| R2.3 | Empty message not sent | 1. Leave input empty. 2. Click Send. | No message sent; input focused. |
| R2.4 | Pending message and Undo | 1. Type message and click Send. 2. Within a few seconds, click Undo. | Pending message disappears; message is not sent. |

### 4.3 Private message

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| R3.1 | Select recipient | 1. In Users panel, click a username. | "sendTo" label changes from "public" to that username. |
| R3.2 | Send private message | 1. Select user. 2. Type message. 3. Send. | Message appears as [private] fromUser → toUser; only recipient sees it in their client. |
| R3.3 | Switch back to public | 1. After selecting a user, click "I want to send public messages". | "sendTo" shows "public"; next send is public. |

### 4.4 Policy and warnings

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| R4.1 | Policy denial | 1. Perform action that is denied by ABAC (e.g. join RESTRICTED room without department / trusted device, or trigger risk threshold). | Error notification (e.g. "Message blocked by policy" or "Recipient may not be in this room"). |
| R4.2 | DLP warning on message | 1. Send text that triggers DLP rule (e.g. keyword or pattern). | Message may still send; DLP warning shown in UI (e.g. below message or as notification). |
| R4.3 | High-risk room confirmation | 1. In CONFIDENTIAL or RESTRICTED room (or when sending private), send message. | Confirm dialog (e.g. "Send to …? Continue?"); on confirm, message sent. |

### 4.5 File attach and download

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| R5.1 | Attach file | 1. Click Attach. 2. Select file. | File uploads; text like "Attached: filename (size)" appears; optional DLP warning if applicable. |
| R5.2 | Send message with attachment | 1. Attach file. 2. Optionally add text. 3. Send. | Message appears with download link (e.g. "Download attachment"); link points to `/api/files/{id}/download`. |
| R5.3 | Download attachment | 1. In a message with attachment, click download link. | File downloads (with auth if required). |
| R5.4 | Upload blocked by policy | 1. Attempt upload when policy denies. | Error (e.g. "Upload blocked by policy"). |

### 4.6 Leave chat room

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| R6.1 | Leave via navbar | 1. In chat room, click username → Logout (Leave). | WebSocket disconnects; redirect to `/chat`. |

---

## 5. Approvals (/approvals)

### 5.1 Pending approval requests (admin only)

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| P1.1 | View pending (admin) | 1. Log in as **admin**. 2. Go to `/approvals` or click "Pending approvals". | Section "Pending approval requests" with table: ID, Requester, Recipient, Room, Content preview, Requested at, Approve/Reject buttons. |
| P1.2 | No pending | 1. As admin, open Approvals with no pending requests. | Table shows "No pending requests." or empty. |
| P1.3 | Approve request | 1. As admin, with pending request, click Approve. | Request approved; row removed from pending; "My approval requests" updates. |
| P1.4 | Reject request | 1. As admin, with pending request, click Reject. | Request rejected; row removed from pending; "My approval requests" updates. |
| P1.5 | Pending section hidden for non-admin | 1. Log in as **user**. 2. Go to `/approvals`. | "Pending approval requests" section not visible. |

### 5.2 My approval requests (all authenticated)

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| P2.1 | View my requests | 1. Log in. 2. Go to `/approvals` or "My approval requests". | Table: ID, Recipient, Room, Content preview, Status (PENDING/APPROVED/REJECTED), Requested at, Decided at. |
| P2.2 | No requests | 1. User with no requests. | "No requests." or empty. |
| P2.3 | Status labels | 1. Have requests in PENDING, APPROVED, REJECTED. | Correct labels/colors (e.g. warning/success/danger). |

---

## 6. Security analytics (/analytics) — Admin only

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| X1.1 | Page load (admin) | 1. Log in as **admin**. 2. Go to `/analytics`. | Title "Security analytics"; two tables: "Risky users (high deny count)" and "Risky rooms (high deny count)". |
| X1.2 | Risky users | 1. On analytics page. | Table shows Username, Deny count (or "No data." if none). |
| X1.3 | Risky rooms | 1. On analytics page. | Table shows Room ID, Deny count (or "No data." if none). |
| X1.4 | API failure handling | 1. With backend down or 500 for analytics API. | Table shows "Failed to load." or similar. |
| X1.5 | Non-admin forbidden | 1. Log in as **user**. 2. Open `/analytics`. | 403 or redirect; no analytics data. |

---

## 7. Language and UX

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| L1.1 | English | 1. Select English in Language dropdown. | Labels (e.g. Login, Chat Rooms, Join, Send) in English. |
| L1.2 | Portuguese | 1. Select Portuguese. | Labels in Portuguese (e.g. from messages_pt.properties). |
| L1.3 | Mobile / responsive | 1. Resize to small width or use mobile. | Navbar collapses to hamburger; layout remains usable. |

---

## 8. Error and edge cases

| ID   | Scenario | Steps | Expected result |
|------|----------|--------|------------------|
| E1.1 | WebSocket disconnect | 1. In chat room, stop backend or disconnect network. | Error notification; message input disabled; reconnect attempted (e.g. after 10 s). |
| E1.2 | Session timeout | 1. Log in. 2. Wait for session timeout (or clear session). 3. Click a protected link. | Redirect to login. |
| E1.3 | File upload failure | 1. Attach very large or invalid file (if limited). | Error message; no crash. |
| E1.4 | Invalid room id in URL | 1. Open `/chatroom/invalid-id`. | 404 or error page; no crash. |

---

## Test case summary by area

| Area | Test case IDs | Count |
|------|----------------|-------|
| Authentication (login, logout, registration) | A1.x, A2.x, A3.x | 9 |
| Layout and navigation | N1.x, N2.x | 9 |
| Chat list (view, create room, join) | C1.x, C2.x, C3.x | 9 |
| Chat room (load, WebSocket, messages, files, leave) | R1.x–R6.x | 18 |
| Approvals | P1.x, P2.x | 8 |
| Analytics | X1.x | 5 |
| Language / UX / errors | L1.x, E1.x | 7 |
| **Total** | | **~65** |

---

## Notes for automation

- **Login:** Use form submit to `/login` with username/password, or call `POST /api/auth/token` and set cookie/localStorage for JWT.
- **Chat room WebSocket:** If using JWT, set `localStorage.setItem('ebookChatToken', token)` before navigating to `/chatroom/{id}`.
- **Admin vs user:** Use `admin`/`admin` and `user`/`password` for role-based cases.
- **Create room:** Only admin; POST to `/chatroom` with JSON body `{ name, description, classification, allowedDepartments? }`.

Document version: 2026-03-08. Scope: Current Thymeleaf UI (templates: login, new-account, chat, chatroom, approvals, analytics).
