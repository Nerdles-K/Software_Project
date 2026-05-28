# CLAUDE.md — VisiTalk Project Context

## Project Overview
VisiTalk is a dual-mode (child/parent) web application that helps autistic children (ages 3-10) communicate through visual PECS cards and structured schedules, while enabling parents to log behavior events and view trends.

- **Deadline**: June 15, 2026 (course project presentation)
- **Team**: Xu Ziyang (PO/full-stack), Xu Zihe (backend/data), Yuen KinNing (frontend/UX), Ke Hongyi (SM/QA)
- **Docs**: See `doc/` folder for PRD, Architecture, Progress, and Workflow documents

## Tech Stack
- **Frontend**: Vue 3 (Composition API, TypeScript) + Vite + Pinia + Tailwind CSS + vue-draggable-plus
- **Backend**: Spring Boot 3 (Java 17, Gradle) + REST + JWT
- **Database**: Supabase (PostgreSQL 15) + Row Level Security
- **Deploy**: Vercel (frontend), Fly.io (backend)

## Directory Structure
```
frontend/     Vue 3 SPA (port 3000)
  src/views/child/   pecs/ schedule/ diary/
  src/views/parent/  pecs/ schedule/ behavior/
  src/stores/        Pinia stores
  src/router/        Vue Router config
  src/locales/       i18n (zh.ts)

backend/      Spring Boot 3 API (port 8080)
  com.visitalk.pecs/
  com.visitalk.schedule/
  com.visitalk.behavior/
  com.visitalk.report/
  com.visitalk.config/
  com.visitalk.security/
```

## Development Commands
- Frontend dev: `cd frontend && npm run dev` (http://localhost:3000)
- Frontend build: `cd frontend && npm run build`
- Frontend test: `cd frontend && npx vitest run`
- Backend dev: `cd backend && ./gradlew bootRun` (http://localhost:8080)
- Backend build: `cd backend && ./gradlew build -x test`
- Backend test: `cd backend && ./gradlew test`

## Key Design Constraints
- **Child mode**: No text-heavy UI, no sound, contrast ≥ 4.5:1, max 2 nav levels, touch targets ≥ 100px
- **Parent mode**: Professional dark UI, PIN-gated (pin: 1234 for dev)
- **RLS Privacy**: child session cannot access BehaviorEvent; parent cannot access DiaryEntry details
- **i18n**: All user-facing strings go through vue-i18n keys, never hardcoded

## Sprint Schedule (4-week course cycle)
- Sprint 0: May 13-26 (infrastructure — current)
- Sprint 1: May 27 - Jun 9 (MVP: A-1/A-2/A-4/B-1/B-2, 31 SP)
- Sprint 2: Jun 10-15 (enhancements: C-1/C-2/B-3 + wrap-up)

## Branch & Commit Convention
- Branch: `feat/<story-id>-<slug>` | `fix/<bug-id>` | `chore/<slug>`
- Commit: `<type>(<scope>): <subject>` — scope: pecs|schedule|behavior|report
- PR: must include Story ID, what/why/how-to-test/screenshots
