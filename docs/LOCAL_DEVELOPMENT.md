# Local development guide

## Prerequisites

- Java 21
- Node.js 20+
- Docker (optional) **or** embedded PostgreSQL via `dev` profile (no Docker required)
- Gemini API key ([Google AI Studio](https://aistudio.google.com/apikey))

## 1. Database

### Option A — Docker (recommended)

```bash
cd "/path/to/TalentFlow AI"
docker compose up -d
```

Defaults match `docker-compose.yml`:

| Setting | Value |
|---------|-------|
| Host | `localhost:5432` |
| Database | `talentflow` |
| User | `talentflow` |
| Password | `talentflow_secret` |

### Option B — Remote PostgreSQL

Set `SPRING_DATASOURCE_*` to your Supabase or Render connection string. See [DATABASE.md](deployment/DATABASE.md).

## 2. Backend

```bash
cd backend/talentflow-api
```

Export environment variables (example for Docker Postgres):

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/talentflow
export SPRING_DATASOURCE_USERNAME=talentflow
export SPRING_DATASOURCE_PASSWORD=talentflow_secret
export JWT_SECRET=local-dev-secret-minimum-32-characters-long
export GEMINI_API_KEY=your-gemini-api-key-from-aistudio
export CORS_ALLOWED_ORIGINS=http://localhost:5173
export FILE_UPLOAD_DIR=./uploads
```

Run (from `backend/talentflow-api`):

```bash
# Easiest — embedded PostgreSQL on port 5433 (no Docker)
../../scripts/run-backend.sh

# Or manually:
export SPRING_PROFILES_ACTIVE=dev
set -a && source .env && set +a
./mvnw spring-boot:run
```

> **Note:** Run Maven from `backend/talentflow-api`, not `backend/`.  
> Without Docker, use profile `dev`. With Docker (`docker compose up -d`), omit `dev` and use `.env` credentials on port 5432.

Verify:

- Health: http://localhost:8080/actuator/health  
- Swagger: http://localhost:8080/swagger-ui.html  
- API base: http://localhost:8080/api/v1  

Flyway applies migrations on startup.

## 3. Frontend

```bash
cd frontend/talentflow-web
cp .env.example .env
```

Set in `.env`:

```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

```bash
npm install
npm run dev
```

Open http://localhost:5173 — register a user and test modules.

## 4. CORS (local)

Backend must include the Vite dev origin:

```bash
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

If you change the Vite port in `vite.config.ts`, add that origin too.

## Troubleshooting

| Issue | Fix |
|-------|-----|
| CORS error in browser | Add frontend origin to `CORS_ALLOWED_ORIGINS` and restart API |
| `Gemini API key is not configured` | Set `GEMINI_API_KEY` |
| DB connection refused | `docker compose ps`, check JDBC URL |
| 401 on API calls | Log in again; check `VITE_API_BASE_URL` in `.env` |

## Production parity

Local uses the same env var **names** as Render/Netlify. See [ENVIRONMENT.md](ENVIRONMENT.md) and [DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md).
