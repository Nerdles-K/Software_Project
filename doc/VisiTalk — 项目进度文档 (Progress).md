# 📊 VisiTalk — 项目进度文档 (Project Progress)

| 项目        | VisiTalk — 自闭症儿童可视化沟通与情绪追踪平台 |
| --------- | ----------------------------- |
| 文档版本      | v1.7                          |
| 文档负责人     | Ke Hongyi (Scrum Master)      |
| 最后更新      | 2026-06-04                    |
| 关联文档      | PRD、项目架构文档、Workflow 文档        |
| 项目跟踪工具    | Jira Scrum 项目 (Board: VisiTalk SCRUM) |

---

## 1. 文档目的 (Purpose)

本文档实时跟踪 VisiTalk 的整体进度，包括 Release 路线、Sprint 状态、Story 看板、速度与风险。SM 每个 Sprint 末更新，是团队对外汇报与 Deliverable 提交的依据。

---

## 2. 关键里程碑 (Milestones)

> 项目硬性截止：**2026-06-15**（演示日）。整体开发周期约 4 周。

| 里程碑                  | 计划日期           | 状态     |
| -------------------- | -------------- | ------ |
| PRD 评审通过             | 2026-05-18     | 🟢 完成 |
| 技术骨架跑通 (Hello VisiTalk) | 2026-05-26  | 🟢 完成 |
| 登录流程端到端              | 2026-06-01     | 🟢 完成 |
| **Epic A (PECS) 全部完成** | **2026-06-03** | 🟢 完成 |
| **Epic C (Behavior + Diary + Report) 全部完成** | **2026-06-04** | 🟢 完成 |
| **Epic B (Visual Schedule) 全部完成** | **2026-06-04** | 🟢 完成 |
| R1 — MVP 完成          | 2026-06-09     | 🟢 完成（提前 5 天） |
| **Deliverable 3 提交** | **2026-06-07** | 🟡 演示物料定稿中 |
| R2 — 增强 + 收尾 完成      | 2026-06-15     | 🟢 功能侧已完成；待测试/演示收尾 |
| **项目展示 (Presentation)** | **2026-06-15** | ⬜ 未开始 |

---

## 3. Release 路线进度 (Release Roadmap)

| Release        | 时间窗口             | Sprint            | 进度     | 主要内容                     |
| -------------- | ---------------- | ----------------- | ------ | ------------------------ |
| R0 — 筹备        | 2026-05-13~05-26 | Sprint 0          | 🟢 完成 | 团队组建、PRD 评审、技术骨架、登录流程    |
| R1 — MVP       | 2026-05-27~06-09 | Sprint 1 (2 周)    | 🟢 完成 | A-1/A-2/A-4/A-5(追加)、B-1/B-2 |
| R2 — 增强 + 收尾   | 2026-06-10~06-15 | Sprint 2 (缩短 6 天) | 🟢 功能侧完成 | B-3/B-4 + C-1..C-7（C-3/4/5 同样追加为 Must） |

图例：🟢 完成 · 🟡 进行中 · ⬜ 未开始

> 6/4 单日大幅赶工：Epic A（A-5 追加 / 上传即建卡）、Epic B（B-1..B-4 含模板 + 庆祝）、Epic C（C-1..C-7 含原 Won't 的 C-3/C-4/C-5）全部端到端验证完成。R1+R2 范围功能侧 100% 达成；接下来重心切到测试覆盖（Vitest + Playwright）、演示脚本、Deliverable 3 物料定稿。距 6/7 D3 提交剩 **3 天**，6/15 演示剩 **11 天**。原 Won't（R3 段）全部上线，本周期 Won't 列表清空。

---

## 4. 当前 Sprint 状态 (Current Sprint)

| 项目          | 内容                                          |
| ----------- | ------------------------------------------- |
| Sprint      | Sprint 2 (R2 收尾)                            |
| 周期          | 2026-06-10 ~ 2026-06-15（功能侧已于 6/4 完成）       |
| Sprint Goal | 测试覆盖 + Deliverable 3 + 演示物料；无功能新增           |
| 已完成 SP     | 91 SP（Sprint 1 39 + Sprint 2 提前 52）         |

