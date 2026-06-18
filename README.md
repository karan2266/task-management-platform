# Task Management Platform

A backend system built as **two independent Spring Boot applications** — one for user authentication and one for task management — that communicate over HTTP and share a single PostgreSQL database.

- **Auth Service** (Port 8081) — User registration, login, JWT issuance, refresh tokens
- **Task Service** (Port 8082) — Project and task CRUD with role-based access control

---

## Table of Contents

1. [Architecture](#architecture)
2. [Tech Stack](#tech-stack)
3. [Features Implemented](#features-implemented)
4. [Bonus Features](#bonus-features)
5. [Prerequisites](#prerequisites)
6. [Environment Variables](#environment-variables)
7. [Running the Services](#running-the-services)
8. [API Endpoints](#api-endpoints)
9. [Security Model](#security-model)
10. [Error Responses](#error-responses)
11. [Example Workflow](#example-workflow)

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│         Single PostgreSQL Instance (tmpdb)          │
├──────────────────────┬──────────────────────────────┤
│     authdb schema    │      taskdb schema           │
├──────────────────────┼──────────────────────────────┤
│ - users              │ - projects                   │
│ - refresh_tokens     │ - tasks                      │
└──────────────────────┴──────────────────────────────┘
         ▲                        ▲
         │                        │
    Port 8081              Port 8082
  ┌──────────────┐     ┌──────────────────┐
  │ Auth Service │◄────┤   Task Service   │
  │              │     │                  │
  │ JWT Issuance │     │ Project CRUD     │
  │ User Mgmt    │     │ Task CRUD        │
  │ Refresh Token│     │ User Validation  │
  └──────────────┘     └──────────────────┘
```

**Design decisions:**
- Both services share one PostgreSQL instance with isolated schemas (`authdb` and `taskdb`)
- The Task Service validates JWTs **locally** using the shared secret — no HTTP call to Auth Service for token validation
- The Task Service calls Auth Service (`GET /api/v1/users/{id}`) at the **application layer** to validate that an `assigneeUserId` refers to a real, active user
- Cross-service foreign keys (e.g. `assignee_user_id` → `authdb.users`) are logical only — no DB-level FKs across schemas
- Both services use **Flyway** for database migrations

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Security | Spring Security 6, JWT (JJWT 0.12.6, HS256) |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL 15+ |
| Migrations | Flyway |
| Build | Maven (multi-module layout) |
| Docs | SpringDoc OpenAPI / Swagger UI |
| Container | Docker + Docker Compose |
| Utilities | Lombok |

---

## Features Implemented

### Core
- ✅ User registration with email uniqueness validation
- ✅ Login returning signed JWT (HS256, configurable expiry)
- ✅ Two roles: `ADMIN` and `USER`
- ✅ Password storage using BCrypt
- ✅ JWT-based stateless authentication (subject = userId, role, iat, exp)
- ✅ Role-based access control (ADMIN / USER)
- ✅ Full CRUD for Projects
- ✅ Full CRUD for Tasks (each belonging to a Project)
- ✅ Task assignment with cross-service user existence validation
- ✅ Task status state machine: `TODO → IN_PROGRESS → DONE` (and `DONE → IN_PROGRESS` to re-open)
- ✅ List tasks with optional `?status=` filter
- ✅ `GET /api/v1/tasks/my-tasks` — all tasks assigned to the authenticated user
- ✅ Global exception handler (`@ControllerAdvice`) with consistent JSON error structure
- ✅ `@Valid` input validation on all request DTOs
- ✅ Sensitive fields (`password_hash`) never returned in any API response

### Bonus
- ✅ **Flyway migrations** — versioned migration scripts for both services
- ✅ **Docker Compose** — single command starts both services + PostgreSQL
- ✅ **Swagger UI** — auto-generated docs at `/swagger-ui.html` on both services
- ✅ **Task due-date + overdue endpoint** — `GET /api/v1/tasks/overdue`
- ✅ **Refresh token** — `POST /api/v1/auth/refresh` with 7-day rotating refresh token

---

## Prerequisites

### Without Docker
- **Java 21+**
- **Maven 3.6+**
- **PostgreSQL 15+** (running on `localhost:5432`)

```bash
java -version        # must show 21.x
mvn --version        # must show 3.6+
psql --version       # must show 15+
```

### With Docker
- **Docker** (24+)
- **Docker Compose** (v2 — `docker compose` command)

---

## Environment Variables

Set all of these **before running the services locally**. When using Docker Compose, these are provided automatically via the `docker-compose.yml`.

| Variable | Used By | Description | Example Value |
|----------|---------|-------------|---------------|
| `DB_URL` | Both | JDBC connection URL | `jdbc:postgresql://localhost:5432/tmpdb` |
| `DB_USER` | Both | Database username | `postgres` |
| `DB_PASSWORD` | Both | Database password | `yourpassword` |
| `JWT_SECRET` | Both | HS256 signing secret (min 32 chars) | `your-super-secret-key-min-32-chars!!` |
| `JWT_EXPIRATION_MS` | Both | Access token lifetime in ms | `86400000` *(24 h, default)* |
| `ADMIN_EMAIL` | Auth | Admin account email (auto-created) | `admin@example.com` |
| `ADMIN_PASSWORD` | Auth | Admin account password | `[PASSWORD]` |
| `ADMIN_FULL_NAME` | Auth | Admin account display name | `Platform Admin` |
| `AUTH_SERVICE_URL` | Task | Base URL of Auth Service | `http://localhost:8081` |

> **Important:** `JWT_SECRET` must be **identical** in both services for token validation to work.

### Recommended: `.env` File

Create a `.env` file at the project root (already in `.gitignore`):

```bash
# .env
DB_URL=jdbc:postgresql://localhost:5432/tmpdb
DB_USER=postgres
DB_PASSWORD=yourpassword
JWT_SECRET=your-super-secret-key-min-32-chars!!
JWT_EXPIRATION_MS=86400000
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=[PASSWORD]
ADMIN_FULL_NAME=Platform Admin
AUTH_SERVICE_URL=http://localhost:8081
```

---

## Running the Services

### Option A: Docker Compose *(Recommended)*

Starts PostgreSQL, Auth Service, and Task Service with a single command:

```bash
docker compose up --build
```

- Auth Service → `http://localhost:8081`
- Task Service → `http://localhost:8082`

To stop and remove volumes (clean slate):

```bash
docker compose down -v
```

> Docker Compose provisions the database, runs Flyway migrations, and seeds the admin user automatically.

---

### Option B: Local Maven Run

#### Step 1: Start PostgreSQL

```bash
# Ubuntu / Debian
sudo systemctl start postgresql

# macOS (Homebrew)
brew services start postgresql@15
```

Ensure the `tmpdb` database exists:

```bash
createdb -U postgres tmpdb
```

#### Step 2: Load Environment Variables

```bash
source .env
```

Or export individually:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/tmpdb
export DB_USER=postgres
export DB_PASSWORD=yourpassword
export JWT_SECRET=your-super-secret-key-min-32-chars!!
export JWT_EXPIRATION_MS=86400000
export ADMIN_EMAIL=admin@example.com
export ADMIN_PASSWORD=[PASSWORD]
export ADMIN_FULL_NAME=Platform Admin
export AUTH_SERVICE_URL=http://localhost:8081
```

#### Step 3: Build the Project

```bash
mvn clean package
```

Expected output:

```
[INFO] BUILD SUCCESS
[INFO] auth-service-1.0.0-SNAPSHOT.jar
[INFO] task-service-1.0.0-SNAPSHOT.jar
```

#### Step 4: Run Auth Service (Terminal 1)

```bash
source .env
mvn spring-boot:run -pl auth-service
```

Expected:

```
Started AuthServiceApplication in X seconds
Tomcat started on port(s): 8081
```

#### Step 5: Run Task Service (Terminal 2)

```bash
source .env
mvn spring-boot:run -pl task-service
```

Expected:

```
Started TaskServiceApplication in X seconds
Tomcat started on port(s): 8082
```

---

## API Endpoints

### Auth Service (Port 8081)

| Method | Endpoint | Auth Required | Notes |
|--------|----------|---------------|-------|
| `POST` | `/api/v1/auth/register` | No | Register new user (role: USER) |
| `POST` | `/api/v1/auth/login` | No | Returns JWT + refresh token |
| `POST` | `/api/v1/auth/refresh` | No | Rotate refresh token, get new JWT |
| `GET` | `/api/v1/users/me` | JWT | Own profile |
| `GET` | `/api/v1/users/{id}` | JWT | User profile by ID |
| `GET` | `/api/v1/users` | JWT (ADMIN) | List all users |

**Swagger UI:** `http://localhost:8081/swagger-ui.html`

---

### Task Service (Port 8082)

#### Projects

| Method | Endpoint | Auth Required | Notes |
|--------|----------|---------------|-------|
| `POST` | `/api/v1/projects` | JWT (ADMIN) | Create project |
| `GET` | `/api/v1/projects` | JWT | List all projects |
| `GET` | `/api/v1/projects/{id}` | JWT | Get project by ID |
| `PUT` | `/api/v1/projects/{id}` | JWT (ADMIN) | Update project |
| `DELETE` | `/api/v1/projects/{id}` | JWT (ADMIN) | Delete project |

#### Tasks

| Method | Endpoint | Auth Required | Notes |
|--------|----------|---------------|-------|
| `POST` | `/api/v1/projects/{pid}/tasks` | JWT | Create task (`assigneeUserId` optional) |
| `GET` | `/api/v1/projects/{pid}/tasks` | JWT | List tasks; optional `?status=TODO\|IN_PROGRESS\|DONE` |
| `GET` | `/api/v1/projects/{pid}/tasks/{tid}` | JWT | Get single task |
| `PUT` | `/api/v1/projects/{pid}/tasks/{tid}` | JWT | Update title, description, priority, dueDate |
| `PATCH` | `/api/v1/projects/{pid}/tasks/{tid}/assign` | JWT (ADMIN) | Assign task to user; body: `{ "assigneeUserId": "..." }` |
| `PATCH` | `/api/v1/projects/{pid}/tasks/{tid}/status` | JWT | Transition status; body: `{ "status": "..." }` |
| `DELETE` | `/api/v1/projects/{pid}/tasks/{tid}` | JWT (ADMIN) | Delete task |
| `GET` | `/api/v1/tasks/my-tasks` | JWT | All tasks assigned to authenticated user |
| `GET` | `/api/v1/tasks/overdue` | JWT | Tasks past their `dueDate` and not yet `DONE` |

**Valid status transitions:**

```
TODO → IN_PROGRESS
IN_PROGRESS → DONE
DONE → IN_PROGRESS  (re-open)
```

Any other transition returns `HTTP 400` with a descriptive error.

**Swagger UI:** `http://localhost:8082/swagger-ui.html`

---

## Security Model

| Role | Permissions |
|------|-------------|
| `ADMIN` | Full access to all endpoints in both services |
| `USER` | Can create and update tasks; read-only on projects; cannot delete any resource |
| Unauthenticated | Returns `HTTP 401` on any protected endpoint |
| Insufficient role | Returns `HTTP 403` |

**JWT Payload:**
```json
{
  "sub": "<userId UUID>",
  "role": "USER",
  "iat": 1718600000,
  "exp": 1718686400
}
```

**Refresh Token:**
- Stored in the `authdb.refresh_tokens` table
- Valid for **7 days**
- Rotated on every use (old token is revoked, new one is issued)
- Returns `HTTP 401` with a clear message if invalid, expired, or revoked

---

## Error Responses

All errors follow this consistent JSON structure:

```json
{
  "timestamp": "2026-06-17T10:00:00Z",
  "status": 404,
  "message": "Task not found",
  "path": "/api/v1/projects/xxx/tasks/yyy"
}
```

| Status Code | Scenario |
|-------------|----------|
| `400` | Validation failure, invalid status transition, missing required field |
| `401` | Missing/invalid/expired JWT or refresh token |
| `403` | Insufficient role/permissions |
| `404` | Resource not found (user, project, task) |
| `409` | Email already registered |
| `500` | Unexpected server error |

---

## Example Workflow

### 1. Register a User

```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"John Doe","email":"john@example.com","password":"Pass@1234"}'
```

### 2. Login

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"Pass@1234"}'

# Returns:
# { "token": "eyJ...", "refreshToken": "uuid", "expiresIn": 86400000, "user": {...} }
```

### 3. Refresh Access Token

```bash
curl -X POST http://localhost:8081/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token-from-login>"}'
```

### 4. Create a Project (ADMIN only)

```bash
TOKEN="<token from login>"
curl -X POST http://localhost:8082/api/v1/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Q3 Roadmap","description":"Features for Q3 2026"}'
```

### 5. Create a Task with Due Date

```bash
PROJECT_ID="<from project response>"
curl -X POST http://localhost:8082/api/v1/projects/$PROJECT_ID/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Build dashboard","priority":"HIGH","dueDate":"2026-12-31T23:59:59"}'
```

### 6. Transition Task Status

```bash
TASK_ID="<from task response>"
curl -X PATCH http://localhost:8082/api/v1/projects/$PROJECT_ID/tasks/$TASK_ID/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"status":"IN_PROGRESS"}'
```

### 7. Get Overdue Tasks

```bash
curl -X GET http://localhost:8082/api/v1/tasks/overdue \
  -H "Authorization: Bearer $TOKEN"
```