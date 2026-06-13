# 🧪 VisiTalk — 测试报告 (Test Report)

| 项目        | VisiTalk — 自闭症儿童可视化沟通与情绪追踪平台 |
| --------- | ----------------------------- |
| 文档版本      | v2.1                          |
| 文档负责人     | Xu Ziyang (PO / 全栈) · Ke Hongyi (SM / QA) |
| 最后更新      | 2026-06-13                    |
| 关联文档      | `VisiTalk — Workflow 文档 (开发与测试).md`、项目架构文档、PRD |
| 范围        | 后端（单元 + 端到端 + Testcontainers）+ 前端（Vitest + Playwright 脚手架） |

---

## 1. 文档目的 (Purpose)

本文档说明 VisiTalk 的**自动化测试**做了什么、用了哪些工具与框架、为什么这样选，并如实标注每一层的**运行/验证状态**（已验证通过 / 需外部依赖未在本环境运行）。
对应 Workflow 文档第二部分「测试金字塔」的全部五层，是 DoD（单元覆盖关键分支、权限隔离、核心流程自动化）的落地证据。

> 一句话：**后端 41 条用例本地全绿（另 2 条真实 Postgres 用例在有 Docker 时运行）；前端 16 条 Vitest 全绿；Playwright 已搭好脚手架待本地运行。**

---

## 2. 总览 (At a Glance)

| 层级 | 工具 | 用例 | 状态 |
|------|------|------|------|
| 后端单元（业务逻辑） | JUnit 5 + Mockito | AuthService 10 · JwtUtil 3 · BehaviorAnalytics 4 | ✅ 17 通过 |
| 后端端到端（HTTP 全链路） | Spring Boot Test + TestRestTemplate + H2 | AuthFlow 5 · **Privacy 10** · Api 6 · JwtFilter 3 | ✅ 24 通过 |
| 后端真实库（Postgres 专有） | **Testcontainers(Postgres)** | BehaviorAggregation 2 | ⏭️ 需 Docker（无 Docker 自动跳过） |
| 前端单元 | **Vitest** + jsdom | cards 6 · client 4 · auth 6 | ✅ 16 通过 |
| 前端 E2E（浏览器级） | **Playwright** + Chromium | auth-guard 2 | ✅ 2 通过（Chromium 无头实跑） |

**后端 41 条通过 + 2 条 Docker 守卫；前端 16 单元 + 2 E2E 通过。**

---

## 3. 工具与框架选型 (Tooling & Why)

| 工具 / 框架 | 用途 | 为什么选它 |
|------------|------|-----------|
| **JUnit 5 (Jupiter)** | 后端测试运行引擎 | Spring Boot 官方默认；Workflow 文档已选定 |
| **Mockito** | 单元测试中替身掉 DB / 加密器 / JWT 工具 | 让业务逻辑脱离数据库快速测分支（毫秒级） |
| **Spring Boot Test (`@SpringBootTest`)** | 端到端启动整个容器 | 验证请求穿过安全链 + 控制器 + 持久层的真实行为 |
| **TestRestTemplate + `RANDOM_PORT`** | 真实 HTTP 打接口 | 比 MockMvc 更真——真的走 `JwtFilter`/Security，能验证鉴权拒绝 |
| **H2（PostgreSQL 兼容模式）** | 端到端内存库 | 无需外部 Postgres 即可一键跑通端到端 |
| **Testcontainers(Postgres)** | 真实 Postgres 容器 | H2 跑不了 `text[]` + `unnest`；这一层只能用真 Postgres 验证 |
| **Vitest + jsdom** | 前端单元测试 | 与 Vite 同构、零额外配置、原生 TS/ESM；CLAUDE.md 已指定 `npx vitest run` |
| **@vue/test-utils** | （预留）Vue 组件测试 | 配合 Vitest 测组件渲染 |
| **Playwright** | 浏览器级 E2E | Workflow §9.3 指定；跨浏览器、自动等待、可录 trace |

### 为什么分这么多层
- **单元**：快、定位准，测每个分支。
- **端到端（API）**：抓集成问题（安全配置、序列化、过滤器顺序）。
- **真实库**：抓 H2 兼容模式照不到的 Postgres 专有 SQL。
- **浏览器 E2E**：抓前端真实用户路径。
逐层互补，正是测试金字塔。

---

## 4. P0 — 隐私 / 权限隔离（项目核心约束）

> CLAUDE.md「RLS Privacy」+ Workflow §9.2 **强制 DoD 卡点**。文件：`PrivacyIsolationE2ETest`（10 条，H2 上真实 HTTP 运行）。

**4.1 调查结论（实际代码）**：`Behavior/Diary/Alert/WeeklyReport/FamilySettings/Sentence` 与 `Schedule` 模板增删改/`today` 控制器，都正确地**从 JWT 取 `familyId`、先校验 `role`**，跨家庭与角色隔离成立。

