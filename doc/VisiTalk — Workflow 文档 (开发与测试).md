# 🔄 VisiTalk — Workflow 文档 (开发与测试流程)

| 项目        | VisiTalk — 自闭症儿童可视化沟通与情绪追踪平台 |
| --------- | ----------------------------- |
| 文档版本      | v1.5                          |
| 文档负责人     | Ke Hongyi (SM/QA) · Xu Ziyang (架构) |
| 最后更新      | 2026-06-13                    |
| 关联文档      | PRD、项目架构文档、项目进度文档            |

---

## 1. 文档目的 (Purpose)

本文档定义团队在一个 Story 的完整生命周期中**如何开发、如何测试**，确保流程可重复、质量可度量。所有成员遵循同一套分支、评审、测试与发布约定。

---

## 2. 总流程：一个 Story 的生命周期

```
Backlog ──► Sprint Backlog ──► In Progress ──► Review ──► Done
   │             │                 │             │          │
 (PO 排序)   (Planning 选入)     (开发+自测)    (PR+评审+CI)  (满足 DoD)
              满足 DoR                                    部署 staging
```

| 阶段             | 触发条件         | 主要动作                                |
| -------------- | ------------ | ----------------------------------- |
| Backlog        | PRD 录入       | PO 排序优先级                            |
| Sprint Backlog | Sprint Planning | 满足 DoR 的 Story 被选入；拆技术子任务           |
| In Progress    | 开发者认领        | 建分支、编码、写测试、本地自测                     |
| Review         | 提交 PR        | CI 跑全套检查；≥1 人 Code Review           |
| Done           | 满足 DoD       | 合入 main、部署 staging、PO 在 Review 接受    |

---

## 2.1 本地开发环境

### 一键启动
```bash
bash start.sh
```
自动完成：PostgreSQL 检查 → 清端口 → 启动后端 (8080) → 启动前端 (3000) → 打开浏览器。

### 手动启动

| 组件 | 启动命令 | 端口 | 依赖 |
|------|---------|------|------|
| PostgreSQL | `brew services start postgresql@17` | 5432 | 首次需建库 |
| 后端 | `cd backend && ./gradlew bootRun` | 8080 | PostgreSQL |
| 前端 | `cd frontend && npm run dev` | 3000 | 后端 :8080 |

- 数据库：PostgreSQL 17 本地实例 (`localhost:5432/visitalk`, 用户 `visitalk/visitalk123`)。
- 表结构：`backend/src/main/resources/schema.sql`（8 张表），JPA `validate` 模式校验。
- 启动时 `DataInitializer` 检查 `users` 表为空才种测试用户（不重复插入）。
- 测试账号：`parent@test.com` / `child@test.com`，密码均为 `password123`。也可通过 UI 注册新账号。
- 前端登录后 JWT token 存 `localStorage`（key: `visitalk_token`），DevTools → Application → Local Storage 可查看。
- **本地图卡上传目录**：`backend/uploads/`（首次上传自动创建，已加入 `.gitignore`）。Spring `WebConfig` 把该目录映射到 `/uploads/**` 路由；切换电脑或清理环境时直接整目录 `rm -rf` 即可。
- **stale token 自救**：若浏览器之前有过登录残留导致行为异常，DevTools 控制台执行 `localStorage.clear(); location.reload()` 即可重置。后端 `JwtFilter` 已对 `/api/auth/**`、`/api/health`、`/uploads/**` 短路放行 stale token，登录本身不会被旧 token 阻塞。

---

# 第一部分：开发流程 (Development Workflow)

## 3. 分支策略 (Branching)

- 主分支 `main` 受保护：禁止直接 push，必须经 PR + Review + CI 全绿。
- 每个 Story / Bug 从最新 `main` 切分支：

| 类型     | 命名规范                       | 示例                       |
| ------ | -------------------------- | ------------------------ |
| 新功能    | `feat/<story-id>-<slug>`   | `feat/A-2-drag-sentence` |
| 缺陷修复   | `fix/<bug-id>-<slug>`      | `fix/BUG-12-pin-reset`   |
| 杂项/配置  | `chore/<slug>`             | `chore/ci-cache`         |

## 4. 提交规范 (Commit Convention)

采用 Conventional Commits：`<type>(<scope>): <subject>`

- type：`feat` / `fix` / `docs` / `test` / `refactor` / `chore`
- scope：模块名 `pecs` / `schedule` / `behavior` / `report`
- 示例：`feat(pecs): 句条支持图卡拖拽排序 (A-2)`

## 5. Pull Request 流程

1. 开发完成、本地测试通过后发起 PR，目标分支 `main`。
2. **PR 标题必须含 Story ID**，如 `[A-2] 拖拽图卡拼句`。
3. **PR 描述四节固定模板**：
   - **关联 Story**：链接 Jira Story
   - **变更说明**：做了什么、为什么
   - **测试步骤**：如何验证
   - **截图**：涉及 UI 时附儿童端/家长端截图
