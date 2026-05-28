# 📦 VisiTalk — Product Requirements Document (PRD)

| 项目                    | VisiTalk — 自闭症儿童可视化沟通与情绪追踪平台                |
| --------------------- | ------------------------------------------------------------ |
| 文档版本              | v1.2                                                         |
| 状态                  | Draft — 待 Sprint 0 评审通过                                 |
| 文档负责人 (PO)       | Xu Ziyang                                                    |
| 团队                  | Xu Ziyang, Xu Zihe, Yuen KinNing, Ke Hongyi                  |
| 最后更新              | 2026-05-18                                                   |
| 相关资料              | `2026-03-17-Orange-ElevatorPitch-ImpactMapping-Personas-InterviewReports-V1.0.pdf` |
| 开发框架              | Scrum (2-week Sprint) + Story Mapping + Continuous Delivery  |

---

## 1. 文档目的 (Purpose)

本 PRD 定义 **VisiTalk** 在三轮 Release 内交付的产品范围、用户价值、功能需求、非功能需求与验收标准，并描述团队在 Agile (Scrum) 框架下的协作方式。所有 Sprint Backlog、Story 拆分、UI 原型与测试用例均以本文档为单一事实源 (Single Source of Truth)。

---

## 2. 产品愿景 (Product Vision)

> **For** 存在语言表达障碍的自闭症儿童 (3–10 岁) 及其主要照护者，
> **Who** 面临日常沟通断层、情绪触发不可预测、缺乏结构化记录工具的痛点，
> **VisiTalk is** 一款双端 (Child / Parent) 数字化辅助应用，
> **That** 通过可视化 PECS 沟通、结构化日程与隐私行为记录，把"无法言说"翻译为"可被理解、可被分析"的信息，
> **Unlike** 传统纸质 PECS 卡片或通用的家长记事 App，
> **Our product** 同时服务儿童的即时表达与家长的长期模式洞察，并以零文字、低误触的儿童交互为第一原则。

### 2.1 商业/影响目标 (Impact Mapping 摘要)

| Why (目标)                  | Who (角色)         | How (行为变化)                                | What (产品交付)                    |
| ------------------------- | ---------------- | ------------------------------------------ | ------------------------------ |
| 降低家庭沟通冲突频次          | 儿童                | 主动用图卡表达需求而非哭闹                                  | Digital PECS Builder           |
| 减少日常过渡焦虑              | 儿童                | 通过预知"接下来发生什么"建立安全感                            | Visual Schedule & Timeline    |
| 让家长能向医生/治疗师提供结构化证据   | 家长                | 持续记录触发点 → 形成可分享的趋势报告                          | Behavior Logger + Weekly Report |

---

## 3. 目标用户与画像 (Personas)

### 3.1 主用户 — 儿童端 (Child User)

| 维度        | 描述                                                         |
| --------- | ---------------------------------------------------------- |
| 代表画像      | **小宇**，6 岁，ASD 二级，最小词汇量 < 20                              |
| 设备        | 家长共享的平板 (iPad / Android tablet)                            |
| 关键能力 / 限制 | 视觉学习者；文字识别能力弱；对屏幕动效/声音过敏；易因误触产生挫败              |
| 核心需求      | 用最少操作表达"我想要 X"、"我现在难受"、"下一步做什么"                          |
| 设计禁忌      | 无文字主导界面、无突兀音效、无模糊图形、无超过 2 层导航                            |

### 3.2 次级用户 — 家长端 (Parent User)

| 维度    | 描述                                              |
| ----- | ----------------------------------------------- |
| 代表画像  | **林女士**，35 岁，全职妈妈，每日记录孩子情绪 1–3 次，平均碎片时间 < 2 分钟 |
| 设备    | 自己的手机；偶尔使用平板                                    |
| 核心需求  | 在不打扰孩子的情况下快速记录；查看趋势；与治疗师/医生分享                   |
| Pain  | 纸质记录易丢失；通用笔记 App 无法做分类聚合；夫妻/祖辈难以同步              |

### 3.3 间接受益者 (Stakeholders)