**4.2 发现并修复的两个真实安全缺口**（详见 §8）：
- `CardController`：GET 信任 query 的 `familyId`、写操作无 role/family 校验 → 已改为从 JWT 取 family + 写操作要求 parent + 校验卡片归属。
- `ScheduleController.toggleStep`：未校验实例归属 → 已加 family 归属校验。

**4.3 覆盖的用例**：
- 跨家庭读隔离：B 用户即便在 query 传 A 的 `familyId`，也只能拿到自己家庭的卡片。
- 跨家庭写隔离：B 删/改 A 的卡片 → 404。
- 家庭设置按家庭隔离：A 开启日记不影响 B。
- 角色边界：儿童不能读行为事件 / 记录行为 / 读预警 / 读周报 / 开关日记 / 建卡片；家长不能读儿童日记内容 / 写日记。

---

## 5. P1 — 控制器端点 + JwtFilter

**5.1 `ApiEndpointE2ETest`（6 条，H2）**
- 卡片 CRUD：家长创建 → PATCH 改名 → DELETE 删除全链路。
- 日记全流程 + 隐私：家长开启 → 儿童写（带真实卡片 id）→ 儿童读到内容 → **家长 check-today 只拿到 `writtenToday`+`count`，断言响应里没有 `emotionCardId`/`doodleUrl`**。
- 日记功能关闭时写入被拒（409）。
- 家庭设置默认关闭 → 家长开启。
- 校验守卫（在数组 SQL 之前返回，故 H2 可测）：行为事件非法 intensity → 400；日程模板空名/空步骤/超 10 步 → 400、儿童建模板 → 403。

**5.2 `JwtFilterE2ETest`（3 条）**
- 过期 token 访问受保护接口 → 401。
- 公开接口（`/api/health`）带垃圾 token 仍放行 → 200。
- 公开登录接口带过期 token 仍能进控制器（返回的是凭证错误 401，而非被过滤器拦截）。

---

## 6. P2 — 前端 Vitest（+ Playwright 脚手架）

**6.1 已通过（`npm run test` → `vitest run`，16 条全绿）**
- `src/stores/cards.spec.ts`（6）：`cardIcon`/`cardText`/`isPhotoCard`/`isTextCard` 按 `emoji:`/`text:`/`/uploads/`/兜底 的渲染约定；`BOARD_CATEGORIES` 正确排除 People/Action/Time。
- `src/api/client.spec.ts`（4）：`assetUrl` 对 null / 绝对 URL / `/uploads` / 伪 URL 的处理。
- `src/stores/auth.spec.ts`（6）：`hydrateFromToken` 从 JWT 解出 role/family、畸形 token 保持登出、logout 清理、login 成功写 token、登录失败回填 error。

**6.2 Playwright（✅ 已用 Chromium 无头实跑，2/2 通过）**
- `playwright.config.ts` + `e2e/auth-guard.spec.ts`：① 落地页加载；② 未登录访问受保护路由 `/parent/behavior` 被路由守卫重定向回首页（若无守卫该断言会失败，故为真实验证）。
- 已装 `@playwright/test` + Chromium；`webServer` 自动起 `npm run dev`（:3000），这两条无需后端。运行：
  ```bash
  cd frontend
  npx playwright install chromium     # 一次性
  npm run test:e2e                    # → 2 passed
  ```
- 涉及后端数据的流程（拼句/日程/行为/日记）需另起 `./gradlew bootRun`；spec 内留有按 Story（A-1/A-2、B-2/B-3、C-1、C-6/C-7）扩展的 TODO 清单。

---

## 7. P3 — Testcontainers 真实 Postgres

> 文件：`BehaviorAggregationPostgresTest`（2 条）。**这是 H2 唯一够不着的一层**：`behavior_event` 用 `text[]`、分析 SQL 用 `unnest(...)`。

- 用 Testcontainers 起真实 `postgres:16-alpine`，应用生产 `schema.sql`，再用真实数组列驱动 `BehaviorAnalyticsService`。
- 验证：周报对真实 `unnest` 数组聚合（Top 触发因素、7 天图表点）；连续触发预警在真实数据上检测 3 天连击。
- **守卫**：`@Testcontainers(disabledWithoutDocker = true)` —— 无 Docker 自动跳过（本环境 `NO_DOCKER`，2 条已跳过，套件仍绿）；有 Docker 的 CI / 本地会真正运行。

---

## 8. 本轮对生产代码的安全加固 (Code Changes)

> ⚠️ 为让 P0 的「跨家庭被拒」成立，修复了两处与设计意图（Workflow §9.2）冲突的真实缺口。**改动向后兼容**（合法调用本就传自己家庭的 id / 由家长发起）：

