# Environment variables

Reference for **local**, **Render (backend)**, and **Netlify (frontend)**.  
Copy from the repo root [`.env.example`](../.env.example) — never commit real secrets.

## Backend (Render / local shell)

| Variable | Required | Description |
|----------|----------|-------------|
| `SPRING_DATASOURCE_URL` | Yes (prod) | JDBC URL, e.g. `jdbc:postgresql://HOST:5432/DB?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Yes (prod) | Database user |
| `SPRING_DATASOURCE_PASSWORD` | Yes (prod) | Database password |
| `JWT_SECRET` | Yes (prod) | HMAC secret, minimum 32 characters |
| `JWT_ACCESS_EXPIRATION_MS` | No | Access token TTL (default `900000` = 15 min) |
| `JWT_REFRESH_EXPIRATION_MS` | No | Refresh token TTL (default `604800000` = 7 days) |
| `GEMINI_API_KEY` | Yes (AI features) | Google Gemini API key |
| `CORS_ALLOWED_ORIGINS` | Yes (prod) | Comma-separated browser origins (no trailing paths) |
| `FILE_UPLOAD_DIR` | No | Resume upload directory (default `./uploads`) |
| `PORT` | No | HTTP port (Render sets automatically) |

### CORS example (local + Netlify)

```bash
CORS_ALLOWED_ORIGINS=http://localhost:5173,https://talentflow-ai.netlify.app
```

Add every Netlify URL you use (production, branch previews, custom domain):

```bash
CORS_ALLOWED_ORIGINS=http://localhost:5173,https://main--talentflow-ai.netlify.app,https://talentflow.ai
```

Spring Boot reads this via `application.yml` → `talentflow.cors.allowed-origins`.  
`SecurityConfig` splits on commas and trims whitespace.

## Frontend (Netlify / local)

| Variable | Required | Description |
|----------|----------|-------------|
| `VITE_API_BASE_URL` | Yes (prod build) | Full API base including `/api/v1` |

### Examples

| Environment | `VITE_API_BASE_URL` |
|-------------|---------------------|
| Local | `http://localhost:8080/api/v1` |
| Production | `https://talentflow-api.onrender.com/api/v1` |

Vite only exposes variables prefixed with `VITE_`. They are baked in at **build time** on Netlify — change the variable and **trigger a new deploy** after updating the backend URL.

## API URL validation

| Check | Local | Production |
|-------|-------|------------|
| Frontend calls | `http://localhost:8080/api/v1` | `https://<render-service>.onrender.com/api/v1` |
| Trailing slash | Omit on base URL | Omit on base URL |
| HTTPS | Optional locally | Required |
| CORS | `http://localhost:5173` in `CORS_ALLOWED_ORIGINS` | Netlify site URL(s) in same variable |

## Supabase PostgreSQL (JDBC)

Use the connection string from Supabase → Settings → Database:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://db.PROJECT_REF.supabase.co:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<your-password>
```

## Render PostgreSQL (internal URL)

From Render dashboard → PostgreSQL → Connections:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:5432/talentflow
```

Use the **internal** URL when the API runs on Render in the same region.
