# 🏗️ VisiTalk — 项目架构文档 (System Architecture)

| 项目        | VisiTalk — 自闭症儿童可视化沟通与情绪追踪平台 |
| --------- | ----------------------------- |
| 文档版本      | v1.7                          |
| 状态        | Epic A + B + C 已完成（R1/R2 范围全部交付）；已生产部署 + 自动化测试/CI 上线 |
| 文档负责人     | Xu Ziyang (PO / 架构)           |
| 最后更新      | 2026-06-13                    |
| 关联文档      | `📦 VisiTalk — Product Requirements Document (PRD).md` |

---

## 1. 文档目的 (Purpose)

本文档描述 **VisiTalk** 的系统架构、模块划分、技术选型理由、数据流与部署拓扑，作为团队开发的技术参考基线。功能范围以 PRD 为准；本文档只回答"系统如何被构建"。

---

## 2. 架构总览 (Architecture Overview)

VisiTalk 采用**前后端分离 + BaaS 辅助**的三层架构：

- **表现层 (Frontend)**：单页应用 (SPA)，内含儿童模式与家长模式，通过 JWT 登录/注册鉴权自动切换角色。
- **应用层 (Backend)**：Spring Boot 提供 REST API（JWT 鉴权），承载登录、注册、图卡拼句、日程、行为分析与周报生成。
- **数据层 (Data)**：PostgreSQL 17 本地实例持久化存储；`schema.sql` 手动管理表结构，JPA `validate` 模式校验。

