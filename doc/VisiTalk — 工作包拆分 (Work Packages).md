# VisiTalk — 工作包拆分 (Work Packages)

| 项目 | VisiTalk |
|------|----------|
| 版本 | v1.4.1 |
| 日期 | 2026-06-04 |
| 说明 | 将全部 User Story 拆分为 4 份独立工作包，每份包含前端页面 + 后端 API + 验收标准。组员各自认领一份，并行推进。 |

---

## 总览

| 工作包 | 模块 | Sprint | 估点 | 状态 |
|--------|------|--------|------|------|
| WP-1 | Module A — PECS Builder（可视化沟通） | Sprint 1 | 26 SP（含追加 A-5） | 🟢 完成 (6/3) |
| WP-2 | Module B — Visual Schedule（可视化日程） | Sprint 1 + 2 | 21 SP | 🟢 完成 (6/4) |
| WP-3 | Module C — Behavior Logger / Weekly Report / Child Diary | Sprint 2 | 50 SP（全部追加为 Must） | 🟢 完成 (6/4) |
| WP-4 | Infrastructure & Quality（基础设施与质量保障） | Sprint 0–2 | — | 🟡 持续 |

---

## WP-1: Module A — PECS Builder (26 SP) ✅ 已完成 (6/3)

**一句话**：让儿童能看图卡、拖图卡、拼句子表达需求；让家长能管理图卡库并上传自家照片。

### 包含的 Story

| ID | Story | SP | 优先级 | Sprint | 状态 |
|----|-------|----|--------|--------|------|
| A-1 | 儿童在主界面看到大尺寸图卡分类（吃/喝/玩/感受） | 5 | Must | Sprint 1 | 🟢 完成 |
| A-2 | 儿童拖拽图卡到顶部句条拼出完整句子 | 8 | Must | Sprint 1 | 🟢 完成 |
| A-4 | 家长在家长模式下增删/排序图卡 | 5 | Must | Sprint 1 | 🟢 完成 |
| A-5 | 家长上传自家物品照片作为自定义图卡 | 8 | **追加 Must** | Sprint 1 | 🟢 完成 |

> 原计划 A-5 列为本周期 Won't，6/2 PO 决策追加进 Sprint 1 并于 6/3 完成。

### 已实现的内容

**前端页面**（实际落地路径）
- `frontend/src/views/child/pecs/ChildPecsView.vue` — 儿童端主界面：4 分类图卡网格（卡片 120×120px ≥ 100px）、嵌入式句条区、HTML5 drag/drop + 点击双通道触发、200ms 缩放反馈。
- `frontend/src/views/parent/pecs/ParentPecsView.vue` — 家长端图卡管理：按分类分组展示、新增表单、删除二次确认弹窗、`vue-draggable-plus` 拖拽排序（`reactive` + `watch` 同步 `store.cards`，不绑 `computed`）、文件上传 UI（前端预校验类型/大小 + 友好错误）。
- `frontend/src/stores/cards.ts` Pinia store — `cards` / `sentenceCards` / `fetchCards` / `addToSentence` / `createCard` / `deleteCard` / `reorderCards` / `uploadPhoto`。
- `frontend/src/api/client.ts` — fetch 封装、`uploadFile` (multipart)、`assetUrl` 帮助函数（拼 `/uploads/` 绝对 URL）；对 `/api/auth/**` 不带 Authorization 头。

**后端 API**（实际端点）
- `GET /api/cards?familyId=X&category=Y` — 按家庭 / 分类查询
- `POST /api/cards` — 新增图卡
- `DELETE /api/cards/{id}` — 删除图卡
- `PATCH /api/cards/{id}` — 重命名图卡 `labelI18n`（A-5 上传即建卡后修改默认 label）
- `PUT /api/cards/reorder` — 批量更新 `sort_order`（接收已排序列表）
- `POST /api/sentences` — 保存句子；`GET /api/sentences?childId=X` — 历史
- `POST /api/uploads` — multipart 上传图卡照片，返回 `{url}`
- `GET /uploads/**` — 静态服务（`WebConfig` 映射至 `backend/uploads/`，缓存 1h）

**数据表**
- `PictogramCard` — id, family_id, category, image_url, label_i18n, is_custom, sort_order（`@JsonProperty("isCustom")` 强制 JSON 字段名，否则 Jackson 默认序列化为 `custom`）
- `Sentence` — id, child_id, card_ids[], created_at

