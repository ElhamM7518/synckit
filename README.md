# SyncKit

Local-first sync engine (Spring Boot) with Atelier, a studio-wall client (React + TypeScript).

Current slice: JWT auth and shared walls (create, list, join by token). The live board comes next.

## Stack

- Backend: Java 21, Spring Boot 4, Spring Security, JPA, Flyway, PostgreSQL
- Frontend: React 19, TypeScript, Vite, React Router, TanStack Query, Zustand
- Infra: Docker Compose (Postgres 16 on host port **5433**)

## Run

Postgres:

```bash
docker compose up -d
```

API:

```bash
cd backend
./mvnw spring-boot:run
```

On Windows use `.\mvnw.cmd spring-boot:run`.

UI:

```bash
cd frontend
nvm use 20
npm install
npm run dev
```

- App: http://127.0.0.1:5173
- Health: http://localhost:8080/api/health
- Postgres: `localhost:5433` (user / password / db: `synckit`)

This project uses the Maven Wrapper and `backend/.mvn/central-settings.xml` so builds resolve from Maven Central (Google mirror) instead of a machine-wide `~/.m2/settings.xml`.

Local JWT signing uses `JWT_SECRET` when set, otherwise a development default in `application.properties`. Change that before any shared or production deploy.

## Auth API

| Method | Path | Auth |
|---|---|---|
| POST | `/api/auth/register` | public |
| POST | `/api/auth/login` | public |
| GET | `/api/health` | public |

## Walls API

All walls routes need `Authorization: Bearer <accessToken>`.

| Method | Path | Notes |
|---|---|---|
| POST | `/api/walls` | Create a wall; caller becomes `OWNER` and receives `shareToken` |
| GET | `/api/walls` | Walls the caller belongs to |
| GET | `/api/walls/{id}` | Members only. `shareToken` is returned for owners, omitted for editors |
| POST | `/api/walls/join` | Body `{ "shareToken": "..." }`; caller becomes `EDITOR` (idempotent if already a member) |

Create body: `{ "name": "Darkroom" }`. Names are trimmed; max 100 characters.
