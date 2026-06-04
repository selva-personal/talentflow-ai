# Deployment checklist

Use this list when going from local development to **Netlify + Render + PostgreSQL + Gemini**.

---

## PostgreSQL

- [ ] Database created (Render PostgreSQL **or** Supabase)
- [ ] JDBC URL, username, password recorded securely
- [ ] SSL configured for Supabase (`sslmode=require` in JDBC URL)
- [ ] API can reach DB (internal URL on Render, or public with firewall rules)

---

## Gemini API

- [ ] API key created in [Google AI Studio](https://aistudio.google.com/apikey)
- [ ] `GEMINI_API_KEY` set on Render (never in frontend)
- [ ] Test endpoint after deploy (e.g. resume upload or mock interview)

---

## Render (backend)

- [ ] Web service created from `backend/talentflow-api` (Docker)
- [ ] `SPRING_DATASOURCE_URL`, `USERNAME`, `PASSWORD` set
- [ ] `JWT_SECRET` set (32+ chars, unique per environment)
- [ ] `JWT_ACCESS_EXPIRATION_MS=900000`
- [ ] `JWT_REFRESH_EXPIRATION_MS=604800000`
- [ ] `GEMINI_API_KEY` set
- [ ] `FILE_UPLOAD_DIR=./uploads`
- [ ] `CORS_ALLOWED_ORIGINS` includes:
  - [ ] `http://localhost:5173` (for local dev against prod API, optional)
  - [ ] `https://YOUR_NETLIFY_DOMAIN.netlify.app`
  - [ ] Custom domain if used (`https://yourdomain.com`)
- [ ] Health check: `/actuator/health` returns UP
- [ ] Service URL noted: `https://______.onrender.com`
- [ ] API base for frontend: `https://______.onrender.com/api/v1`

---

## Netlify (frontend)

- [ ] Site connected to Git repo
- [ ] Base directory: `frontend/talentflow-web`
- [ ] Build: `npm run build`, publish: `dist`
- [ ] `VITE_API_BASE_URL=https://______.onrender.com/api/v1` set for **Production**
- [ ] Deploy succeeded; SPA redirects work (`/login`, `/app/dashboard`)
- [ ] Netlify URL noted: `https://______.netlify.app`

---

## Cross-service verification

- [ ] Render `CORS_ALLOWED_ORIGINS` updated with **exact** Netlify URL (no path suffix)
- [ ] Render redeployed after CORS change
- [ ] Netlify redeployed after `VITE_API_BASE_URL` change
- [ ] Register → login → dashboard works in production
- [ ] Resume upload / AI feature returns data (not CORS / 401 / 500)
- [ ] Browser devtools: API requests go to Render HTTPS URL, not `localhost`

---

## Security (production)

- [ ] No secrets in Git
- [ ] `.env` files gitignored
- [ ] `JWT_SECRET` not shared with development
- [ ] Swagger restricted or disabled if exposing public internet (optional hardening)

---

## CI (optional)

- [ ] `.github/workflows/ci.yml` passes on main branch
- [ ] `VITE_API_BASE_URL` set in CI for frontend build

---

## Quick reference

| Platform | Key variable |
|----------|----------------|
| Netlify | `VITE_API_BASE_URL` |
| Render | `CORS_ALLOWED_ORIGINS`, `SPRING_DATASOURCE_*`, `JWT_SECRET`, `GEMINI_API_KEY` |

Full details: [ENVIRONMENT.md](ENVIRONMENT.md)