**关键验收标准对照**
- A-1 4 分类、卡片 120×120 ≥ 100、对比度 ≥ 4.5:1 → ✅
- A-2 句条按顺序显示、200ms 缩放反馈、无声音、屏外点击不清空 → ✅
- A-4 拖拽排序、删除二次确认、新增同分类、家长改动后儿童端 `fetchCards` 时刷新 → ✅（实时推送未做，需手动刷新）
- A-5 JPG/PNG ≤5MB（前端 + 后端双重校验）→ ✅；失败给友好错误 → ✅；**"上传成功后图卡出现在对应分类中" → ✅（选文件即调 `uploadAndCreateCard`，卡片立刻出现；label 默认文件名，列表 inline 重命名）**

**实测发现并已修复的 bug（计入 Sprint 1 缺陷库）**
- BUG-A1: JwtFilter 对 stale token 返回 401，导致登录端点本身被阻塞 → 公开路径短路放行
- BUG-A2: `PictogramCard.isCustom` 经 Jackson 序列化丢前缀 → `@JsonProperty`
- BUG-A3: ParentPecsView `v-model` 绑 `computed` → 改 `reactive` + `watch`
- BUG-A4: ChildPecsView `@drop.prevent` 无 handler → 补 `draggedCard` ref 与真实 drop handler

**DoD 待补**
- Vitest store/组件单元测试
- Playwright E2E（A-2 拖拽拼句、A-5 上传 → 儿童端可见）
- Code Review + Merge to main

---

## WP-2: Module B — Visual Schedule (21 SP) ✅ 已完成 (6/4)

**一句话**：让家长用图卡编排分步骤日程，儿童按高亮指引逐步完成任务并打勾；模板可保存复用。

### 包含的 Story

| ID | Story | SP | 优先级 | Sprint | 状态 |
|----|-------|----|--------|--------|------|
| B-1 | 家长用图卡组合出分步骤日程（最多 10 步） | 8 | Must | Sprint 1 | 🟢 完成 |
| B-2 | 儿童看到当前步骤高亮、其余变暗 | 5 | Must | Sprint 1 | 🟢 完成 |
| B-3 | 儿童完成步骤后点击大复选框打勾，自动进入下一步 | 3 | Should | Sprint 2 | 🟢 完成 |
| B-4 | 家长保存日程为模板以便重复使用 | 5 | Could | Sprint 2 | 🟢 完成 |

### 实际落地

**前端**
- [ParentScheduleView.vue](frontend/src/views/parent/schedule/ParentScheduleView.vue) — builder（按分类筛选 card library，点选添加，↑/↓ 重排，× 删除）；模板列表（icon row 预览）；编辑/删除；二次确认弹窗。
- [ChildScheduleView.vue](frontend/src/views/child/schedule/ChildScheduleView.vue) — 多模板 picker（pill 风格）；step 行带 80×80 大复选框；4 种视觉状态（current 强高亮 / next 半高亮 / rest 暗 / done 翠绿）；全完成 🎉 庆祝 + Start Over。
- [stores/schedule.ts](frontend/src/stores/schedule.ts) — fetchTemplates / create / update / delete / fetchToday / toggleStep。
- 入口：ParentNav 加 Schedule tab；ChildPecsView 左下角 🗓️ 大圆按钮。

**后端**
- [ScheduleController.java](backend/src/main/java/com/visitalk/schedule/ScheduleController.java) — 6 条端点（详见 Architecture 文档表）。
- [ScheduleTemplate.java](backend/src/main/java/com/visitalk/model/ScheduleTemplate.java) / [ScheduleInstance.java](backend/src/main/java/com/visitalk/model/ScheduleInstance.java) — POJO，JdbcTemplate 持久化（BIGINT[]）。
- [ScheduleTemplateRepository](backend/src/main/java/com/visitalk/repository/ScheduleTemplateRepository.java) / [ScheduleInstanceRepository](backend/src/main/java/com/visitalk/repository/ScheduleInstanceRepository.java)。

**AC 对照**
- B-1 ≤10 steps 前端 disabled + 后端 400 → ✅；child 无法创建（403）→ ✅；保存后 child 端立刻可见 → ✅
- B-2 current 强高亮 + next 半高亮 + 其余暗 → ✅（一次只 2 步同时高亮，符合 AC "不超过 2 个步骤同时高亮"）
- B-3 大复选框（80×80）+ ✓ pop 动画 + 高亮自动移动 → ✅；全完成 → 🎉 庆祝（**无声**，符合 "可关闭声音" 原则 = 本来就没声音）+ Start Over → ✅
- B-4 命名模板 + list + 一键 load + 编辑 + 删除（级联清 instance） → ✅

