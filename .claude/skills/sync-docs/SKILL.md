---
name: sync-docs
description: Sync the doc/ folder (PRD, Architecture, Progress, Work Packages, Workflow) with the current code state. Use after code changes to update docs without manual diffing — detects what changed, identifies affected docs, proposes a plan, and writes only after user OK. Invoke as /sync-docs or /sync-docs <git-ref>.
---

VisiTalk has 5 Chinese-language markdown docs in `doc/` that must stay in sync with code. They are reviewed by the team (course stakeholders), so edits must be precise and preserve the existing voice/format.

## The 5 docs and what each owns

| Doc | What lives here | Don't touch unless… |
| --- | --- | --- |
| `📦 VisiTalk — Product Requirements Document (PRD).md` | Features, user stories, AC (acceptance criteria), UX principles | Story scope/AC actually changed |
| `VisiTalk — 项目架构文档 (Architecture).md` | Modules, tech stack, API surface, data model, deployment topology | New endpoint / new model / changed module / new dependency |
| `VisiTalk — 项目进度文档 (Progress).md` | Milestones, sprint board, story status, SP burn, risks | Story status flipped (🟡→🟢), sprint date passed, new risk |
| `VisiTalk — 工作包拆分 (Work Packages).md` | Story breakdown, SP estimates, owner assignments | New story, scope split/merge, owner reassignment |
| `VisiTalk — Workflow 文档 (开发与测试).md` | Branching, commit convention, test commands, CI | Build/test command changed, new tool, new CI step |

## Workflow (always follow this order)

### 1. Pick the diff window

- `/sync-docs` (no arg) → diff against the **earliest** `最后更新` date among the 5 docs. Get it by `grep -h "最后更新" doc/*.md`.
- `/sync-docs <git-ref>` → diff against `<git-ref>` (commit/tag/branch).
- `/sync-docs --since=YYYY-MM-DD` → date-based.

Collect changes:
```
git status --short                              # uncommitted
git log --since="<date>" --pretty=format:"%h %s" --name-status
git diff <ref>..HEAD --stat
```
Read commit messages first — they usually say what intent the change had. Open files only when commit messages are ambiguous.

### 2. Categorize changes → docs

For each change, decide which doc(s) it touches. Examples:
- New `*Controller.java` or new `/api/...` endpoint → **Architecture** §API + maybe §模块, AND **PRD** if user-visible
- New `*Repository.java`, new entity → **Architecture** §数据模型
- New Vue view under `views/child/` or `views/parent/` → **Architecture** §前端模块 + **PRD** §功能
- `schema.sql` changed → **Architecture** §数据模型
- Story marked done in commit msg (e.g. `feat(pecs): A-2 ...`) → **Progress** §sprint board + **Work Packages** status column
- `build.gradle` / `package.json` dependency added → **Architecture** §技术选型 + **Workflow** if it changes a command
- New test file or test script → **Workflow** §测试

If a change touches nothing user-facing (rename, internal refactor), say so — don't fabricate a doc edit.

### 3. Show the plan FIRST, then write

Present a compact table to the user:

```
Doc                  | Section        | Edit summary
---------------------|----------------|-----------------------------------
Architecture v1.6→1.7| §5.2 API 端点  | + GET /api/sentences, + POST ...
Progress v1.7→1.8    | §4 当前 Sprint | A-2 🟡→🟢, SP 39→44
Work Packages        | §A Epic        | A-5 状态：进行中 → 完成
```

Wait for explicit user OK. **Do not silently rewrite paragraphs.**

### 4. When writing the edits

For every doc you touch:
- Bump `文档版本` — patch (v1.7→v1.7.1) for typos/tiny fact fixes, minor (v1.7→v1.8) for new content
- Update `最后更新` to today's date
- Update `状态` line if Epic completion shifted
- Preserve Chinese voice, table format, emoji legend (🟢 完成 / 🟡 进行中 / ⬜ 未开始)
- Use `Edit` for targeted changes; only `Write` if a section was actually rewritten end-to-end
- Don't introduce new top-level sections without flagging it in the plan

### 5. After writing

- Summarise in 2–3 lines: "edited X docs, bumped versions, A→B story flipped". No essays.
- Do **not** auto-commit. The user reviews and commits docs themselves.

## Anti-patterns (read once, internalize)

- ❌ Don't touch PRD user-stories text unless the user explicitly says a story scope changed — those are reviewed externally.
- ❌ Don't restructure or reorder sections — only edit content inside existing sections.
- ❌ Don't add docs to `doc/` without asking.
- ❌ Don't translate Chinese sections to English or vice versa.
- ❌ Don't auto-bump version to `v2.0` — keep the small-step bumping convention.
- ❌ Don't read every doc front-to-back every time — use grep to locate the affected section, then read just that span.

## Source-of-truth files to consult

- `CLAUDE.md` — project overview, sprint schedule, branch/commit convention
- `backend/src/main/resources/schema.sql` — current data model
- `backend/src/main/java/com/visitalk/**/*Controller.java` — current API surface
- `frontend/src/router/` + `frontend/src/views/**` — current route map and pages
- `git log` — what was *intended* (commit messages > code reading when both available)