- 言语/行为治疗师 (BCBA)、特教老师、儿童精神科医生 — 接收周报。
- 学校行政 — 未来可能接入 (Out of Scope for v1)。

---

## 4. 范围 (Scope)

### 4.1 In Scope (v1.0 – v3.0)

- 双端单 App (儿童模式 / 家长模式，通过 PIN 切换)。
- Web 优先 (Vue.js, 响应式)，平板浏览器 + 手机浏览器适配。
- 中英双语 (i18n key 化，首发简体中文)。

### 4.2 Out of Scope (本周期不做)

- 原生 iOS / Android 打包发布。
- AI 自动生成图卡 / 语音合成 TTS (列入 v4 候选)。
- 多家庭/学校多租户管理。
- 离线 PWA 完整能力 (仅做最小可用缓存)。

---

## 5. 核心功能模块 (Features) 与 用户故事

下列功能在 Story Mapping 中按 "用户活动 → 任务 → 故事" 三层拆解，所有故事均使用 INVEST 原则书写，并附验收标准 (AC, Gherkin 风格)。

### 5.1 Module A — Digital PECS Builder (可视化沟通系统)

**User Activity**：儿童在需要时表达自己的需求或感受。

| ID    | User Story                                                   | 优先级    | 估点 (SP) | Release |
| ----- | ------------------------------------------------------------ | ------ | ------- | ------- |
| A-1   | 作为儿童，我希望在主界面看到大尺寸、字面化的图卡分类 (吃/喝/玩/感受)，以便快速找到想表达的内容。 | Must   | 5       | R1      |
| A-2   | 作为儿童，我希望把图卡拖拽到顶部句条上，自动拼成"我想要 + 苹果"，以便表达完整意图。           | Must   | 8       | R1      |
| A-4   | 作为家长，我希望在家长模式下增删/排序图卡，以便贴合我家孩子的真实词汇量。                       | Must   | 5       | R1      |
| A-5   | 作为家长，我希望上传自家物品的照片作为自定义图卡，以提高识别率。                              | Could  | 8       | R2      |

**示例验收标准 (A-2)**

```
Given 儿童在主界面已选中"我想要"图卡
When 他用手指拖动"苹果"图卡至句条区域
Then 句条上按顺序显示两张图卡且无文字
And 系统在 200ms 内给出温和的视觉反馈 (轻微缩放, 无声音)
And 误触屏幕其他区域 1 秒内 不会清空句条
```

### 5.2 Module B — Visual Schedule & Timeline (可视化日程)

**User Activity**：儿童完成日常多步骤任务并降低过渡焦虑。

| ID    | User Story                                                | 优先级    | 估点 | Release |
| ----- | ------------------------------------------------------- | ------ | -- | ------- |
| B-1   | 作为家长，我希望用图卡组合出一个分步骤日程 (例：去超市的 5 步)，以便孩子预知流程。              | Must   | 8  | R1      |
| B-2   | 作为儿童，我希望看到"现在/下一步"用高亮标识，以便聚焦当前任务。                          | Must   | 5  | R1      |
| B-3   | 作为儿童，我希望完成一步后点击大复选框打勾，以获得完成感。                              | Should | 3  | R2      |
| B-4   | 作为家长，我希望保存日程模板 (上学日 / 周末 / 看医生)，以便重复使用。                    | Should | 5  | R2      |

### 5.3 Module C — Behavior Logger, Weekly Report & Child Diary (行为记录、周报与儿童私人日记)

**User Activity**：(1) 家长快速记录情绪事件 → 系统生成趋势 → 与医生分享；(2) 儿童在私人日记空间用图卡记录情绪，家长仅知晓是否有记录但不可查看内容。

