# 🏗️ VisiTalk — 项目架构文档 (System Architecture)

| 项目        | VisiTalk — 自闭症儿童可视化沟通与情绪追踪平台 |
| --------- | ----------------------------- |
| 文档版本      | v1.1                          |
| 状态        | Sprint 1 进行中                     |
| 文档负责人     | Xu Ziyang (PO / 架构)           |
| 最后更新      | 2026-06-01                    |
| 关联文档      | `📦 VisiTalk — Product Requirements Document (PRD).md` |

---

## 1. 文档目的 (Purpose)

本文档描述 **VisiTalk** 的系统架构、模块划分、技术选型理由、数据流与部署拓扑，作为团队开发的技术参考基线。功能范围以 PRD 为准；本文档只回答"系统如何被构建"。

---

## 2. 架构总览 (Architecture Overview)

VisiTalk 采用**前后端分离 + BaaS 辅助**的三层架构：

- **表现层 (Frontend)**：单页应用 (SPA)，内含儿童模式与家长模式，通过 JWT 登录鉴权自动切换角色。
- **应用层 (Backend)**：Spring Boot 提供 REST API（JWT 鉴权），承载登录、图卡拼句、日程、行为分析与周报生成。
- **数据层 (Data)**：开发阶段使用 H2 内存数据库；生产环境使用 Supabase (PostgreSQL) 持久化 + RLS 行级安全；Supabase Storage 存储图卡与导出 PDF。

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
│                后端 API — Spring Boot 3 (Fly.io)             │
│   pecs 包  │  schedule 包  │  behavior 包  │  report 包       │
│  鉴权过滤器 (JWT)  ·  业务校验  ·  周报生成  ·  PDF 导出       │
└───────────────────────────┬─────────────────────────────────┘
                             │ SQL (RLS 强制)
                             ▼
┌─────────────────────────────────────────────────────────────┐
│              Supabase — PostgreSQL 15 + Auth + Storage        │
│   数据表 (见 §5)  ·  Row Level Security 策略  ·  对象存储桶    │
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
| 数据库（开发） | H2 In-Memory                                | 本地开发零配置，每次启动自动建表并种测试数据，无需 Supabase 即可跑通全流程。 |
| 数据库（生产） | Supabase (PostgreSQL 15)                    | 关系型保证数据一致性；自带 Auth 与 RLS，省去自建鉴权成本。                         |
| 行级安全   | Supabase Row Level Security                 | 在数据库层强制儿童/家长数据隔离 (见 §6)，比应用层校验更可靠。                         |
| 文件存储   | Supabase Storage                            | 与数据库同生态；存自定义图卡照片与导出的周报 PDF。                                |
| CI/CD  | GitHub Actions                              | 与代码仓库同平台；自动跑 lint / 测试 / 部署。                               |
| 前端部署   | Vercel                                      | 静态 SPA 部署快、自带 CDN 与预览环境。                                   |
| 后端部署   | Fly.io                                      | 容器化部署 Spring Boot，免运维，支持 staging / prod 分环境。               |
| 测试     | Vitest · Playwright · JUnit 5               | 单元 / E2E / 后端单元，详见 Workflow 文档。                            |

---

## 4. 模块架构 (Module Architecture)

系统按 PRD 三大功能模块分包，前后端均保持同名分层，降低认知成本。

### 4.1 Module A — Digital PECS Builder
- **前端**：`views/child/pecs/` — 图卡分类网格、句条区、拖拽逻辑。
- **后端**：`pecs` 包 — 图卡 CRUD、句子保存、自定义图卡上传校验。
- **关键依赖**：vue-draggable-plus；Supabase Storage (A-5 自定义图卡)。

### 4.2 Module B — Visual Schedule & Timeline
- **前端**：`views/child/schedule/` (儿童执行) + `views/parent/schedule/` (家长编排)。
- **后端**：`schedule` 包 — 模板 CRUD、日程实例、步骤完成状态。
- **数据**：`ScheduleTemplate` / `ScheduleInstance`。

### 4.3 Module C — Behavior Logger, Weekly Report & Child Diary
- **前端**：`views/parent/behavior/` (记录与周报) + `views/child/diary/` (儿童私人日记)。
- **后端**：`behavior` 包 (事件记录、连续触发检测) + `report` 包 (周报聚合、PDF 导出、分享链接)。
- **数据**：`BehaviorEvent` / `WeeklyReport` / `DiaryEntry`。
- **隐私要点**：`DiaryEntry` 明细对家长不可见 (见 §6)。

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

1. 用户输入 email + password → 前端调 `POST /api/auth/login`。
2. 后端 `AuthService` 查 `User` 表，`BCryptPasswordEncoder` 验密，`JwtUtil` 签发 JWT（含 `sub=userId`, `role`, `family_id`，24h 过期）。
3. 前端 `auth store` 将 token 存入 `localStorage`，Pinia 记录 `mode` / `familyId`。
4. 前端 `api/client.ts` 的 fetch 封装在每次请求时从 `localStorage` 取 token，自动带 `Authorization: Bearer <token>`。
5. 后端 `JwtFilter` 解析每个请求的 JWT，验证签名后提取身份信息注入 Spring Security Context。
6. 路由守卫 `router.beforeEach` 拦截未登录访问（检查 `localStorage` 无 token 则重定向到 `/`）。
7. 开发阶段使用 `DataInitializer`（`CommandLineRunner`）在启动时向 H2 自动种测试用户。

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
GitHub Actions ── lint + unit + e2e ──┐
   │                                  │ 全绿才部署
   ├──► Vercel        (前端 SPA)  ──► staging.visitalk  ──► prod
   └──► Fly.io        (后端 API)  ──► staging API       ──► prod
                                       │
                                       ▼
                              Supabase (staging / prod 各一实例)
```

- **环境**：`dev` (本地) / `staging` (合并 main 自动部署，对内演示) / `prod` (Release 末手动晋级)。
- 每个 Sprint 末打 tag `vR{n}-sprint{m}`。

---

## 8. 关键架构决策记录 (ADR 摘要)

| #     | 决策                                  | 理由                                      |
| ----- | ----------------------------------- | --------------------------------------- |
| ADR-1 | 采用 Supabase 而非自建 PostgreSQL + 鉴权服务  | 4 人团队，RLS 与 Auth 开箱即用，把精力留给业务功能。         |
| ADR-2 | 数据隔离放在数据库层 (RLS) 而非仅应用层             | 应用层校验易遗漏；RLS 是隐私需求 (儿童不被察觉) 的硬保证。        |
| ADR-3 | 移除 TTS 语音播报功能 (原 A-3)               | 目标用户对声音敏感，语音播报与产品愿景冲突，列入 v4 候选。          |
| ADR-4 | 前后端分别部署到 Vercel / Fly.io           | SPA 与 API 伸缩特性不同；分开部署各自取最优方案。            |
| ADR-5 | 开发阶段使用 H2 内存数据库替 Supabase        | Supabase 尚未创建；H2 零配置、启动即用，`create-drop` + `DataInitializer` 每轮重启自动重建测试数据，前端后端可独立联调。生产切换仅需改 `application.yml` 数据源。 |

---

## 9. 变更记录 (Change Log)

| 版本   | 日期         | 作者        | 变更         |
| ---- | ---------- | --------- | ---------- |
| v1.0 | 2026-05-18 | Xu Ziyang | 架构文档初稿     |
| v1.1 | 2026-06-01 | Xu Ziyang | 更新鉴权流程（JWT 登录端到端实现）；新增 H2 开发数据库；User 表增加 email/password_hash 字段；新增 ADR-5 |