```
┌─────────────────────────────────────────────────────────────┐
│                   客户端 (平板 / 手机浏览器)                  │
│  ┌───────────────────────┐   ┌───────────────────────────┐  │
│  │   儿童模式 Child UI    │   │     家长模式 Parent UI     │  │
│  │  PECS / Schedule /     │   │  Behavior Logger /         │  │
│  │  Child Diary           │   │  Weekly Report / Settings  │  │
│  └───────────┬───────────┘   └─────────────┬─────────────┘  │
│              └────────  Vue 3 SPA  ─────────┘                │
└───────────────────────────┬─────────────────────────────────┘
                             │ HTTPS / JSON (JWT)
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                后端 API — Spring Boot 3 (Render)             │
│   pecs 包  │  schedule 包  │  behavior 包  │  report 包       │
│  鉴权过滤器 (JWT)  ·  业务校验  ·  周报生成  ·  PDF 导出       │
└───────────────────────────┬─────────────────────────────────┘
                             │ SQL (应用层 family_id 校验)
                             ▼
┌─────────────────────────────────────────────────────────────┐
│        PostgreSQL — 本地 17 (dev) / Neon (prod) 持久化存储       │
│   users · pictogram_card · sentence · schedule · behavior ...  │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. 技术栈与选型理由 (Tech Stack & Rationale)

| 层      | 选型                                          | 选型理由                                                       |
| ------ | ------------------------------------------- | ---------------------------------------------------------- |
| 前端框架   | Vue 3 + Vite                                | 组件化、上手快、Vite 热更新快；团队前端成员已有经验。                              |
| 状态管理   | Pinia                                       | Vue 3 官方推荐，比 Vuex 更轻、类型友好；用于跨页面共享会话与图卡库。                   |
| 样式     | Tailwind CSS                                | 工具类快速搭建大尺寸、高对比度的儿童界面；易统一无障碍规范。                             |
| 拖拽交互   | vue-draggable-plus                          | A-2 图卡拖拽拼句的核心依赖；支持触摸事件，适配平板。                               |
| 后端框架   | Spring Boot 3 (Java 17)                     | 团队后端成员熟悉 Java；模块化分包清晰；生态成熟。                                |
| 接口风格   | REST + JWT                                  | 简单、易测试、易与前端联调；JWT 承载 role/family_id 用于鉴权。                  |
| 数据库    | PostgreSQL 17 (本地)                        | 关系型保证数据一致性；本地零网络依赖；`schema.sql` 管理 DDL。 |
| 认证方式   | Spring Security + JWT (自建)                 | BCrypt 密码哈希 + JJWT 签发/验证；JWT 含 userId, role, family_id。 |
| RLS 替代  | Spring Security 角色 + family_id 应用层校验   | 课程项目周期内应用层校验替代数据库 RLS，逻辑等价。 |
| 文件存储   | 本地文件系统 `backend/uploads/`（生产可接对象存储）     | 自定义图卡照片落盘 + `WebConfig` 映射 `/uploads/**`；Render 临时磁盘重启即丢，长期可接 Cloudinary 等对象存储。 |
| 数据库托管  | Neon (PostgreSQL)                           | 生产数据库；免费档、无需绑卡，标准 Postgres 协议，与本地 17 同源迁移零成本。                |
| CI/CD  | GitHub Actions                              | 与代码仓库同平台；自动跑 lint / 测试；push main 触发平台侧自动部署。                |
| 前端部署   | Vercel                                      | 静态 SPA 部署快、自带 CDN 与预览环境。                                   |
| 后端部署   | Render (Docker)                             | 容器化部署 Spring Boot，免运维、免绑卡；push main 自动部署。               |
| 测试     | Vitest · Playwright · JUnit 5               | 单元 / E2E / 后端单元，详见 Workflow 文档。                            |

---

## 4. 模块架构 (Module Architecture)

系统按 PRD 三大功能模块分包，前后端均保持同名分层，降低认知成本。

### 4.1 Module A — Digital PECS Builder ✅ 已完成 (Sprint 1, 6/3)，**A-2 6/4 扩展为双向对话**

> 6/3 收尾后补：A-5 改为"上传即建卡"语义，新增 `PATCH /api/cards/{id}` 与列表 inline 重命名（详见 §6 API 列表）。
> 6/4 A-2 升级：词卡库扩到 **53 卡 × 7 分类**（新增 People/Action/Time 让句子能拼出主谓宾+时间），并把 sentence 表从单向"儿童拼好就存"改为双向家庭对话——sentence 表去 `child_id`，加 `family_id` + `sender_role`；新增 `/child/chat`（amber 浅色）和 `/parent/chat`（slate 深色）共用 `ChatComposer` + `MessageBubble`；两端 3s polling 拉新消息；ParentNav 加 Chat tab，child 主屏右下加 💬 大圆按钮入口。
- **前端**：`views/child/pecs/ChildPecsView.vue`（分类网格 + 句条 + HTML5 drag/drop + 点击双通道）；`views/parent/pecs/ParentPecsView.vue`（CRUD + vue-draggable-plus 排序 + 文件上传 UI）。
- **后端**：`pecs.CardController`（GET/POST/DELETE/PUT reorder）、`pecs.SentenceController`、`pecs.UploadController`（multipart 文件落盘 + 类型/大小校验）。
- **数据**：`PictogramCard`（`@JsonProperty("isCustom")` 强制 JSON 字段名）、`Sentence`。
- **关键依赖**：vue-draggable-plus（家长端排序）；本地文件系统 `backend/uploads/`（A-5 上传图卡，通过 `WebConfig` 暴露为 `/uploads/**` 静态资源；生产环境可平滑切换到对象存储，如 Cloudinary）。

### 4.2 Module B — Visual Schedule & Timeline ✅ 已完成 (Sprint 1+2 合并, 6/4)
- **前端**：
  - `views/parent/schedule/ParentScheduleView.vue` — list / build / edit / delete templates（≤10 steps 前端硬限 + 后端 400 兜底）；分类筛选 + 库点选 + 上下箭头排序 + 实时预览（icon + label）。
  - `views/child/schedule/ChildScheduleView.vue` — 多模板 picker；逐步执行：current 强高亮（ring-4 + scale）+ next 半高亮（ring-2 + opacity-80）+ rest 暗化（opacity-40）+ done 翠绿；80×80 大复选框（B-3），✓ pop 动画；全完成 🎉 庆祝 + Start Over 重置。
  - 入口：[ParentNav](frontend/src/components/ParentNav.vue) 加 Schedule tab，[ChildPecsView](frontend/src/views/child/pecs/ChildPecsView.vue) 左下角 🗓️ 大圆按钮（≥120px 触控区）。
- **后端**：
  - `schedule.ScheduleController` — `GET/POST/PUT/DELETE /api/schedules/templates`、`GET /api/schedules/today?templateId=`、`PUT /api/schedules/instances/{id}/step`。
  - `repository.ScheduleTemplateRepository` / `ScheduleInstanceRepository` — JdbcTemplate（`BIGINT[]` 数组列），与 `BehaviorEventRepository` 模式一致。
  - `today` 端点合并 template + instance + 按步骤顺序展开的 PictogramCard 三个对象，让儿童端单次请求就能渲染完整 schedule，不再额外拉 cards 表。
- **数据**：`ScheduleTemplate (steps BIGINT[])` / `ScheduleInstance (completed_step_ids BIGINT[] 存索引)`。
- **关键约束**：
  - `steps` 长度 1..10 控制器层硬校验；超出返回 400。
  - `instances.findByTemplateAndDate` 不存在则 controller `today` 端点自动建（不需要 child 端 POST）。
  - 删除 template 会先 DELETE 对应 instances，避免 FK 残留。
  - `completed_step_ids` 用 **索引**（0-based）而非 card id，让同一张卡在同一 schedule 重复出现也能去重不歧义。

### 4.3 Module C — Behavior Logger, Weekly Report & Child Diary ✅ 已完成 (Sprint 2, 6/4)
- **前端**：
  - `views/parent/behavior/ParentBehaviorView.vue` — C-1 三步表单（intensity → trigger → submit），C-2 深色 slate-900 UI，C-2 PIN-1234 退出确认。
  - `views/parent/behavior/ParentReportView.vue` — C-3 自绘 SVG 折线图 + Top-3，C-4 `window.print()` 走浏览器 PDF + Create Share Link。
  - `views/parent/settings/ParentSettingsView.vue` — C-5 连续触发警告横幅 + dismiss，C-7 diary toggle + today-bool 指示。
  - `views/child/diary/ChildDiaryView.vue` — C-6 5 张情绪图卡 + Pointer Events 画布涂鸦 + 颜色选择。
  - `views/PublicShareView.vue` — C-4 anonymous 公开页面（路由 `/share/reports/:token`，无 requiresAuth）。
- **后端**：
  - `behavior.BehaviorController` — C-1 `POST /api/behavior-events`（只 parent）、`GET` 列表。
  - `behavior.BehaviorAnalyticsService` — C-3 周聚合算法（Mon-Sun 自然周；≥3 记录 → success，否则 insufficient）、C-5 连续天数检测（30 天窗口 + dismissal 抑制）。
  - `behavior.AlertController` — C-5 `GET /api/alerts`、`POST /api/alerts/dismiss`。
  - `report.WeeklyReportController` — C-3 `GET /api/reports/weekly`、C-4 `POST /api/reports/share`（生成 token + payload 快照 + 24h 过期）。
  - `report.PublicShareController` — C-4 `GET /share/reports/{token}`（无鉴权；404 / 410 / 200 三态）。
  - `pecs.DiaryController` — C-6 `POST` / `GET`（child-only 写读）；C-7 `GET /check-today`（parent-only 布尔）。
  - `pecs.FamilySettingsController` — C-7 `GET` / `PUT /diary-enabled`（PUT 仅 parent）。
- **数据**：`BehaviorEvent`（JdbcTemplate，含 `text[] trigger_tags`）、`DiaryEntry`、`FamilySettings` (system_configs)、`ReportShare`、`AlertDismissal`。
- **隐私要点**：`DiaryEntry` 明细对家长 403；parent 只能拿 `{enabled, writtenToday, count}`。child 完全无法访问 `/api/behavior-events*`、`/api/reports/*`、`/api/alerts*`。所有跨角色规则在 controller 入口处通过 `req.getAttribute("role")` 强制校验。

---

## 5. 数据架构 (Data Architecture)

实体模型 (与 PRD §7 一致)：

| 表                  | 关键字段                                                        | 归属模块 |
| ------------------ | ----------------------------------------------------------- | ---- |
| `User`             | id, email, password_hash, role(child\|parent), family_id, pin_hash | 全局   |
| `PictogramCard`    | id, family_id, category, image_url, label_i18n, is_custom   | A    |
| `Sentence`         | id, child_id, card_ids[], created_at                        | A    |
| `ScheduleTemplate` | id, family_id, name, steps[card_id]                         | B    |
| `ScheduleInstance` | id, template_id, date, completed_step_ids[]                 | B    |
| `BehaviorEvent`    | id, parent_id, child_id, intensity(1-5), trigger_tags[], note_encrypted, occurred_at | C |
| `WeeklyReport`     | id, child_id, week_start, metrics_json, pdf_url             | C    |
| `DiaryEntry`       | id, child_id, emotion_card_id, doodle_url, created_at       | C    |

- **family_id** 是数据隔离的核心维度：同一家庭的所有数据共享一个 `family_id`。
- `note_encrypted` 字段对行为记录备注做端到端加密。

---

## 6. 安全与隐私架构 (Security & Privacy)

### 6.1 鉴权流程

```
前端 Login Form (email + password)
        │  POST /api/auth/login
        ▼
后端 AuthController
        │  AuthService.login(email, password)
        ▼
   查 User 表 → BCrypt 验密 → JwtUtil.generateToken(userId, role, familyId)
        │  返回 { token, role, familyId }
        ▼
前端 auth store
        │  存 token 到 localStorage
        │  存 role + familyId 到 Pinia
        │  路由守卫检查 localStorage token
        ▼
后续 API 请求
        │  Authorization: Bearer <token>
        ▼
JwtFilter
        │  解析 JWT → 提取 userId, role, family_id
        │  注入 Spring SecurityContext
        ▼
   业务 Controller（SecurityContext 中取当前用户身份）
```

### 现有 API 端点

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/health` | 无 | 健康检查 |
| POST | `/api/auth/register` | 无 | 注册（email, password, role） |
| POST | `/api/auth/login` | 无 | 登录，返回 JWT token, role, familyId |
| GET | `/api/cards?familyId=&category=` | JWT | 按家庭 / 分类查询图卡（A-1） |
| POST | `/api/cards` | JWT | 新增图卡（A-4） |
| DELETE | `/api/cards/{id}` | JWT | 删除图卡（A-4） |
| PATCH | `/api/cards/{id}` | JWT | 重命名图卡 `labelI18n`（A-5 上传后默认 label 改名） |
| PUT | `/api/cards/reorder` | JWT | 批量更新 `sort_order`（A-4） |
| POST | `/api/sentences` | JWT | 发送 PECS 对话消息；sender_role+family_id 从 JWT 取，body 仅含 cardIds（A-2 对话版） |
| GET | `/api/sentences` | JWT | 当前家庭对话 feed（time asc，limit ≤500） |
| GET | `/api/sentences?sinceId=N` | JWT | 增量 poll：仅返回 id > N 的新消息 |
| POST | `/api/uploads` | JWT | multipart 上传 JPG/PNG ≤5MB，返回 `{url:"/uploads/{uuid}.{ext}"}`（A-5） |
| GET | `/uploads/**` | 无 | 静态服务上传的图卡照片（A-5；缓存 1h） |
| POST | `/api/behavior-events` | JWT parent | 记录情绪/触发事件（C-1） |
| GET | `/api/behavior-events` | JWT parent | 列出历史事件（C-1） |
| GET | `/api/reports/weekly` | JWT parent | 自然周报：折线 chart + Top3，<3 条返回 `status=insufficient`（C-3） |
| POST | `/api/reports/share` | JWT parent | 创建只读 token，payload 冻结，24h 过期（C-4） |
| GET | `/share/reports/{token}` | 无 | 匿名读取报告；过期 410；未知 404（C-4） |
| GET | `/api/alerts` | JWT parent | 当前连续 3 天同触发的标签列表（C-5） |
| POST | `/api/alerts/dismiss` | JWT parent | 关闭某 tag 提示，直到新一轮连续 streak 开始（C-5） |
| POST | `/api/diary-entries` | JWT child | 写日记；feature off 返回 409（C-6） |
| GET | `/api/diary-entries` | JWT child | 仅儿童读自己（C-6；parent 直访 403） |
| GET | `/api/diary-entries/check-today` | JWT parent | 仅返回 `{enabled, writtenToday, count}`，不暴露明细（C-7） |
| GET | `/api/family-settings` | JWT 任意 | 家庭开关（child 需要知道是否显示日记图标） |
| PUT | `/api/family-settings/diary-enabled` | JWT parent | 开/关儿童日记功能（C-7） |
| GET | `/api/schedules/templates` | JWT | 当前家庭模板列表（B-4） |
| POST | `/api/schedules/templates` | JWT parent | 创建模板（B-1）；steps ∈ [1,10] |
| PUT | `/api/schedules/templates/{id}` | JWT parent | 改名 / 改步骤（B-4） |
| DELETE | `/api/schedules/templates/{id}` | JWT parent | 删除模板 + 级联清 instance（B-4） |
| GET | `/api/schedules/today?templateId=` | JWT | 返回 `{template, instance, cards in order}`；instance 不存在则自动建 |
| PUT | `/api/schedules/instances/{id}/step` | JWT | body `{stepIndex, completed}`；B-3 |

#### Multipart / 上传约束
- `application.yml` 设 `spring.servlet.multipart.max-file-size: 10MB`、`max-request-size: 12MB`。
- 业务层 `UploadController.MAX_BYTES = 5MB` 比 Spring 阈值小一档，目的是让超大文件在**应用层**返回友好 400 + JSON `{"error":"File exceeds 5MB limit"}`，避免 Spring 默认在过滤器链直接抛 `MaxUploadSizeExceededException` → 403。
- 允许的 MIME：`image/jpeg`、`image/jpg`、`image/png`。文件名使用 `UUID` 防覆盖。

### 鉴权流程

1. **注册**：用户输入 email + password + role（parent/child）→ 前端调 `POST /api/auth/register` → 后端 BCrypt 加密密码 → 自动生成 `family_id` → 存入 PostgreSQL → 返回 JWT 直接登录。
2. **登录**：用户输入 email + password → 前端调 `POST /api/auth/login` → 查 `users` 表 → BCrypt 验密 → `JwtUtil` 签发 JWT（含 `sub=userId`, `role`, `family_id`，24h 过期）。
3. 前端 `auth store` 将 token 存入 `localStorage`，Pinia 记录 `mode` / `familyId`。
4. 前端 `api/client.ts` 的 fetch 封装在每次请求时从 `localStorage` 取 token，自动带 `Authorization: Bearer <token>`；**例外**：`/api/auth/**` 路径不带 token，避免 stale token 让登录本身失败。
5. 后端 `JwtFilter` 解析每个请求的 JWT，验证签名后提取身份信息注入 Spring Security Context；当 token 无效但路径是公开路径（`/api/auth/**`、`/api/health`、`/uploads/**`）时**短路放行**，让请求进入对应 Controller。
6. 路由守卫 `router.beforeEach` 拦截未登录访问（检查 `localStorage` 无 token 则重定向到 `/`）。
7. 启动时 `DataInitializer`（`CommandLineRunner`）自动种测试用户（parent@test.com / child@test.com）。

### 6.2 Row Level Security (RLS) 策略
| 角色     | PECS / Schedule | BehaviorEvent | DiaryEntry                         |
| ------ | --------------- | ------------- | ---------------------------------- |
| child  | 读写本家庭数据         | **完全不可访问**    | 读写本人记录                             |
| parent | 读写本家庭数据         | 读写本家庭数据       | **仅可读聚合字段** (当日是否有记录)，不可读明细内容       |

> 关键约束：儿童会话永远读不到 `BehaviorEvent`（避免孩子察觉被记录）；家长会话读不到 `DiaryEntry` 的 `emotion_card_id` / `doodle_url`（保护儿童私人表达空间）。所有约束以 Postgres RLS Policy 形式强制，不依赖应用层判断。

### 6.3 合规
- 遵循 GDPR 与中国《个人信息保护法》对未成年人数据的最小化原则。
- 周报分享链接 24 小时自动失效，只读且无需登录。

---

## 7. 部署架构 (Deployment)

```
GitHub (main 分支)
   │  push / merge
   ▼
GitHub Actions ── lint + unit + e2e + build (6 Job) ──┐
   │                                                  │ 全绿才合入 main
   ├──► Vercel        (前端 SPA)  ──► prod
   └──► Render        (后端 API, Docker)  ──► prod
                                       │
                                       ▼
                              Neon (PostgreSQL, prod 实例)
```

- **环境**：`dev` (本地 PostgreSQL 17 + Spring Boot + Vite) / `prod` (push main 后 Vercel + Render 自动部署，数据库 Neon)。
- 平台变更：早期规划的 Fly.io / Supabase 已分别换为 **Render**（免绑卡）/ **Neon**（免费额度），详见 ADR-1 / ADR-4。
- 一键启动脚本：`bash start.sh`（自动检查 PostgreSQL、清理端口、启动前后端、打开浏览器）。
- 每个 Sprint 末打 tag `vR{n}-sprint{m}`。

---

## 8. 关键架构决策记录 (ADR 摘要)

| #     | 决策                                  | 理由                                      |
| ----- | ----------------------------------- | --------------------------------------- |
| ADR-1 | 数据库用托管 PostgreSQL（原选 Supabase，**后改为 Neon**） | 4 人团队省运维；Supabase 免费组织额度满后迁到 Neon（标准 Postgres、免绑卡、与本地 17 同源）。鉴权未用 Supabase Auth，改自建 JWT（见 ADR-6）。 |
| ADR-2 | 数据隔离改在应用层（JwtFilter 注入 family_id + 角色 + controller 归属校验），不依赖 Supabase RLS | 迁到 Neon 后无 Supabase RLS；隐私隔离落到 Spring Security + 应用层校验，并用 PrivacyIsolation/JwtFilter E2E（13 条）+ 线上 `/verify` 做硬保证（见《Test Report》）。 |
| ADR-3 | 移除 TTS 语音播报功能 (原 A-3)               | 目标用户对声音敏感，语音播报与产品愿景冲突，列入 v4 候选。          |
| ADR-4 | 前后端分别部署到 Vercel / Render（后端原选 Fly.io，**后改为 Render**） | SPA 与 API 伸缩特性不同，分开部署各取最优；Fly.io 需绑卡，改用 Render（Docker、push main 自动部署、免绑卡）。 |
| ADR-5 | 本地用 PostgreSQL 17 替代 H2 + 云库       | H2 每次重启丢数据不利于持续开发；本地 PostgreSQL 17 零网络依赖、数据持久化、与生产托管库（Neon）同为标准 Postgres，迁移零成本。 |
| ADR-6 | 自建注册 + JWT 认证替代托管 Auth          | 托管 Auth（如 Supabase Auth）绑定云服务；自建 BCrypt + JJWT 认证可本地独立运行、无外部依赖，课程演示环境更可控。 |
| ADR-7 | A-5 自定义图卡先用本地文件系统而非对象存储     | Storage SDK 接入与凭据管理成本高于 6/15 演示窗口可承担的范围；本地 `uploads/` + `WebConfig` 静态映射零依赖；接口契约（返回相对 URL）与对象存储同构，未来切换零业务改动。 |
| ADR-8 | JwtFilter 对公开路径短路放行无效 token        | 修复实测 bug：stale token 残留 localStorage 让登录端点直接返回 401。Spring `permitAll()` 不影响过滤器链，过滤器自己必须区分公开/受保护路径。 |
| ADR-9 | C-4 PDF 用浏览器 `window.print()` 替代服务端 PDF 库 | 课程周期内只需 1 张图 + 几行文字，引入 OpenHTMLToPDF / iText 等带样式损失风险，且会让后端膨胀。前端打印走 `@media print` 隐藏 chrome 后由浏览器生成原生 PDF（含图表、Top-3、周次），用户在打印对话框选"另存为 PDF"。零依赖、所见即所得、跨平台。 |
| ADR-10 | C-4 分享报告冻结快照存 `payload_json` 文本字段 | 替代方案是匿名访问时实时重算聚合，但①家庭数据 24h 内可能变（新增事件影响 chart/Top3），分享链接应展示生成时刻；②匿名读到时不应再触发任何鉴权-相关查询。冻结 JSON 简单、稳定、对 DB 友好。 |
| ADR-11 | C-5 dismissal 只抑制"当前正在进行的连续 streak" | 实现方式：以 streak 起点日期为锚点；若存在 dismissed_at ≥ streak 起点，则当次警报被压制。当一段连续期断开后再次组成新的 3 天连续期时，会自然产生 streak 起点更新，警报重新激活。这与 AC "关闭后不重复弹出直到新一轮触发" 一致。 |
| ADR-12 | C-6/C-7 隐私不依赖前端 — controller 层强校验 | child JWT 调 `/api/diary-entries/check-today` 拒绝；parent JWT 调 `/api/diary-entries` 拒绝（403）。即便前端被绕过或用第三方客户端，明细字段也永不外泄。 |
| ADR-13 | BehaviorEvent 用 JdbcTemplate 而非 JPA Entity | Postgres `text[]` 需要外部依赖（hypersistence-utils）或自定义 UserType 才能与 JPA 集成。JdbcTemplate 直接、无依赖；分析服务本就用 JdbcTemplate，统一一致。 |
| ADR-14 | Schedule completed_step_ids 存索引而非 card_id | 同一张卡（如"喝水"）可能在一个 schedule 出现两次。存 index 让"第 1 步完成 vs 第 4 步完成"语义无歧义；UI 复杂度不变。 |
| ADR-15 | /api/schedules/today 单次返回 template+instance+cards 全量 | 儿童端无 join 逻辑、不发额外请求；后端在 1 个 endpoint 内 join，符合儿童端"少网络、少 spinner"的可用性原则。 |
| ADR-16 | Sentence 表 schema 重构：去 child_id、加 family_id + sender_role | 原表把 sentence 绑在单个 child，无法表达家长回复。改成 family-scoped + sender role 后，对话双方共享同一 feed，且 family_id 强制从 JWT 注入（PRD §6.2 跨族隔离）。 |
| ADR-17 | PECS 对话用 3 s polling 而非 WebSocket | 课程项目周期不需要 push 即时性；HTTP polling 简单、零额外依赖、可调试。前端 `setInterval(refresh, 3000)` + 后端 `?sinceId=N` 增量端点保证 traffic 极小（只返回新增）。WebSocket 在 v4 路线再考虑。 |

---

## 9. 变更记录 (Change Log)

| 版本   | 日期         | 作者        | 变更         |
| ---- | ---------- | --------- | ---------- |
| v1.0 | 2026-05-18 | Xu Ziyang | 架构文档初稿     |
| v1.1 | 2026-06-01 | Xu Ziyang | 更新鉴权流程（JWT 登录端到端实现）；新增 H2 开发数据库；User 表增加 email/password_hash 字段；新增 ADR-5 |
| v1.2 | 2026-06-01 | Xu Ziyang | H2 → PostgreSQL 17 本地实例；新增注册端点 POST /api/auth/register；新增 ADR-6（自建认证）；新增 API 端点列表；新增 start.sh 一键启动 |
| v1.3 | 2026-06-03 | Xu Ziyang | Epic A 全部完成：新增 cards/sentences/uploads 端点列表与 multipart 约束；新增 ADR-7（A-5 本地文件系统）、ADR-8（JwtFilter 公开路径短路）；记录 client.ts 鉴权例外；Module A 状态改为 ✅ 已完成 |
| v1.3.1 | 2026-06-04 | Xu Ziyang | 日期刷新；API 端点表补充 `PATCH /api/cards/{id}`（A-5 上传即建卡后重命名）；状态改为 "Epic A 已完成，B 模块开发中" |
| v1.4 | 2026-06-04 | Xu Ziyang | **Epic C 全部完成**：§4.3 列出 5 个前端视图 + 6 个后端 controller 落地；API 表新增 13 条 C 模块端点；新增 ADR-9..13（打印 PDF / 快照 / dismissal 算法 / 隐私校验 / JdbcTemplate）；状态改为 "Epic A + Epic C 已完成，B 进行中" |
| v1.5 | 2026-06-04 | Xu Ziyang | **Epic B 全部完成**：§4.2 列出 ParentScheduleView + ChildScheduleView 落地与 6 条 schedule 端点；ADR-14（索引而非 card_id）、ADR-15（today 一次性返回 template+instance+cards）；状态改为 "A/B/C 全部完成" |
| v1.5.1 | 2026-06-04 | Xu Ziyang | 跨文档校对，无内容变更 |
| v1.6 | 2026-06-04 | Xu Ziyang | **A-2 升级为双向对话**：§4.1 加 A-2 扩展说明；API 表 sentence 端点三连（POST/GET/sinceId 增量）；ADR-16（sentence schema 改 family + sender_role）、ADR-17（3 s polling 替代 WebSocket） |
| v1.7 | 2026-06-13 | Xu Ziyang | **平台名与部署架构同步实际**：Supabase→Neon、Fly.io→Render（§2 顶层图、§3 技术栈表、§7 部署图全部更新）；文件存储改为本地 `uploads/` + 未来对象存储；ADR-1/ADR-4 记录平台变更，ADR-2 改为"应用层隔离（非 Supabase RLS）+ E2E/`/verify` 硬保证"，ADR-5/6/7 去 Supabase 专名改"托管 PG / 托管 Auth / 对象存储"。配套《Test Report v2.2》《Workflow v1.5》《Progress v2.0》 |