| ID    | User Story                                              | 优先级    | 估点 | Release |
| ----- | ----------------------------------------------------- | ------ | -- | ------- |
| C-1   | 作为家长，我希望 3 次点击内完成一次"事件记录" (情绪强度 + 触发标签 + 时间)，以便不打断照护。       | Must   | 8  | R2      |
| C-2   | 作为家长，我希望记录页面与儿童模式视觉完全分离，避免孩子察觉。                            | Must   | 3  | R2      |
| C-3   | 作为家长，我希望按周自动生成情绪强度时间分布图与高频触发点 Top 3，以便就医时直接展示。              | Must   | 13 | R3      |
| C-4   | 作为家长，我希望把周报导出为 PDF 或一次性分享链接，以便给治疗师查看。                       | Should | 5  | R3      |
| C-5   | 作为家长，我希望系统在连续 3 天出现同一触发点时给我提示，以提前干预。                        | Could  | 8  | R3      |
| C-6   | 作为儿童，我希望进入私人日记空间，用情绪图卡或颜色涂鸦记录今天的心情，以便用自己的方式表达感受。           | High   | 5  | R2      |
| C-7   | 作为家长，我希望在家长模式下开启或关闭儿童私人日记功能，以控制是否展示给孩子。                      | Medium | 3  | R2      |

**示例验收标准 (C-6)**

```
Given 家长已在家长模式下开启儿童日记功能
When 儿童从主界面点击日记图标进入日记空间
Then 显示至少 5 种情绪图卡（开心/难过/愤怒/害怕/平静）供选择，无文字标签
And 选择情绪图卡后可叠加颜色涂鸦（可选步骤）
And 日记记录仅儿童可见；家长端只能看到"孩子今天有/没有写日记"，不可查看内容
```

---

## 6. 非功能需求 (Non-Functional Requirements)

| 类别           | 要求                                                                          |
| ------------ | --------------------------------------------------------------------------- |
| 性能           | 儿童端任意点击/拖拽响应 ≤ 200 ms；首屏 LCP ≤ 2.5 s (中端平板, 4G)。                            |
| 可用性          | 儿童端最大导航深度 ≤ 2；任何破坏性操作 (清空/退出) 需家长 PIN 二次确认。                                   |
| 无障碍          | 符合 WCAG 2.1 AA；色弱友好；图卡对比度 ≥ 4.5:1；可关闭一切动效与声音。                                |
| 安全 / 隐私       | 儿童画像与家长记录在 DB 层逻辑隔离；行为记录字段端到端加密；遵循 GDPR 与 中国《个人信息保护法》对未成年人数据的最小化原则。 |
| 可靠性          | 行为记录写入失败需本地草稿持久化，恢复网络后自动重试 ≥ 3 次。                                            |
| 兼容性          | Chrome / Safari 最近两个大版本；iPad Safari (≥ iPadOS 15)。                          |
| 国际化          | 全部用户可见文案走 i18n key，禁止硬编码。                                                   |
| 可观测性         | 关键事件 (拖拽完成率、记录提交时长、周报打开率) 上报至自建埋点表。                                          |

---

## 7. 信息架构与数据模型 (High-Level)

```
User (id, role: child|parent, family_id, pin_hash)
PictogramCard (id, family_id, category, image_url, label_i18n, is_custom)
Sentence (id, child_id, card_ids[], created_at)
ScheduleTemplate (id, family_id, name, steps[card_id])
ScheduleInstance (id, template_id, date, completed_step_ids[])
BehaviorEvent (id, parent_id, child_id, intensity 1-5, trigger_tags[], note_encrypted, occurred_at)
WeeklyReport (id, child_id, week_start, metrics_json, pdf_url)
DiaryEntry (id, child_id, emotion_card_id, doodle_url, created_at)
```

> 数据权限关键约束：`role = child` 的会话**只能**读到本 `family_id` 下的 PECS / Schedule / DiaryEntry 数据，**永远不能**读到 `BehaviorEvent`。`role = parent` 的会话对 `DiaryEntry` 只能读取聚合字段（当日是否存在记录），**不可读取** `emotion_card_id` 与 `doodle_url` 明细。在 Supabase Row Level Security (RLS) 中以策略形式强制。

---

## 8. 技术栈 (Tech Stack)

