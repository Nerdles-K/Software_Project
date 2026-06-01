# 🔄 VisiTalk — Workflow 文档 (开发与测试流程)

| 项目        | VisiTalk — 自闭症儿童可视化沟通与情绪追踪平台 |
| --------- | ----------------------------- |
| 文档版本      | v1.2                          |
| 文档负责人     | Ke Hongyi (SM/QA) · Xu Ziyang (架构) |
| 最后更新      | 2026-06-01                    |
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

## 7. CI/CD 流水线 (GitHub Actions)

每次 PR 与每次合并 `main` 触发：

```
lint  ──►  unit test  ──►  build  ──►  e2e test  ──►  deploy staging
(前端 ESLint /          (Vitest /                      (Vercel /
 后端 Checkstyle)        JUnit 5)        (Playwright)    Fly.io)
```

- 任一步失败则流水线中断，PR 不可合并。
- 合并 `main` 后自动部署到 staging；Release 末手动晋级到 prod。
- 每个 Sprint 末打 tag `vR{n}-sprint{m}`。
- CI 配置文件：`.github/workflows/ci.yml`；前端 lint/单元测试使用 Vitest，后端使用 JUnit 5 + PostgreSQL。

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
- 必须验证：`child` 角色 JWT 访问 `BehaviorEvent` API 被拒绝。
- 必须验证：`parent` 角色 JWT 读取 `DiaryEntry` 明细字段被拒绝、只能读聚合。
- 必须验证：跨 `family_id` 访问任意资源被拒绝。
- 权限通过 Spring Security 角色 + JWT 中的 `family_id` 在应用层校验。
- 必须验证：跨 `family_id` 访问任何表被拒绝。
- 这是风险 R4 (RLS 配置错误) 的核心防线，缺失则 PR 不可合并。

### 9.3 E2E 测试
- 每个核心 Story 至少 1 条 Playwright 用例覆盖主路径。
- R1 必备 E2E：A-2 拖拽拼句、B-1+B-2 日程编排与高亮。

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
