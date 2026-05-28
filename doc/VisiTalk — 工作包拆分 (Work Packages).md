# VisiTalk — 工作包拆分 (Work Packages)

| 项目 | VisiTalk |
|------|----------|
| 版本 | v1.0 |
| 日期 | 2026-05-28 |
| 说明 | 将全部 User Story 拆分为 4 份独立工作包，每份包含前端页面 + 后端 API + 验收标准。组员各自认领一份，并行推进。 |

---

## 总览

| 工作包 | 模块 | Sprint | 估点 |
|--------|------|--------|------|
| WP-1 | Module A — PECS Builder（可视化沟通） | Sprint 1 | 18 SP |
| WP-2 | Module B — Visual Schedule（可视化日程） | Sprint 1 + 2 | 21 SP |
| WP-3 | Module C — Behavior Logger & Child Diary（行为记录与日记） | Sprint 2 | 19 SP |
| WP-4 | Infrastructure & Quality（基础设施与质量保障） | Sprint 0–2 | — |

---

## WP-1: Module A — PECS Builder (18 SP)

**一句话**：让儿童能看图卡、拖图卡、拼句子表达需求；让家长能管理图卡库。

### 包含的 Story

| ID | Story | SP | 优先级 | Sprint |
|----|-------|----|--------|--------|
| A-1 | 儿童在主界面看到大尺寸图卡分类（吃/喝/玩/感受） | 5 | Must | Sprint 1 |
| A-2 | 儿童拖拽图卡到顶部句条拼出完整句子 | 8 | Must | Sprint 1 |
| A-4 | 家长在家长模式下增删/排序图卡 | 5 | Must | Sprint 1 |

> A-5（上传自定义图卡照片）本周期不做。

### 需要做的事情

**前端页面**
- `ChildPecsView.vue` — 儿童端主界面：4 分类图卡网格，图卡 ≥ 100x100px，无文字，对比度 ≥ 4.5:1
- `ChildSentenceBar.vue` — 顶部句条组件：接收拖入的图卡，按顺序排列，200ms 缩放反馈
- `ParentCardManager.vue` — 家长端图卡管理：列表展示、新增、删除（二次确认弹窗）、拖拽排序
- 使用 `vue-draggable-plus` 实现拖拽交互（A-2 核心依赖）

**后端 API**
- `GET /api/cards?family_id=X&category=Y` — 按分类获取图卡列表
- `POST /api/cards` — 新增图卡
- `DELETE /api/cards/{id}` — 删除图卡
- `PUT /api/cards/{id}/order` — 更新图卡排序
- `POST /api/sentences` — 保存拼好的句子
- `GET /api/sentences?child_id=X` — 获取历史句子

**数据表**
- `PictogramCard` — id, family_id, category, image_url, label_i18n, is_custom
- `Sentence` — id, child_id, card_ids[], created_at

**关键验收标准**
- 图卡拖拽后句条按顺序显示，无文字
- 误触 1 秒内不清空句条
- 删除操作需二次确认
- 修改后儿童端实时更新

---

## WP-2: Module B — Visual Schedule (21 SP)

**一句话**：让家长用图卡编排分步骤日程，儿童按高亮指引逐步完成任务并打勾。

### 包含的 Story

| ID | Story | SP | 优先级 | Sprint |
|----|-------|----|--------|--------|
| B-1 | 家长用图卡组合出分步骤日程（最多 10 步） | 8 | Must | Sprint 1 |
| B-2 | 儿童看到当前步骤高亮、其余变暗 | 5 | Must | Sprint 1 |
| B-3 | 儿童完成步骤后点击大复选框打勾，自动进入下一步 | 3 | Should | Sprint 2 |
| B-4 | 家长保存日程为模板以便重复使用 | 5 | Could | Sprint 2 |

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

## WP-3: Module C — Behavior Logger & Child Diary (19 SP)

**一句话**：让家长 3 次点击完成行为记录（不可被儿童察觉），儿童可在私人日记空间用图卡表达情绪。

### 包含的 Story

| ID | Story | SP | 优先级 | Sprint |
|----|-------|----|--------|--------|
| C-1 | 家长 3 次点击内完成一次事件记录（情绪强度+触发标签+时间） | 8 | Should | Sprint 2 |
| C-2 | 记录页面与儿童模式视觉完全分离，避免孩子察觉 | 3 | Should | Sprint 2 |
| C-6 | 儿童在私人日记空间用情绪图卡记录心情 | 5 | Could | Sprint 2 |
| C-7 | 家长开启或关闭儿童私人日记功能 | 3 | Could | Sprint 2 |

> C-3（周报）、C-4（PDF导出）、C-5（触发点提示）本周期不做。

### 需要做的事情

**前端页面**
- `ParentBehaviorView.vue` — 行为记录页（C-1+C-2）：
  - 3 步流程：选情绪强度（1-5 级）→ 选触发标签 → 自动提交
  - 整体配色/布局与儿童端完全不同（深色/专业风格）
  - 记录时长中位数 ≤ 15 秒
- `ChildDiaryView.vue` — 儿童日记（C-6）：
  - 5 种情绪图卡（开心/难过/愤怒/害怕/平静），无文字标签
  - 可选颜色涂鸦画布
- `ParentSettingsView.vue` — 家长设置中的日记开关（C-7）

**后端 API**
- `POST /api/behavior-events` — 记录行为事件
- `GET /api/behavior-events?child_id=X&from=Y&to=Z` — 查询历史记录
- `POST /api/diary-entries` — 儿童创建日记（仅 child role 可写）
- `GET /api/diary-entries/check-today?child_id=X` — 家长查询今日是否有记录（仅返回 bool，不返回内容）
- `PUT /api/family-settings/diary-enabled` — 开关儿童日记入口

**数据表**
- `BehaviorEvent` — id, parent_id, child_id, intensity(1-5), trigger_tags[], note_encrypted, occurred_at
- `DiaryEntry` — id, child_id, emotion_card_id, doodle_url, created_at

**RLS 隐私策略（本工作包重点）**
- `child` role 完全不能访问 `BehaviorEvent` 表
- `parent` role 对 `DiaryEntry` 只能读聚合字段（今日是否有记录），不能读 `emotion_card_id` / `doodle_url`
- 跨 `family_id` 访问任何表均被拒绝

**关键验收标准**
- 行为记录流程 ≤ 3 次点击
- 儿童端看不到任何家长记录入口
- 日记内容对家长完全不可见，只能看到"今天有/没有写日记"
- RLS 需有自动化测试覆盖

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