| 层    | 选型                                            | 备注                                 |
| ---- | --------------------------------------------- | ---------------------------------- |
| 前端   | Vue 3 + Vite + Pinia + Tailwind                | 拖拽用 `vue-draggable-plus`           |
| 后端   | Spring Boot 3 (Java 17)                       | REST + JWT；按模块分包 `pecs/schedule/behavior` |
| 数据库  | Supabase (PostgreSQL 15) + Row Level Security | 直接复用 Supabase Auth                 |
| 文件   | Supabase Storage                              | 图卡/导出 PDF                          |
| CI/CD | GitHub Actions → Vercel (前端) / Fly.io (后端)    | 每次合并 main 自动部署到 staging            |
| 测试   | Vitest + Playwright (E2E) + JUnit 5 (后端)      | 儿童交互专用易用性测试脚本                      |

---

## 9. 发布计划 (Release Roadmap)

> 本项目为课程周期项目，硬性截止日 **2026-06-15**（演示日），Deliverable 3 于 **2026-06-07** 提交。可用开发周期约 4 周，Release 计划据此压缩为 1 个筹备 Sprint + 2 个开发 Sprint。

| Release            | 时间窗口               | Sprint              | 主题 / 验收门槛                                              |
| ------------------ | ------------------ | ------------------- | ------------------------------------------------------ |
| **R0 — 筹备**        | 2026-05-13 ~ 05-26 | Sprint 0            | 团队组建、PRD 评审通过、技术骨架跑通 "Hello VisiTalk" 端到端              |
| **R1 — MVP**       | 2026-05-27 ~ 06-09 | Sprint 1 (2 周)      | A-1, A-2, A-4, B-1, B-2 全部 Done；MVP 可演示；Deliverable 3 于 06-07 提交 |
| **R2 — 增强 + 收尾**   | 2026-06-10 ~ 06-15 | Sprint 2 (缩短至 6 天)  | C-1, C-2, B-3 等增强项视实际 Velocity 交付；缺陷收敛；演示与文档定稿          |

> 受 06-15 硬截止约束，Sprint 2 缩短为 6 天（非标准 2 周），仅承诺约半个 Sprint 的容量。
> 任何 Release 的"完成"必须同时满足 §10 的 **Definition of Done** 与对应 Sprint Goal。

### 9.1 本周期范围取舍 (MoSCoW for This Cycle)

由于周期仅 4 周，按以下优先级在 6/15 前压缩交付；最终纳入 Sprint 2 的具体故事在 Sprint 2 Planning 时按 Sprint 1 实际 Velocity 决定。

| 级别              | Story                     | 说明                                          |
| --------------- | ------------------------- | ------------------------------------------- |
| **Must**        | A-1, A-2, A-4, B-1, B-2   | R1 MVP，演示与 Deliverable 3 的核心，Sprint 1 必交付    |
| **Should**      | C-1, C-2, B-3             | Sprint 2 优先增强项：行为记录闭环 + 日程打勾                 |
| **Could**       | C-6, C-7, B-4             | Sprint 2 拉伸项，有余力才做                           |
| **Won't (本周期)** | A-5, C-3, C-4, C-5        | 工作量过大或依赖长期数据积累（如 C-3 周报需数周真实记录），列入产品未来路线，不在 6/15 前实现 |

---

## 10. Agile 工作流 (Scrum Framework)

### 10.1 团队角色 (Scrum Roles)

| 角色                   | 担任者          | 主要职责                                                       |
| -------------------- | ------------ | ---------------------------------------------------------- |
| **Product Owner (PO)** | Xu Ziyang    | 维护 Product Backlog 优先级；本 PRD 的最终决策人；与家长用户保持联络。            |
| **Scrum Master (SM)**  | Ke Hongyi    | 主持 Scrum 仪式；移除阻塞；维护 Burndown 与 Velocity；保障流程纪律。            |
| **Dev Team — 前端 / UX** | Yuen KinNing | 儿童端 PECS / Schedule 交互、UI 资产、可用性测试脚本。                      |
| **Dev Team — 后端 / 数据** | Xu Zihe      | Spring Boot API、Supabase RLS、Behavior 分析与周报算法。             |
| **Dev Team — 全栈 / 架构** | Xu Ziyang    | (PO 兼任) 端到端联调、CI/CD、数据库 schema、跨模块技术决策。                    |

> 由于团队仅 4 人，PO 兼一名 Developer；SM 同时承担 QA 主导。所有人在 Sprint 期内均承担测试责任。

### 10.2 Sprint 节奏 (2 周一个 Sprint)