### Story 完成快照 (截至 6/4)
| Epic / 任务                                | 负责人          | 状态      |
| ---------------------------------------- | ------------ | ------- |
| Sprint 0 基础设施 / CI/CD / 登录注册 / start.sh   | Xu Ziyang    | 🟢 完成   |
| Epic A — A-1 / A-2 / A-4 / A-5(追加)       | Xu Ziyang / Yuen KinNing | 🟢 完成 |
| Epic B — B-1 / B-2 / B-3 / B-4           | Xu Ziyang / Yuen KinNing | 🟢 完成 |
| Epic C — C-1..C-7 (含原 Won't 的 C-3/4/5)    | Xu Ziyang / Xu Zihe      | 🟢 完成 |
| Vitest 单元测试（DoD 要求）                       | Yuen KinNing | ⬜ 待开始 |
| Playwright E2E（A-2 拖拽 / B-3 打勾 / C-1 记录） | Ke Hongyi    | ⬜ 待开始 |
| Supabase 生产库迁移                           | Xu Zihe      | ⬜ 待开始 |
| Jira Story 录入 Backlog                    | Ke Hongyi    | 🟡 进行中 |
| Deliverable 3 演示视频 + 截图                  | Xu Ziyang    | ⬜ 待开始 |
| Teams 频道发送技术栈 + Jira 链接                  | Xu Ziyang    | 🟡 待确认 |

> 6/4 一日内拿下 Epic B 全部 + Epic C 全部（含 PO 决策追加的 5 条原 Won't）。本地 PostgreSQL 17 + Spring Boot + Vite 三件套全跑通；Supabase 生产环境在 D3 前迁移即可。下一步压力在测试覆盖与演示物料定稿。

### Epic A 完成增量 (6/2–6/3)
- A-1 主界面：4 分类网格、卡片 120×120px、对比度 ≥ 4.5:1。
- A-2 拖拽拼句：HTML5 drag/drop + 点击双通道、200ms 缩放反馈、句条不会被屏外误触清空。
- A-4 家长 CRUD：vue-draggable-plus 拖拽排序持久化 (PUT /api/cards/reorder)、删除二次确认。
- A-5 自定义图卡：
  - ✅ 后端 `POST /api/uploads` (JPG/PNG ≤5MB) + `WebConfig` 静态服务 `/uploads/**`。
  - ✅ 前端文件类型/大小双重校验 + 友好错误提示。
  - ✅ **上传即建卡**：选完文件直接 `uploadAndCreateCard()`（label 默认文件名去扩展名），卡片立即出现在所选分类。儿童端 `<img>` 渲染端到端通过。
  - ✅ 新增 `PATCH /api/cards/{id}` + 列表内 inline 重命名（家长可把默认 label 改成更直观名称）。
- 端到端验证发现并修复 4 个 bug：
  - JwtFilter 对带 stale token 的登录请求误返回 401（前端 localStorage 残留 token 即无法登录）→ public path 短路放行。
  - `PictogramCard.isCustom` 经 Jackson 序列化变成 `custom` 字段 → 加 `@JsonProperty`。
  - ParentPecsView `v-model` 绑在 `computed` 上拖拽不持久化 → 改 `reactive` + `watch`。
  - ChildPecsView 句条 `@drop.prevent` 无 handler 拖拽无效 → 补 `draggedCard` ref + 真实 drop handler。
- multipart 上限 10MB > controller MAX_BYTES 5MB，让应用层兜底返回友好 400 而非 Spring 默认 403。

---

## 5. Story 看板 (Backlog Status)

「本周期」列为 4 周周期内的 MoSCoW 取舍（详见 PRD §9.1）；「Sprint」列为计划承接的 Sprint。

### Module A — Digital PECS Builder
| ID  | Story        | SP | 本周期        | Sprint   | 状态     |
| --- | ------------ | -- | ---------- | -------- | ------ |
| A-1 | 主界面大尺寸图卡分类   | 5  | Must       | Sprint 1 | 🟢 Done |
| A-2 | 拖拽图卡拼出完整句子   | 8  | Must       | Sprint 1 | 🟢 Done |
| A-4 | 家长模式增删/排序图卡  | 5  | Must       | Sprint 1 | 🟢 Done |
| A-5 | 上传照片作为自定义图卡  | 8  | **追加 Must** | Sprint 1 | 🟢 Done |

### Module B — Visual Schedule & Timeline
| ID  | Story        | SP | 本周期    | Sprint   | 状态     |
| --- | ------------ | -- | ------ | -------- | ------ |
| B-1 | 家长组合分步骤日程    | 8  | Must   | Sprint 1 | 🟢 Done |
| B-2 | 当前步骤高亮标识     | 5  | Must   | Sprint 1 | 🟢 Done |
| B-3 | 完成步骤点击大复选框打勾 | 3  | Should | Sprint 2 | 🟢 Done |
| B-4 | 保存日程模板       | 5  | Could  | Sprint 2 | 🟢 Done |

### Module C — Behavior Logger, Weekly Report & Child Diary
| ID  | Story            | SP | 本周期        | Sprint   | 状态     |
| --- | ---------------- | -- | ---------- | -------- | ------ |
| C-1 | 3 次点击内完成事件记录     | 8  | Must (追加)  | Sprint 2 | 🟢 Done |
| C-2 | 记录页与儿童模式视觉分离     | 3  | Must (追加)  | Sprint 2 | 🟢 Done |
| C-3 | 按周自动生成情绪趋势报告     | 13 | Must (追加)  | Sprint 2 | 🟢 Done |
| C-4 | 周报导出 PDF / 分享链接  | 5  | Must (追加)  | Sprint 2 | 🟢 Done |
| C-5 | 连续 3 天同一触发点提示家长  | 8  | Must (追加)  | Sprint 2 | 🟢 Done |
| C-6 | 儿童在私人日记空间记录情绪    | 5  | Must (追加)  | Sprint 2 | 🟢 Done |
| C-7 | 家长开启/关闭儿童私人日记    | 3  | Must (追加)  | Sprint 2 | 🟢 Done |

图例：⬜ Backlog · 🔵 Sprint Backlog · 🟡 In Progress · 🟣 Review · 🟢 Done

**本周期估点统计**
- **Sprint 1**：A-1+A-2+A-4+A-5(追加)+B-1+B-2 = **39 SP** ✅ 全部完成
- **Sprint 2**：B-3+B-4+C-1..C-7 = **52 SP** ✅ 全部完成（C-3/C-4/C-5 由 Won't 升 Must 同日完成）
- **Won't (本周期)**：— （原 C-3/C-4/C-5 已上线，本周期不再列 Won't）

> Sprint 2 原计划 12–15 SP；6/4 提前完成 Epic C 全部 7 个 Story (50 SP)，远超预算。剩余仅 B-3/B-4 与 B-1/B-2 收尾。

---

## 6. 速度与燃尽 (Velocity & Burndown)

| Sprint            | 时长   | 承诺 SP    | 完成 SP | 实际 Velocity |
| ----------------- | ---- | -------- | ----- | ----------- |
| Sprint 0          | 2 周  | —              | —     | — (筹备性)     |
| Sprint 1          | 2 周  | 39 (含追加 A-5) | 39    | 🟢 19.5 SP/wk |
| Sprint 2          | 6 天  | 52（含 C-3/4/5 追加）| 52  | 🟢 远超原 12–15 SP 预算 |

> Sprint 0 已签收。初始 Velocity 假设 25 SP / 2 周 Sprint。Sprint 1 + Sprint 2 累计 91 SP 在 ~9 天内交付，实际 Velocity ≈ 70+ SP/2-周，远高于初始假设——主要由于 AI 辅助编码 + 范围内全程端到端验证的工作流。Burndown 已实质归零。

---

## 7. 阻塞与风险跟踪 (Impediment & Risk Log)

### 当前阻塞 (Impediments)
| #   | 阻塞描述               | 负责人        | 目标解决日期     | 状态    |
| --- | ------------------ | ---------- | ---------- | ----- |
| —   | 暂无                 | —          | —          | —     |

### 活跃风险 (Top Risks，详见 PRD §10.8)
| #   | 风险                       | 概率 | 影响 | 当前应对                       |
| --- | ------------------------ | -- | -- | -------------------------- |
| R1  | 难以招募自闭症家庭做真实测试           | 高  | 高  | Sprint 0 内联系本地特教机构         |
| R3  | 4 人团队 Velocity 被高估       | 高  | 中  | Sprint 2 起以实际 Velocity 重排  |
| R4  | Supabase RLS 配置错误导致越权    | 低  | 极高 | 强制 RLS 单元测试 + 上线前安全自查      |

---

## 8. 团队待办提醒 (Action Items)

- [x] Sprint 0 签收 & 打 tag `vR0-sprint0` ✅
- [x] 前后端脚手架 + CI/CD 流水线 ✅
- [x] 登录流程端到端 (JWT + login form + route guard) ✅
- [x] Epic A 之 A-1/A-2/A-4/A-5 全部端到端跑通 ✅（A-5 改造为"上传即建卡"）
- [x] Epic C 之 C-1..C-7 全部端到端跑通 ✅（每条 AC 用 curl 走过）
- [x] Epic B 之 B-1/B-2/B-3/B-4 全部端到端跑通 ✅（max-10 / 高亮 / 庆祝 / 模板 CRUD）
- [ ] 为 Epic A 补 Vitest 单元测试 + Playwright E2E（DoD 要求）
- [ ] 在 Jira 中补全全部 13 条 Story (A/B/C 模块) 并挂到对应 Epic
- [ ] 把前端 / 后端技术栈 + Jira 看板链接发到 Teams 私有频道 (老师要求)
- [ ] **写 Vitest 单元测试**（DoD 卡点）：Pinia store 逻辑 + 关键组件渲染
- [ ] **写 Playwright E2E**（DoD 卡点）：A-2 拖拽拼句 / B-3 打勾庆祝 / C-1 行为记录 3 端到端
- [ ] **6/7 Deliverable 3 提交**：演示视频 + 截图 + 体验账号 — 剩余 **3 天**
- [ ] Supabase 生产库迁移（schema.sql 复用 + RLS Policy 重建）
- [ ] **6/15 项目展示**：演示稿 + 现场演示流程 — 剩余 **11 天**

---

## 9. 变更记录 (Change Log)

| 版本   | 日期         | 作者        | 变更                                          |
| ---- | ---------- | --------- | ------------------------------------------- |
| v1.0 | 2026-05-18 | Ke Hongyi | 进度文档初稿                                      |
| v1.1 | 2026-05-18 | Ke Hongyi | 配合 6/15 课程硬截止，压缩为 1 筹备 + 2 开发 Sprint；引入本周期 MoSCoW 取舍与 Won't 标注 |
| v1.2 | 2026-06-01 | Ke Hongyi | Sprint 0 正式签收；Sprint 1 进展更新（登录流程完成、前后端骨架就绪、A-1 进行中）；新增 H2 本地开发数据库说明 |
| v1.3 | 2026-06-01 | Ke Hongyi | H2 → PostgreSQL 17；注册功能完成；start.sh 一键启动脚本；更新 Sprint 1 任务状态 |
| v1.4 | 2026-06-03 | Ke Hongyi | Epic A 全部 4 个 Story (A-1/A-2/A-4 + 追加 A-5) 完成；新增 Epic A 完成增量、bug 修复清单、文件上传端点；Sprint 1 承诺 31→39 SP；A-5 从 Won't 升 Must |
| v1.4.1 | 2026-06-03 | Ke Hongyi | 复审 A-5：API 与静态服务端到端通过，但 UI 流程 "上传 → 填 label → Create" 不符 AC #2 "上传成功后图卡出现在对应分类中"，状态退回 🟡 部分完成 |
| v1.4.2 | 2026-06-03 | Ke Hongyi | A-5 改造为"上传即建卡"：选文件即调 POST /api/cards（label 默认文件名）；新增 PATCH /api/cards/{id} 与列表 inline 重命名。AC #2 满足，状态回到 🟢 完成 |
| v1.4.3 | 2026-06-04 | Ke Hongyi | 日期刷新；Deliverable 3 倒计时 4→3 天；Sprint 1 进度快照截止 6/4；下一焦点明确为 B-1/B-2 |
| v1.5 | 2026-06-04 | Ke Hongyi | **Epic C 全部 7 个 Story (C-1..C-7) 完成并端到端验证**：行为记录三步表单 + 暗色家长 UI；周报 ≥3 阈值与不足态；浏览器打印 PDF + 24h 失效分享链接（公开路由 + 410 过期）；连续 3 天同触发预警 + dismiss 抑制；儿童 5 情绪卡 + 涂鸦画布；家长 toggle 与 today-bool 隐私门面；7 项 RLS 跨角色拒访已实测 |
| v1.6 | 2026-06-04 | Ke Hongyi | **Epic B 全部 4 个 Story (B-1..B-4) 完成并端到端验证**：B-1 家长 schedule builder（≤10 steps 后端硬校验）；B-2 child 视图 current 强高亮 + next 半高亮；B-3 大复选框打勾 + ✓ pop 动画 + 全完成 🎉 庆祝 + Start Over；B-4 命名模板 list/edit/delete（删除级联清 instance）；ParentNav 加 Schedule tab，ChildPecsView 加 🗓️ 入口；至此 Epic A/B/C 全部完成 |
| v1.7 | 2026-06-04 | Ke Hongyi | 大刷新：里程碑 R1 🟢、R2 🟢（功能侧）；§4 Current Sprint 改为 Sprint 2 收尾视角；估点统计去除"待做"列；Velocity 表回填 Sprint 1=39 / Sprint 2=52；Action Items 切到测试 + Deliverable + 演示，移除已完成的功能开发条目 |
| v1.8 | 2026-06-04 | Ke Hongyi | **A-2 升级为双向 PECS 对话**：词卡库 16→53（新增 People/Action/Time 三分类）；sentence 表去 child_id 改 family_id + sender_role；新增 `/child/chat` + `/parent/chat` 共用 ChatComposer 组件 + MessageBubble；polling 3 s；child 端首页加 💬 入口，ParentNav 加 Chat tab。端到端实测：child→parent→child 来回各 200 + 增量 polling 正确 |
