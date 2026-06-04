# PostgreSQL setup (Supabase or Render)

TalentFlow uses **PostgreSQL** with **Flyway** migrations (`V1__init_schema.sql`). Hibernate `ddl-auto` is `validate` — schema changes go through Flyway only.

## Option 1 — Render PostgreSQL

1. Render → **New +** → **PostgreSQL**
2. Name: `talentflow-db` (example)
3. Copy **Internal Database URL** for the API on Render
4. Convert to JDBC for Spring:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:5432/talentflow
SPRING_DATASOURCE_USERNAME=<from dashboard>
SPRING_DATASOURCE_PASSWORD=<from dashboard>
```

Use the **internal** hostname when the API runs on Render in the same account/region.

## Option 2 — Supabase

1. [Supabase](https://supabase.com) → New project
2. **Settings → Database** → connection string (URI)
3. JDBC format:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://db.PROJECT_REF.supabase.co:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<your-database-password>
```

Supabase requires SSL — include `sslmode=require`.

## Option 3 — Local Docker

```bash
docker compose up -d
```

From project root `docker-compose.yml`:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/talentflow
SPRING_DATASOURCE_USERNAME=talentflow
SPRING_DATASOURCE_PASSWORD=talentflow_secret
```

## Migrations

On API startup, Flyway runs migrations automatically. No manual SQL required for initial deploy.

## Connection pool

HikariCP defaults in `application.yml` (`maximum-pool-size: 20`). Adjust for your Render/Supabase plan limits if needed.

## Security

- Never commit passwords; use Render/Supabase secrets
- Restrict database network access (Supabase IP allowlist, Render private services)
- Rotate credentials periodically