| 仪式 (Ceremony)               | 时长       | 频率       | 输出                                |
| --------------------------- | -------- | -------- | --------------------------------- |
| **Sprint Planning**         | 90 min   | 每 Sprint 开始 (周一) | 明确 Sprint Goal + Sprint Backlog (估点确认) |
| **Daily Standup**           | 15 min   | 每工作日 10:00 | 昨天 / 今天 / 阻塞 三句话                  |
| **Backlog Refinement**      | 60 min   | Sprint 中段 (周四) | 下个 Sprint 候选故事拆分、估点、补 AC          |
| **Sprint Review**           | 60 min   | Sprint 末 (周五上午) | 向 PO + 至少 1 名外部用户 Demo 增量          |
| **Sprint Retrospective**    | 45 min   | Sprint 末 (周五下午) | "Keep / Drop / Try" 三栏行动项，进入下个 Sprint |

### 10.3 工件 (Artifacts)

1. **Product Backlog** — 由本 PRD §5 全部 Story 组成，PO 排序，GitHub Projects "Backlog" 列。
2. **Sprint Backlog** — 当前 Sprint 承诺完成的 Story 子集 + 拆出的技术子任务，"In Progress / Review / Done" 列。
3. **Increment** — 每 Sprint 末部署到 staging 的可演示版本，必须满足 DoD。
4. **Burndown Chart** — SM 每日更新剩余 SP，发现偏差 > 20% 立即在 Standup 中拉警报。
5. **Impediment Log** — 由 SM 维护的阻塞清单 (问题、负责人、解决目标日期)。

### 10.4 Definition of Ready (DoR) — 故事进入 Sprint 的前置

- [ ] 有清晰的 "As a / I want / So that" 用户价值描述。
- [ ] 至少 1 条 Gherkin 形式的验收标准。
- [ ] 估点完成 (Planning Poker, Fibonacci)，且 ≤ 13 SP；超出则必须拆分。
- [ ] 涉及 UI 的故事，已有低保真原型 (Figma 链接) 附在 Issue 中。
- [ ] 依赖项已识别且不阻塞本 Sprint。

### 10.5 Definition of Done (DoD) — 故事 / 增量"完成"标准

- [ ] 代码合入 `main`，CI (lint + unit + e2e) 全绿。
- [ ] 单元测试覆盖关键分支，前端核心交互有 Playwright E2E 用例。
- [ ] 至少 1 位团队成员 Code Review 通过。
- [ ] 所有验收标准被自动化或手动验证通过。
- [ ] 部署至 staging，PO 在 Sprint Review 上接受。
- [ ] 文档 (PRD / API Docs / Storybook 资产) 已同步更新。
- [ ] 无新增 P0/P1 缺陷。

### 10.6 估点 (Estimation)

- 使用 **Planning Poker** + **Fibonacci** (1, 2, 3, 5, 8, 13)。
- 1 SP ≈ 半个工作日的"理想工作量"参考，但只与历史故事相对比较，不与人天直接换算。
- 团队初始 Velocity 假设为 25 SP / 标准 2 周 Sprint；Sprint 1 结束后即用实际 Velocity 校准 Sprint 2 的承诺量（Sprint 2 仅 6 天，按比例约 12–15 SP）。

### 10.7 分支与代码协作流

- 主分支 `main` 保护，必须 PR + Review + CI 绿。
- 分支命名：`feat/<story-id>-short-slug`, `fix/<bug-id>`, `chore/...`。
- PR 标题包含 Story ID；Description 必须包含「关联 Story / 变更说明 / 测试步骤 / 截图 (UI)」四节。
- 每个 Sprint 末打 tag `vR{n}-sprint{m}`。

### 10.8 风险管理 (Risk Register, Top 5)