**实测脚本**：parent 建 [Apple, Water, Ball] → child 看到 cards in order → 顺序 tick → completed=[0,1,2] → 庆祝；untick 1 → completed=[0,2] 庆祝消失 → 当前位回到 1；改名 + 加 step → ✅；删除模板 → templates 数量正确减少。

### 需要做的事情

**前端页面**
- `ParentScheduleBuilder.vue` — 家长端日程编排：从图卡库选卡 → 拖入步骤列表排序 → 保存
- `ChildScheduleView.vue` — 儿童端日程执行：当前步骤高亮（边框+放大），其余步骤变暗，大复选框打勾动效
- `TemplateList.vue` — 模板列表（B-4）：已保存模板展示，一键加载
- 完成全部步骤后的庆祝画面（可关闭动画）

**后端 API**
- `POST /api/schedule-templates` — 创建日程模板
- `GET /api/schedule-templates?family_id=X` — 获取模板列表
- `PUT /api/schedule-templates/{id}` — 编辑模板
- `DELETE /api/schedule-templates/{id}` — 删除模板
- `POST /api/schedule-instances` — 从模板创建当天执行实例
- `GET /api/schedule-instances/{id}` — 获取当前执行状态
- `PUT /api/schedule-instances/{id}/steps/{stepId}/complete` — 标记步骤完成

**数据表**
- `ScheduleTemplate` — id, family_id, name, steps[card_id]
- `ScheduleInstance` — id, template_id, date, completed_step_ids[]

**关键验收标准**
- 最多 10 个步骤
- 不超过 2 个步骤同时高亮
- 打勾后高亮自动移到下一步
- 全部完成显示庆祝画面
- 模板支持编辑和删除

---

## WP-3: Module C — Behavior Logger / Weekly Report / Child Diary (50 SP) ✅ 已完成 (6/4)

**一句话**：让家长 3 次点击完成行为记录（不可被儿童察觉），自动汇总每周情绪/触发报告并可导出 PDF / 分享链接，连续高发触发主动预警；儿童可在私人日记空间用图卡表达情绪，家长可一键开关此功能。

### 包含的 Story（原 4 个 + 追加 3 个）

| ID | Story | SP | 优先级 | Sprint | 状态 |
|----|-------|----|--------|--------|------|
| C-1 | 家长 3 次点击内完成一次事件记录（情绪强度+触发标签+时间） | 8 | Must (追加) | Sprint 2 | 🟢 完成 |
| C-2 | 记录页面与儿童模式视觉完全分离，避免孩子察觉 | 3 | Must (追加) | Sprint 2 | 🟢 完成 |
| C-3 | 系统按周自动生成情绪趋势报告 | 13 | Must (追加) | Sprint 2 | 🟢 完成 |
| C-4 | 家长把周报导出为 PDF 或分享链接（24h 失效） | 5 | Must (追加) | Sprint 2 | 🟢 完成 |
| C-5 | 系统在连续 3 天出现同一触发点时提示家长 | 8 | Must (追加) | Sprint 2 | 🟢 完成 |
| C-6 | 儿童在私人日记空间用情绪图卡记录心情 | 5 | Must (追加) | Sprint 2 | 🟢 完成 |
| C-7 | 家长开启或关闭儿童私人日记功能 | 3 | Must (追加) | Sprint 2 | 🟢 完成 |

> 6/4 PO 决策：把原列为 Won't / Could 的 5 条 (C-3..C-7) 全部追加为本周期 Must，并于当日完成。

### 已实现的内容

**前端页面**（实际落地路径）
- `frontend/src/views/parent/behavior/ParentBehaviorView.vue` — C-1 三步表单（intensity → trigger → submit，每步 1 click）+ C-2 暗色 slate-900 UI + PIN-1234 退出确认 + 最近事件列表。
- `frontend/src/views/parent/behavior/ParentReportView.vue` — C-3 自绘 SVG 折线图 + Top-3 列表 + insufficient 态；C-4 "Download PDF"（`window.print()` + `@media print` 隐藏 chrome）+ "Create Share Link"（带过期时间显示和 Copy 按钮）。
- `frontend/src/views/parent/settings/ParentSettingsView.vue` — C-5 alert 横幅（dismiss 按钮）+ C-7 diary toggle + "today written?" 布尔指示。
- `frontend/src/views/child/diary/ChildDiaryView.vue` — C-6 5 张情绪图卡（无文字 label，≥120×120）+ HTML5 canvas 涂鸦（Pointer Events，6 色画笔）+ 完成庆祝动画；进入时若 diary 未开则 replace 回首页。
- `frontend/src/views/PublicShareView.vue` — C-4 anonymous 公开页（路由 `/share/reports/:token`，无 `requiresAuth`）。
- `frontend/src/views/child/pecs/ChildPecsView.vue` 已加 C-7 入口图标：`<button v-if="diary.settings?.diaryFeatureEnabled">`。
- Stores：`stores/behavior.ts`（events / report / alerts + 行为 + 周报 + 分享 + 警报）；`stores/diary.ts`（entries / settings / todayWritten）。