1. **`CardController`**
   - GET：`familyId` 改为从 JWT 取，忽略 query 覆盖（保留 query 参数仅作兼容）。
   - POST：要求 `parent` 角色，并强制把卡片归入调用者家庭。
   - DELETE / PATCH / reorder：要求 `parent`，且只操作属于本家庭的卡片（否则 404 / 跳过）。
2. **`ScheduleController.toggleStep`**：勾选步骤前校验该实例的模板属于调用者家庭，否则 404。

> **建议人工确认**：前端 child 模式仍能正常读卡片（GET 用 JWT family，结果不变）；卡片增删改属家长操作。可用 `/verify` 或 `/run` 跑一遍前端确认无回归。

---

## 9. 如何运行 (How to Run)

```bash
# 后端
cd backend
./gradlew test                                   # 全部（无 Docker 时自动跳过 Postgres 用例）
./gradlew test --tests "*PrivacyIsolationE2ETest" # 只跑隐私隔离

# 前端单元
cd frontend
npm run test                                     # vitest run（16 条）

# 前端 E2E（需本地浏览器 + 双服务，见 §6.2）
npm run test:e2e
```
报告：后端 `backend/build/reports/tests/test/index.html`。

---

## 10. 测试结果 (Results)

```
后端：BUILD SUCCESSFUL — 41 passed, 2 skipped (Docker-gated), 0 failed
前端单元：Test Files 3 passed (3) · Tests 16 passed (16)
前端 E2E：2 passed (Playwright + Chromium, headless)
```

| 测试类 | 结果 |
|--------|------|
| AuthServiceTest | 10 ✅ |
| JwtUtilTest | 3 ✅ |
| BehaviorAnalyticsServiceTest | 4 ✅ |
| AuthFlowE2ETest | 5 ✅ |
| PrivacyIsolationE2ETest | 10 ✅ |
| ApiEndpointE2ETest | 6 ✅ |
| JwtFilterE2ETest | 3 ✅ |
| BehaviorAggregationPostgresTest | 2 ⏭️（本环境无 Docker，已跳过） |
| 前端 cards / client / auth（Vitest） | 16 ✅ |
| 前端 auth-guard（Playwright） | 2 ✅ |

---

## 11. 已知约束 (Trade-offs)

1. **H2 vs 真实 Postgres**：端到端默认用 H2（无需 Docker 一键跑）；`behavior_event` / `schedule` / `sentence` 的数组 SQL happy-path 由 Testcontainers（P3）覆盖，其 403/404 拒绝路径在 H2 上已覆盖（校验先于数组 SQL）。
2. **Playwright 已实跑**：装 Chromium 后无头运行 2/2 通过；后端相关流程需另起 `bootRun` 后扩展。
3. **本沙箱无 Docker**（无 CLI / 无守护进程 / 无 Docker.app / 无 colima）：P3 的 2 条真实 Postgres 用例本环境**无法运行**，已由 `disabledWithoutDocker` 守卫跳过，需在有 Docker 的机器 / CI 验证。

---

## 12. 后续待办 (Next Steps)

- [ ] 在有 Docker 的 CI 上启用 P3（去掉/确认 `disabledWithoutDocker` 行为，作为质量门）。
- [ ] 按 Story 扩展 Playwright：拖拽拼句、日程执行、行为记录、日记隐私（§6.2 TODO）；为关键元素加 `data-testid`。
- [ ] 前端组件级测试（@vue/test-utils）覆盖 child 端关键交互。
- [ ] 把后端 `./gradlew test` + 前端 `npm run test` 接入 `.github/workflows/ci.yml`，PR 卡点强制跑。
- [ ] 人工回归确认 §8 的 CardController/Schedule 加固对前端无影响。

---

## 13. 变更记录 (Changelog)

| 版本 | 日期 | 作者 | 变更 |
|------|------|------|------|
| v2.1 | 2026-06-13 | Xu Ziyang, Ke Hongyi | 实跑验证 Playwright（装 Chromium，2/2 通过）；确认本沙箱无 Docker，P3 仍只能跳过 |
| v2.0 | 2026-06-13 | Xu Ziyang, Ke Hongyi | P0 隐私/权限隔离（10 条）+ P1 控制器/JwtFilter（9 条）+ P2 前端 Vitest（16 条）+ Playwright 脚手架 + P3 Testcontainers（2 条，Docker 守卫）；修复 CardController / Schedule.toggleStep 两处安全缺口 |
| v1.0 | 2026-06-13 | Xu Ziyang, Ke Hongyi | 首版：后端 AuthService/JwtUtil 单元 + AuthFlow 端到端，修复原失效的 BehaviorAnalyticsServiceTest，引入 H2 测试环境 |
