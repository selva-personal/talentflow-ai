# TalentFlow AI

**Your Personal AI Interview Coach** — enterprise-grade AI SaaS for resume ATS analysis, mock interviews, coding assessments, career roadmaps, and interview preparation.

## Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 19, TypeScript, Vite, Tailwind, React Query, Zustand, R3F, Framer Motion |
| Backend | Java 21, Spring Boot 3, Spring Security, JWT, JPA, PostgreSQL |
| AI | Google Gemini API |
| Deploy | **Netlify** (frontend), **Render** (backend), **PostgreSQL** (Supabase or Render) |

## Documentation

| Guide | Description |
|-------|-------------|
| [Local development](docs/LOCAL_DEVELOPMENT.md) | Docker Postgres, backend, frontend |
| [Environment variables](docs/ENVIRONMENT.md) | All env vars for local and production |
| [Deploy frontend (Netlify)](docs/deployment/NETLIFY.md) | Build settings, env, custom domain |
| [Deploy backend (Render)](docs/deployment/RENDER.md) | Web service, Docker, env |
| [Database](docs/deployment/DATABASE.md) | Supabase or Render PostgreSQL |
| [Deployment checklist](docs/DEPLOYMENT_CHECKLIST.md) | Netlify, Render, DB, Gemini |

## Quick start (local)

See [docs/LOCAL_DEVELOPMENT.md](docs/LOCAL_DEVELOPMENT.md) for the full guide.

```bash
# 1. Database
docker compose up -d

# 2. Backend
cd backend/talentflow-api
# Set env vars (see .env.example) then:
./mvnw spring-boot:run

# 3. Frontend
cd frontend/talentflow-web
cp .env.example .env   # VITE_API_BASE_URL=http://localhost:8080/api/v1
npm install && npm run dev
```

- **App:** http://localhost:5173  
- **API:** http://localhost:8080/api/v1  
- **Swagger:** http://localhost:8080/swagger-ui.html  

## Project structure

```
TalentFlow AI/
├── backend/talentflow-api/   # Spring Boot API (Render)
├── frontend/talentflow-web/  # React SPA (Netlify)
├── docs/                     # Deployment & environment guides
├── docker-compose.yml        # Local PostgreSQL
└── .env.example              # Variable reference (no secrets)
```

## Default roles

- `USER` — standard access  
- `ADMIN` — admin endpoints  

Register via `POST /api/v1/auth/register`; new accounts receive the `USER` role.

## License

Proprietary — TalentFlow AI © 2026
