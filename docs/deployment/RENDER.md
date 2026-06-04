# Deploy backend to Render

TalentFlow API is a **Spring Boot 3** application deployed on **Render** using the included **Dockerfile**.

## Prerequisites

- PostgreSQL database (Render PostgreSQL or Supabase) — see [DATABASE.md](DATABASE.md)
- Gemini API key
- Netlify frontend URL for CORS

## Step-by-step

### 1. PostgreSQL

Create a database on Render or Supabase. Note JDBC URL, username, and password.

### 2. Web service

1. [Render Dashboard](https://dashboard.render.com) → **New +** → **Web Service**
2. Connect the Git repository
3. **Root directory:** `backend/talentflow-api`
4. **Environment:** Docker (uses `Dockerfile` in that directory)
5. **Instance type:** Free or paid as needed

Alternatively deploy with **Blueprint** from `render.yaml` in `backend/talentflow-api`.

### 3. Environment variables

Set in **Environment** for the web service:

| Key | Example / notes |
|-----|-----------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://...` (see DATABASE.md) |
| `SPRING_DATASOURCE_USERNAME` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | DB password |
| `JWT_SECRET` | Random 32+ character string (Render can generate) |
| `JWT_ACCESS_EXPIRATION_MS` | `900000` |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` |
| `GEMINI_API_KEY` | From Google AI Studio |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,https://YOUR_NETLIFY_DOMAIN.netlify.app` |
| `FILE_UPLOAD_DIR` | `./uploads` |

Render sets `PORT` automatically — do not hardcode in the app.

### 4. Health check

Configure health check path: `/actuator/health` (see `render.yaml`).

### 5. Deploy

After deploy, note the service URL:

```
https://talentflow-api.onrender.com
```

API base for the frontend:

```
https://talentflow-api.onrender.com/api/v1
```

Set this as `VITE_API_BASE_URL` on Netlify and redeploy the frontend.

### 6. Verify

```bash
curl https://YOUR_SERVICE.onrender.com/actuator/health
```

Swagger (optional): `https://YOUR_SERVICE.onrender.com/swagger-ui.html`

## Docker build

The `Dockerfile` multi-stage build:

1. JDK 21 — Maven package
2. JRE 21 — run `app.jar`

## Persistent uploads

Free web services have **ephemeral disk**. Resume files in `./uploads` may be lost on restart. For production, plan object storage (S3, Supabase Storage) in a future iteration.

## render.yaml

Optional Infrastructure-as-Code at `backend/talentflow-api/render.yaml`. Sync env vars in the dashboard if not using a blueprint.

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Flyway / DB errors | Check JDBC URL, SSL (`?sslmode=require` for Supabase) |
| CORS from Netlify | Add exact Netlify URL to `CORS_ALLOWED_ORIGINS`, redeploy |
| 502 on cold start | Free tier spins down; first request may be slow |
| AI errors | Verify `GEMINI_API_KEY` |