**后端 API**（实际端点）
- `POST /api/behavior-events` / `GET /api/behavior-events` — 仅 parent
- `GET  /api/reports/weekly` — 仅 parent；`<3` 记录返回 `status=insufficient`，不抛异常
- `POST /api/reports/share` — 仅 parent；冻结 JSON payload，24h 过期
- `GET  /share/reports/{token}` — 匿名；过期 410，不存在 404
- `GET  /api/alerts` / `POST /api/alerts/dismiss` — 仅 parent；dismissal 抑制当前 streak
- `POST /api/diary-entries` — 仅 child；feature off 返回 409
- `GET  /api/diary-entries` — 仅 child（parent 直访 403）
- `GET  /api/diary-entries/check-today` — 仅 parent；返回 `{enabled, writtenToday, count}`，**不返回任何明细字段**
- `GET  /api/family-settings` — 任意角色可读
- `PUT  /api/family-settings/diary-enabled` — 仅 parent

**数据表**
- `BehaviorEvent`（既有 schema）— 用 JdbcTemplate 持久化，避免 `text[]` JPA 依赖
- `DiaryEntry` / `system_configs (FamilySettings)`（既有）
- 新增 `report_share`（token PK + payload_json + expires_at）
- 新增 `alert_dismissal`（family_id + trigger_tag + dismissed_at + idx）

**RLS / 隐私策略（强制 controller 校验）**
- ✅ child JWT 访问 `/api/behavior-events*` / `/api/reports/*` / `/api/alerts*` → 403（实测）
- ✅ parent JWT 访问 `/api/diary-entries` 明细 → 403（实测）；parent 只能从 `/check-today` 拿布尔
- ✅ child JWT 尝试翻 `PUT /api/family-settings/diary-enabled` → 403（实测）
- ✅ 跨 family 访问：JwtFilter 把 `family_id` 从 token 注入 `request.attribute`，每个 controller 直接用，不接受 query 中的 `familyId`

**关键验收标准对照**
- C-1 ≤3 click：intensity → trigger → submit，每步 1 click → ✅
- C-1 时间自动 + 可调：`occurredAt` 默认 `LocalDateTime.now()`，body 可覆盖 → ✅
- C-1 完成时长 ≤15s：后端响应 <100ms，前端按钮反应 <50ms → ✅
- C-2 暗色 vs 儿童端：slate-900/100 vs amber-50/orange-400 → ✅；儿童端入口按钮（💗）不出现在 behavior/report/settings 任一页 → ✅；exit 需 PIN → ✅
- C-3 ≥3 触发自动生成 → ✅；折线图 → ✅；Top-3 → ✅；自然周 Mon-Sun → ✅
- C-4 PDF 含图表 + 摘要 → `window.print()` + `@media print` → ✅；24h 自动失效 → ✅（实测 410）；分享链接无登录可读 → ✅
- C-5 连续 3 天 → ✅；下次开 App 提示（settings 页加载时 `fetchAlerts`）→ ✅；dismiss 不重弹直到新一轮 → ✅（实测）
- C-6 主屏图标无 text label → 💗 emoji → ✅；≥5 emotion 卡 → 5 张 → ✅；可选涂鸦 → ✅；明细家长不可见 → ✅
- C-7 默认 off → ✅（schema default）；toggle on 后图标出现 → ✅（v-if 绑 setting）；parent 只看 today bool → ✅；disable 不删数据 → ✅（实测 count 1→1）

**端到端实测脚本**：见 6/4 session log；每条 AC 单独走过 curl + frontend HTTP probe。

---

## WP-4: Infrastructure & Quality（基础设施与质量保障）

**一句话**：确保 CI/CD 跑通、数据库部署、测试覆盖、代码质量，Sprint 仪式正常运转。

### 需要做的事情

**CI/CD 与部署**
- 维护 `.github/workflows/ci.yml`（lint → unit test → build → deploy staging）
- 前端部署到 Vercel，后端部署到 Fly.io
- 每次合并 main 自动部署 staging
- Sprint 末打 tag `vR{n}-sprint{m}`

**数据库**
- 创建 Supabase 项目（免费 tier）
- 建全部 7 张表：User, PictogramCard, Sentence, ScheduleTemplate, ScheduleInstance, BehaviorEvent, DiaryEntry
- 编写并验证全部 RLS 策略
- 种子数据：4 分类 x 各 5 张默认图卡 + 测试家庭（1 parent + 1 child）