4. 至少 **1 名团队成员 Code Review** 通过。
5. CI 全绿后由作者合入 (Squash merge)。

## 6. Code Review 关注点

- 是否实现了 Story 的全部验收标准 (AC)。
- 儿童端交互：无文字主导、无突兀音效、误触保护、对比度 ≥ 4.5:1。
- 安全：是否绕过 RLS、是否泄露 `BehaviorEvent` / `DiaryEntry` 明细。
- 是否带了对应层级的测试 (见第二部分)。
- 命名、分包是否符合架构文档约定。
- **运行验证**：仅 `npm run build` / `./gradlew build` 通过 ≠ 功能可用。涉及交互/状态/网络的改动，PR 描述里必须给出"实际启动两端、操作走完主路径"的证据（截图或控制台日志）。Epic A 实测发现 v-model 绑 `computed`、`@drop.prevent` 无 handler、Jackson `is*` 序列化丢前缀、JwtFilter 误拒登录等 bug，单跑编译都查不出来。

## 7. CI/CD 流水线 (GitHub Actions)

push 到 `main` / `feat/**` / `fix/**` / `chore/**`、向 `main` 提 PR、以及手动 `workflow_dispatch` 均触发。**6 个 Job 已落地并全绿**（详见《Test Report》§9）：

```
frontend-lint ─► frontend-test ─┬─► frontend-build
 (vue-tsc)        (vitest run)   └─► frontend-e2e (playwright + chromium)

backend-test ─► backend-build
 (gradlew test，起 postgres:15 service；runner 自带 Docker，
  Testcontainers 真 Postgres 用例在此实跑)
```

- 任一 Job 失败则 PR 不可合并（`main` 受保护）。
- 最近一次 main 运行 6/6 success（run 27471191370）。
- 部署走平台侧自动化：前端 **Vercel**、后端 **Render**（push `main` 自动部署），数据库 **Neon**（PostgreSQL）。> 注：早期文档写的 Fly.io / Supabase 已分别换为 Render / Neon。
- CI 配置文件：`.github/workflows/ci.yml`。前端 lint=`vue-tsc --noEmit`、单元=Vitest、E2E=Playwright；后端=JUnit 5 + 真实/容器化 PostgreSQL。

---

# 第二部分：测试流程 (Testing Workflow)

## 8. 测试分层 (Test Pyramid)

| 层级       | 工具                  | 覆盖对象                          | 由谁写        | 何时跑          |
| -------- | ------------------- | ----------------------------- | ---------- | ------------ |
| 单元测试 (前端) | Vitest              | 组件逻辑、Pinia store、工具函数         | 前端开发者      | 本地 + CI      |
| 单元测试 (后端) | JUnit 5 + PostgreSQL | Service 业务逻辑、周报聚合算法           | 后端开发者      | 本地 + CI      |
| 集成测试     | JUnit 5 + PostgreSQL  | API 端到端、角色权限校验                | 后端开发者      | CI           |
| E2E 测试    | Playwright          | 关键用户流程 (拖拽拼句、日程执行、行为记录)        | 前端 / QA    | CI + 发布前     |
| 易用性测试     | 人工脚本 (儿童交互专项)        | 儿童端首达成率、误触率                   | SM/QA 主导   | 每 Sprint ≥1 次 |

## 9. 各类测试要求

### 9.1 单元测试
- 覆盖关键分支；新增业务逻辑必须带测试。
- 后端 Service 层、前端 store 与纯函数为重点。

### 9.2 权限 / 安全测试 (强制)
- 必须验证：`child` 角色 JWT 访问 `/api/behavior-events*` / `/api/reports/*` / `/api/alerts*` 全部 403（PRD §6.2 RLS）。
- 必须验证：`parent` 角色 JWT 调 `/api/diary-entries`（list / get）→ 403，只能调 `/check-today` 拿布尔。
- 必须验证：`child` 角色 JWT 调 `PUT /api/family-settings/diary-enabled` → 403。
- 必须验证：`child` 角色 JWT 调 `POST /api/schedules/templates` → 403（child 不能改日程）。
- 必须验证：跨 `family_id` 访问任意资源被拒绝（JwtFilter 把 family 从 token 注入 request attribute，controller 不接受 query 中的 familyId 覆盖）。
- 上述项每条已在 Epic A/B/C 开发期通过 curl 实测验证；**自动化层已补齐**——`PrivacyIsolationE2ETest`（10 条）+ `JwtFilterE2ETest`（3 条）在 CI 上以真实 HTTP 覆盖跨家庭 / 角色 / 过期 token，DoD 卡点满足。期间还发现并修复了 `CardController` / `ScheduleController.toggleStep` 两处真实越权缺口（详见《Test Report》§8），并对线上部署做了 `/verify` 权限抽查 PASS。

