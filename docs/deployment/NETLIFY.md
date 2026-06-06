# Deploy frontend to Netlify

TalentFlow AI frontend is a **Vite + React** SPA deployed on **Netlify**. The API runs separately on **Render**.

## Prerequisites

- Git repository connected to Netlify
- Render backend deployed (see [RENDER.md](RENDER.md))
- `CORS_ALLOWED_ORIGINS` on Render includes your Netlify URL

## Repository layout

This monorepo includes **`netlify.toml` at the repository root** (required). Netlify only reads a nested `frontend/talentflow-web/netlify.toml` when the UI **Base directory** is already set to that folder — if the site is connected at repo root without a root config, builds and publish paths are wrong and `/` returns Netlify’s 404 page.

Root `netlify.toml` settings:

| Setting | Value |
|---------|-------|
| Build command | `npm run build` |
| Publish directory | `dist` |
| Node version | 20 |

## Step-by-step

### 1. Create site

1. [Netlify](https://app.netlify.com) → **Add new site** → **Import an existing project**
2. Connect Git provider and select the TalentFlow AI repository
3. **Production branch:** `develop` ( **`main` only contains README/LICENSE** — deploying `main` will 404)
4. Leave **Base directory**, **Build command**, and **Publish directory** empty in the UI so root `netlify.toml` applies, **or** set:
   - **Base directory:** `frontend/talentflow-web`
   - **Build command:** `npm run build`
   - **Publish directory:** `dist` (relative to base — **not** repo root)

### 2. Environment variables

**Site settings → Environment variables → Production** (and Deploy Previews if needed):

| Key | Value |
|-----|-------|
| `VITE_API_BASE_URL` | `https://YOUR_RENDER_SERVICE.onrender.com/api/v1` |

Example:

```
VITE_API_BASE_URL=https://talentflow-api.onrender.com/api/v1
```

No trailing slash on the URL. Must include `/api/v1`.

### 3. Deploy

Trigger **Deploy site**. Netlify runs `npm run build` and publishes `dist`.

### 4. Update backend CORS

On Render, set:

```bash
CORS_ALLOWED_ORIGINS=http://localhost:5173,https://YOUR_SITE.netlify.app
```

Redeploy the API after changing CORS.

### 5. Custom domain (optional)

Netlify → **Domain management** → add domain.  
Add the custom domain to `CORS_ALLOWED_ORIGINS` on Render (e.g. `https://app.talentflow.ai`).

## SPA routing

Client-side routes use React Router. Netlify is configured via:

- `netlify.toml` → `[[redirects]]` to `/index.html`
- `public/_redirects` → `/* /index.html 200` (backup)

## Branch deploys & previews

Preview URLs look like `https://deploy-preview-123--site.netlify.app`.  
Either:

- Add each preview origin to `CORS_ALLOWED_ORIGINS`, or  
- Use a wildcard-friendly approach only if you accept the security tradeoff (not recommended for production APIs).

For previews, add:

```bash
CORS_ALLOWED_ORIGINS=http://localhost:5173,https://YOUR_SITE.netlify.app,https://deploy-preview-*--YOUR_SITE.netlify.app
```

Note: Spring CORS does **not** support wildcards in origins when `allowCredentials` is true. Add specific preview URLs or use a single preview origin pattern via Netlify’s primary URL for testing.

## Build troubleshooting

| Error | Solution |
|-------|----------|
| **404 on `/` (Netlify “Page not found”)** | Deploy branch must be **`develop`**. Publish must be **`dist`** (via root `netlify.toml` or UI base `frontend/talentflow-web`). Do not publish repo root — there is no `index.html` there. |
| `VITE_API_BASE_URL is not set` | Add env var in Netlify UI and redeploy |
| 404 on `/login` refresh | Confirm `_redirects` / `netlify.toml` redirects |
| API network errors | Check CORS on Render and HTTPS API URL |

## CI

GitHub Actions builds the frontend with `VITE_API_BASE_URL` set — see `.github/workflows/ci.yml`.