| #   | 风险                              | 概率 | 影响 | 缓解                                       |
| --- | ------------------------------- | -- | -- | ---------------------------------------- |
| R1  | 难以招募到自闭症家庭做真实用户测试         | 高  | 高  | Sprint 0 即联系本地特教机构；备用方案：邀请儿童康复师做代理评估。     |
| R2  | 儿童端误触导致负向情绪 (项目愿景反向)        | 中  | 高  | 设"破坏性操作需 PIN"原则；做易用性专项测试，每 Sprint 至少 1 次。 |
| R3  | 4 人团队 Velocity 被高估              | 高  | 中  | 第 2 个 Sprint 起以实际 Velocity 重排 Backlog。   |
| R4  | Supabase RLS 配置错误导致数据越权        | 低  | 极高 | 强制 RLS 单元测试；上线前安全自查 checklist。           |
| R5  | PO 兼 Dev 导致优先级与实现冲突            | 中  | 中  | Sprint Review 邀请外部 mentor 旁听，制衡决策。       |

### 10.9 度量 (Metrics)

| 指标                         | 目标                                     |
| -------------------------- | -------------------------------------- |
| Sprint 承诺达成率              | ≥ 85%                                  |
| 缺陷逃逸率 (上线后 P0/P1 / Sprint) | ≤ 1                                    |
| 儿童端任务首达成率 (可用性测试)         | R1 ≥ 60%，R2 ≥ 75%，R3 ≥ 85%             |
| 家长记录完成时长中位数               | ≤ 15 秒 (R2 验收门槛)                       |

---

## 11. 团队职责矩阵 (RACI 摘要)

| 工作项           | Xu Ziyang (PO/全栈) | Xu Zihe (后端/数据) | Yuen KinNing (前端/UX) | Ke Hongyi (SM/QA) |
| ------------- | ----------------- | --------------- | -------------------- | ----------------- |
| Backlog 优先级   | **A**             | C               | C                    | C                 |
| 技术架构决策        | **R**             | C               | C                    | I                 |
| 数据库 & RLS     | C                 | **R/A**         | I                    | I                 |
| 儿童端 UI/交互     | C                 | I               | **R/A**              | C (易用性)           |
| 行为分析 & 周报     | C                 | **R/A**         | C (可视化)              | C                 |
| Sprint 仪式主持   | I                 | I               | I                    | **R/A**           |
| 测试策略 & QA     | C                 | C               | C                    | **R/A**           |
| 与外部用户/治疗师沟通 | **R**             | I               | C                    | C                 |

> R=Responsible, A=Accountable, C=Consulted, I=Informed

---

## 12. 未决事项 (Open Questions)

1. 图卡素材的版权来源：自绘 vs. 开源 PECS 资源 (如 ARASAAC) — 需在 Sprint 0 内确认。
2. 多孩子家庭 (一对父母两个孩子) 是否纳入 R3 —— 待用户访谈确认需求频率。
3. 周报中是否引入"打分式"治疗建议 —— 涉及医疗合规边界，倾向于不做，待 PO 决策。
4. PIN 找回机制：邮箱重置 vs. 安全问题 —— UX 侧待决定。

---

## 13. 附录 (Appendix)

- **A. Story Map**：参见 `docs/story-map.md` (Sprint 0 内补齐)。
- **B. Figma 原型**：链接占位 (Sprint 0 内补齐)。
- **C. 用户访谈原始记录**：见 `2026-03-17-Orange-ElevatorPitch-ImpactMapping-Personas-InterviewReports-V1.0.pdf`。
- **D. 变更记录**：

| 版本     | 日期         | 作者         | 变更                                       |
| ------ | ---------- | ---------- | ---------------------------------------- |
| v0.1   | 2026-03-17 | 团队         | 项目计划书初稿 (Elevator Pitch / 三大模块 / 团队分工)   |
| v1.0   | 2026-05-13 | Xu Ziyang  | 改写为完整 PRD；引入 Scrum 工作流、User Story、AC、DoR/DoD、Risk |
| v1.1   | 2026-05-18 | Xu Ziyang  | 移除 A-3 (TTS 播放按钮，与声音敏感原则冲突)；Module C 新增 C-6/C-7 儿童私人日记；更新数据模型与 Release Roadmap |
| v1.2   | 2026-05-18 | Xu Ziyang  | 配合 6/15 课程硬截止，Release 压缩为 1 筹备 + 2 开发 Sprint；新增 §9.1 本周期 MoSCoW 取舍；更新估点校准说明 |