### 9.3 E2E 测试
- 每个核心 Story 至少 1 条 Playwright 用例覆盖主路径。
- **当前已落地**：`e2e/auth-guard.spec.ts`（2 条：落地页加载 + 未登录访问受保护路由被守卫重定向），CI `frontend-e2e` Job 用 Chromium 实跑通过。下列按 Story 的主路径用例仍待补（spec 内已留 TODO）。
- R1+R2 必备 E2E（功能侧已全部上线，E2E 覆盖进行中）：
  - A-2 拖拽拼句、A-5 上传即建卡
  - B-1 builder 保存 ≤10 steps、B-3 打勾推进到庆祝
  - C-1 行为记录 3 步、C-3 周报 ≥3 阈值、C-4 分享链接 24h 过期回 410、C-6 child 写日记 / parent 只看 bool

### 9.4 易用性测试 (儿童端专项)
- 每 Sprint 至少 1 次，邀请儿童用户或康复师代理评估。
- 度量：儿童端任务首达成率 (R1 ≥ 60%、R2 ≥ 75%、R3 ≥ 85%)、误触率。
- 结果回流到 Sprint Retrospective 形成改进项。

## 10. 缺陷管理 (Defect Workflow)

```
发现缺陷 ──► Jira 建 Bug ──► 定级 ──► 排期修复 ──► 验证 ──► 关闭
```

| 级别  | 定义                  | 处理时限              |
| --- | ------------------- | ----------------- |
| P0  | 阻断主流程 / 数据越权 / 崩溃   | 当前 Sprint 内必须修复    |
| P1  | 核心功能不可用，有临时绕过       | 当前 Sprint 内修复      |
| P2  | 非核心问题、体验瑕疵          | 进入 Backlog 排期      |

> DoD 要求：合并前**无新增 P0/P1 缺陷**。

---

## 11. Definition of Ready / Done (引用 PRD §10.4–10.5)

### DoR — Story 进入 Sprint 的前置
- [ ] 有清晰的 As a / I want / So that。
- [ ] 至少 1 条 Gherkin 验收标准。
- [ ] 完成估点 (Planning Poker, Fibonacci)，≤ 13 SP。
- [ ] 涉及 UI 的 Story 有低保真原型。
- [ ] 依赖项已识别且不阻塞本 Sprint。

### DoD — Story / 增量"完成"标准
- [ ] 代码合入 `main`，CI (lint + unit + e2e) 全绿。
- [ ] 单元测试覆盖关键分支，核心交互有 Playwright E2E。
- [ ] 至少 1 人 Code Review 通过。
- [ ] 全部验收标准验证通过。
- [ ] 部署 staging，PO 在 Sprint Review 接受。
- [ ] 文档已同步更新。
- [ ] 无新增 P0/P1 缺陷。

---

## 12. 变更记录 (Change Log)

| 版本   | 日期         | 作者                   | 变更         |
| ---- | ---------- | -------------------- | ---------- |
| v1.0 | 2026-05-18 | Ke Hongyi, Xu Ziyang | Workflow 文档初稿 |
| v1.1 | 2026-06-01 | Ke Hongyi, Xu Ziyang | 更新开发环境说明（H2 本地数据库）；登录流程已实现 |
| v1.2 | 2026-06-01 | Ke Hongyi, Xu Ziyang | H2 → PostgreSQL 17；新增 start.sh 一键启动；注册功能已实现 |
| v1.3 | 2026-06-03 | Ke Hongyi, Xu Ziyang | 补充本地 `backend/uploads/` 上传目录与 stale token 自救步骤；Code Review 新增"运行验证"项（编译通过 ≠ 功能可用） |
| v1.3.1 | 2026-06-04 | Ke Hongyi, Xu Ziyang | 日期刷新；无流程变更 |
| v1.4   | 2026-06-04 | Ke Hongyi, Xu Ziyang | §9.2 权限测试列表扩到具体端点（behavior / report / alerts / diary / family-settings / schedules）；§9.3 R1+R2 必备 E2E 列表扩到 7 项覆盖 A/B/C 全部关键 AC |
| v1.5   | 2026-06-13 | Ke Hongyi, Xu Ziyang | §7 CI/CD 改写为实际落地的 6-Job 流水线（前端 lint/test/build/e2e + 后端 test/build，含 Testcontainers 真 Postgres），平台名同步 Render/Neon/Vercel；§9.2 权限测试 DoD 卡点标记为已自动化（Privacy/JwtFilter E2E + 修复两处越权 + 线上 `/verify` PASS）；§9.3 标注 auth-guard E2E 已落地、其余按 Story 待补。详见《Test Report v2.2》 |
