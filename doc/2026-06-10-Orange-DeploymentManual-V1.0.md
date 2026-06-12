# VisiTalk — Deployment Manual

This manual explains **how and where to retrieve the application code**, how to **run it locally**, and where the **live deployment** is hosted.

---

## 1. Live Application (no install required)

> 👉 **To use the app, open the FRONTEND URL in your browser:**
> ## https://frontend-nine-lyart-85.vercel.app

| Component | URL | What it is / how to open it |
| --- | --- | --- |
| **Frontend (web app)** | https://frontend-nine-lyart-85.vercel.app | **This is the application.** Open it in a browser — register, log in, and use Child / Parent mode. |
| **Backend API — health check** | https://software-project-m0jm.onrender.com/api/health | Returns `{"status":"UP",...}`. Use this *only* to confirm the API is alive. |
| **Backend API — root** | ~~https://software-project-m0jm.onrender.com~~ | ⚠️ **Do NOT open this in a browser.** It is a headless REST API with no homepage — the root path returns **HTTP 403 ("Forbidden")**, which is **normal and expected**, not an error. |
| **Database** | Neon — PostgreSQL 17 (eu-central-1) | Managed, not publicly exposed. |

> ⚠️ **First load may take 30–90 s.** The Render free tier sleeps after ~15 min idle; the first request wakes it (cold start). Just wait and refresh.
>

---

## 2. Source Code (Git)

The full source code is hosted on GitHub:

**Repository:** https://github.com/Nerdles-K/Software_Project

Clone it:

```bash
git clone https://github.com/Nerdles-K/Software_Project.git
cd Software_Project
```

**Repository layout:**

```
Software_Project/
├── frontend/     Vue 3 + Vite SPA          (runs on port 3000)
├── backend/      Spring Boot 3 REST API    (runs on port 8080)
├── doc/          PRD, Architecture, Progress, this manual
├── start.sh      one-command local launcher (macOS)
├── Dockerfile    container build for Render (repo-root context)
└── render.yaml   Render blueprint
```

---

## 3. Prerequisites

| Tool | Version | Used by |
| --- | --- | --- |
| **Node.js** | 18 LTS or newer (with npm) | Frontend |
| **Java JDK** | 17 | Backend |
| **PostgreSQL** | 15 or newer (17 recommended) | Database |
| **Git** | any recent | Cloning |

> Gradle is **not** required globally — the project ships the Gradle wrapper (`./gradlew`).

---

## 4. Quick Start — macOS (one command)

A helper script provisions the database, starts the backend, and starts the frontend:

```bash
cd Software_Project
./start.sh
```

It will:
1. Start PostgreSQL 17 (via Homebrew) and create the `visitalk` database + load `schema.sql` if missing.
2. Start the Spring Boot backend on **http://localhost:8080**.
3. Start the Vite frontend on **http://localhost:3000** and open it in your browser.

Press **Ctrl+C** to stop all services.

> Pre-seeded test accounts referenced by the script: `parent@test.com / password123` and `child@test.com / password123` (only valid if your DB already contains them — otherwise register a new account).

---

## 5. Manual Start (any OS)

### 5.1 Database

Create a PostgreSQL database and user, then load the schema (11 tables):

```bash
psql postgres -c "CREATE USER visitalk WITH PASSWORD 'visitalk123';"
psql postgres -c "CREATE DATABASE visitalk OWNER visitalk;"
psql -U visitalk -d visitalk -f backend/src/main/resources/schema.sql
```

### 5.2 Backend (Spring Boot, port 8080)

```bash
cd backend
./gradlew bootRun          # Windows: gradlew.bat bootRun
```

The backend reads all settings from environment variables, with local defaults:

| Variable | Default (local) | Purpose |
| --- | --- | --- |
| `PORT` | `8080` | API port |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/visitalk` | JDBC URL |
| `DATABASE_USERNAME` | `visitalk` | DB user |
| `DATABASE_PASSWORD` | `visitalk123` | DB password |
| `JWT_SECRET` | dev secret (change in prod) | JWT signing key (≥ 256 bits) |
| `JWT_EXPIRATION_MS` | `86400000` (24 h) | Token lifetime |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,https://visitalk.vercel.app` | Allowed frontends |

Verify it is up:

```bash
curl http://localhost:8080/api/health
```

### 5.3 Frontend (Vue 3 + Vite, port 3000)

```bash
cd frontend
cp .env.example .env        # then edit if needed
npm install
npm run dev                 # http://localhost:3000
```

The only frontend variable:

| Variable | Default | Purpose |
| --- | --- | --- |
| `VITE_API_BASE_URL` | `http://localhost:8080` | Backend API base URL |

Production build:

```bash
npm run build               # outputs to frontend/dist
npm run preview             # serve the build locally
```

---

## 6. Production Deployment (how the live site is built)

| Layer | Platform | How it builds |
| --- | --- | --- |
| Frontend | **Vercel** | Auto-builds `frontend/` on push to `main`; SPA rewrite via `frontend/vercel.json`; set build env `VITE_API_BASE_URL` to the Render backend URL |
| Backend | **Render** (Docker web service) | Builds the **root `Dockerfile`** (Render uses repo-root build context); blueprint in `render.yaml`; secrets set as env vars in the Render dashboard |
| Database | **Neon** (Postgres 17) | Connection via `DATABASE_URL` / `DATABASE_USERNAME` / `DATABASE_PASSWORD`; schema loaded from `backend/src/main/resources/schema.sql` |

> CORS uses `allowedOriginPatterns` and always permits `https://*.vercel.app`, so Vercel **preview** URLs work without changing backend env vars.

---

## 7. Known Constraints

- **Cold start:** Render free tier sleeps after ~15 min idle → first request takes 30–90 s.
- **Uploaded images are ephemeral:** custom PECS photos are stored on Render's ephemeral disk and are **lost on redeploy** (a persistent object store such as Supabase Storage / Cloudinary is the production fix).
- **Empty database on first deploy:** register an account before attempting to log in.

---

## 8. Troubleshooting

| Symptom | Cause | Fix |
| --- | --- | --- |
| Login fails / 401 on every request | Empty DB, or stale token in `localStorage` | Register a fresh account; clear site data |
| Frontend can't reach API (CORS / network error) | Wrong `VITE_API_BASE_URL`, or backend asleep | Point to the correct backend URL; wait for cold start |
| `port 8080 already in use` | A previous backend is still running | `lsof -ti:8080 \| xargs kill -9` |
| Custom card images disappear | Render redeploy wiped ephemeral disk | Re-upload, or wire a persistent object store |
| `gradlew: permission denied` | Wrapper not executable | `chmod +x backend/gradlew` |