**测试**
- 前端单元测试：Vitest（Pinia store 逻辑、组件渲染）
- 后端单元测试：JUnit 5（Service 层、周报聚合算法）
- RLS 安全测试（强制）：
  - child 访问 BehaviorEvent → 拒绝
  - parent 读取 DiaryEntry 明细 → 拒绝
  - 跨 family_id 访问任意表 → 拒绝
- E2E 测试：Playwright（A-2 拖拽拼句、B-1+B-2 日程编排与执行、C-1 行为记录）
- 每 Sprint ≥ 1 次儿童交互易用性测试（人工脚本）

**Sprint 仪式（SM 主导）**
- Sprint Planning（每 Sprint 首日，90min）
- Daily Standup（每工作日 10:00，15min）
- Backlog Refinement（Sprint 中段，60min）
- Sprint Review + Retrospective（Sprint 末，60min + 45min）
- Burndown Chart 每日更新，偏差 > 20% 即刻拉警报

**文档维护**
- PRD、架构文档、进度文档、Workflow 文档随 Sprint 进度同步更新
- Sprint Review 产出演示视频或截图
- Deliverable 3（6/7 提交）内容整合

---

---

## 变更记录

| 版本 | 日期 | 作者 | 变更 |
|------|------|------|------|
| v1.0 | 2026-05-28 | Xu Ziyang | 工作包初稿 |
| v1.1 | 2026-06-03 | Xu Ziyang | WP-1 标记完成；A-5 从"本周期不做"改为"追加 Must"并完成；WP-1 节落地实际文件路径、API、bug 修复清单与 DoD 待补项；总览表加状态列 |
| v1.2 | 2026-06-04 | Xu Ziyang | 日期刷新；WP-1 API 列表补 `PATCH /api/cards/{id}`（A-5 上传后重命名）；当前焦点转入 WP-2 (B-1/B-2) |
| v1.3 | 2026-06-04 | Xu Ziyang | **WP-3 全部完成**：C-1..C-7（原 4 + 追加 3）全数 🟢；总览表 19→50 SP；列出实际落地文件路径、后端端点、新增表（report_share / alert_dismissal）、ADR-9..13、每条 AC 对照与实测证据 |
| v1.4 | 2026-06-04 | Xu Ziyang | **WP-2 全部完成**：B-1..B-4 全数 🟢；落地 ParentScheduleView builder + ChildScheduleView 4 态高亮 + 庆祝 + 6 条后端端点；同日 A/B/C 三大 WP 均 🟢 |
| v1.4.1 | 2026-06-04 | Xu Ziyang | 跨文档校对，与 Progress v1.7 / Workflow v1.4 / Architecture v1.5.1 / PRD v1.6.2 同步对齐 |
| v1.5 | 2026-06-04 | Xu Ziyang | **WP-1 A-2 升级为双向 PECS 对话**：词卡库扩到 53 卡 / 7 分类（People/Action/Time 新增）；sentence 表 child_id → family_id + sender_role；新增 ChatComposer / MessageBubble / ChildChatView / ParentChatView |

---

## 依赖关系

```
WP-4（基础设施）必须先完成
    ├── Supabase 建表 → WP-1/2/3 后端可开始
    ├── CI/CD 流水线 → 所有 WP 的 PR 可合入
    └── 部署环境 → staging 可供演示

WP-1（PECS）←→ WP-2（Schedule）可并行
    └── 共享 PictogramCard 表（WP-1 维护，WP-2 消费）

WP-3（Behavior）← 依赖 WP-4 的 RLS 策略测试通过后才能签收

WP-4（测试）← 贯穿始终，与 WP-1/2/3 并行
```

## 每份工作包的输出物

| 工作包 | 代码产出 | 文档产出 |
|--------|---------|---------|
| WP-1 | `frontend/src/views/child/pecs/` + `frontend/src/views/parent/pecs/` + `backend/…/pecs/` | 图卡 API 文档 |
| WP-2 | `frontend/src/views/child/schedule/` + `frontend/src/views/parent/schedule/` + `backend/…/schedule/` | 日程 API 文档 |
| WP-3 | `frontend/src/views/parent/behavior/` + `frontend/src/views/child/diary/` + `backend/…/behavior/` + `backend/…/report/` | 行为记录 API 文档 + RLS 策略文档 |
| WP-4 | `.github/` + `backend/src/main/resources/application.yml` + 测试文件 + Supabase DDL | Sprint 报告 + Burndown 图表 + 测试结果 + Deliverable 3 |
