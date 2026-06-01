# 📊 VisiTalk — 项目进度文档 (Project Progress)

| 项目        | VisiTalk — 自闭症儿童可视化沟通与情绪追踪平台 |
| --------- | ----------------------------- |
| 文档版本      | v1.3                          |
| 文档负责人     | Ke Hongyi (Scrum Master)      |
| 最后更新      | 2026-06-01                    |
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
| R1 — MVP 完成          | 2026-06-09     | 🟡 进行中 |
| **Deliverable 3 提交** | **2026-06-07** | ⬜ 未开始 |
| R2 — 增强 + 收尾 完成      | 2026-06-15     | ⬜ 未开始 |
| **项目展示 (Presentation)** | **2026-06-15** | ⬜ 未开始 |

---

## 3. Release 路线进度 (Release Roadmap)

| Release        | 时间窗口             | Sprint            | 进度     | 主要内容                     |
| -------------- | ---------------- | ----------------- | ------ | ------------------------ |
| R0 — 筹备        | 2026-05-13~05-26 | Sprint 0          | 🟢 完成 | 团队组建、PRD 评审、技术骨架、登录流程    |
| R1 — MVP       | 2026-05-27~06-09 | Sprint 1 (2 周)    | 🟡 进行中 | A-1, A-2, A-4, B-1, B-2  |
| R2 — 增强 + 收尾   | 2026-06-10~06-15 | Sprint 2 (缩短 6 天) | ⬜ 未开始 | C-1, C-2, B-3（视容量）+ 收尾演示 |

图例：🟢 完成 · 🟡 进行中 · ⬜ 未开始

> Sprint 0 已结束（5/26）。Sprint 1 自 5/27 启动，截至 6/1 已完成登录流程与前后端骨架联调，下一步投入 A-1/A-2 图卡功能开发。Sprint 2 因 6/15 硬截止缩短为 6 天。R3（C-3/C-4/C-5）与 A-5 列为本周期 **Won't**。

---

## 4. 当前 Sprint 状态 (Current Sprint)

| 项目          | 内容                                          |
| ----------- | ------------------------------------------- |
| Sprint      | Sprint 1 (R1 MVP)                           |
| 周期          | 2026-05-27 ~ 2026-06-09                     |
| Sprint Goal | 交付 MVP：儿童可看图卡分类、拖拽拼句；家长可管理图卡；可跑可视化日程    |
| 承诺 SP       | 31 SP (A-1/A-2/A-4/B-1/B-2)                |

### Sprint 1 当前进度 (截至 6/1)
| 任务                              | 负责人          | 状态      |
| ------------------------------- | ------------ | ------- |
| Sprint 0 签收 & tag vR0-sprint0   | Ke Hongyi    | 🟢 完成   |
| 前端脚手架 (Vue 3 + Vite + Pinia + Tailwind) | Yuen KinNing | 🟢 完成 |
| 后端脚手架 (Spring Boot 3 + JWT)   | Xu Zihe      | 🟢 完成 |
| CI/CD 流水线 (GitHub Actions)     | Xu Ziyang    | 🟢 完成 |
| 登录流程端到端 (JWT + 登录页 + 路由守卫)  | Xu Ziyang    | 🟢 完成 |
| PostgreSQL 17 本地数据库               | Xu Ziyang    | 🟢 完成 |
| 注册功能端到端 (register API + UI)      | Xu Ziyang    | 🟢 完成 |
| start.sh 一键启动脚本                   | Xu Ziyang    | 🟢 完成 |
| A-1: 儿童端图卡分类主界面               | Yuen KinNing | 🔵 进行中 |
| A-2: 拖拽图卡拼句                    | Yuen KinNing | ⬜ 待开始 |
| A-4: 家长增删/排序图卡                 | Xu Ziyang    | ⬜ 待开始 |
| B-1: 家长组合分步骤日程                | Yuen KinNing | ⬜ 待开始 |
| B-2: 儿童端步骤高亮标识                | Yuen KinNing | ⬜ 待开始 |
| Supabase 项目与基础表结构              | Xu Zihe      | ⬜ 待开始 |
| Jira Story 录入 Backlog          | Ke Hongyi    | 🟡 进行中 |
| Teams 频道发送技术栈 + Jira 链接       | Xu Ziyang    | 🟡 待确认 |

> Sprint 0 全部任务已于 5/26 前完成。Sprint 1 自 5/27 启动，当前焦点为 A 模块图卡功能。登录流程已于 6/1 端到端跑通（前端 login form → 后端 JWT → 路由守卫）。H2 内存数据库替 Supabase 做本地开发，Supabase 生产库在 D3 前完成即可。

---

## 5. Story 看板 (Backlog Status)

「本周期」列为 4 周周期内的 MoSCoW 取舍（详见 PRD §9.1）；「Sprint」列为计划承接的 Sprint。

### Module A — Digital PECS Builder
| ID  | Story        | SP | 本周期        | Sprint   | 状态     |
| --- | ------------ | -- | ---------- | -------- | ------ |
| A-1 | 主界面大尺寸图卡分类   | 5  | Must       | Sprint 1 | ⬜ Backlog |
| A-2 | 拖拽图卡拼出完整句子   | 8  | Must       | Sprint 1 | ⬜ Backlog |
| A-4 | 家长模式增删/排序图卡  | 5  | Must       | Sprint 1 | ⬜ Backlog |
| A-5 | 上传照片作为自定义图卡  | 8  | Won't (本周期) | —        | ⬜ Backlog |

### Module B — Visual Schedule & Timeline
| ID  | Story        | SP | 本周期    | Sprint   | 状态     |
| --- | ------------ | -- | ------ | -------- | ------ |
| B-1 | 家长组合分步骤日程    | 8  | Must   | Sprint 1 | ⬜ Backlog |
| B-2 | 当前步骤高亮标识     | 5  | Must   | Sprint 1 | ⬜ Backlog |
| B-3 | 完成步骤点击大复选框打勾 | 3  | Should | Sprint 2 | ⬜ Backlog |
| B-4 | 保存日程模板       | 5  | Could  | Sprint 2 | ⬜ Backlog |

### Module C — Behavior Logger, Weekly Report & Child Diary
| ID  | Story            | SP | 本周期        | Sprint   | 状态     |
| --- | ---------------- | -- | ---------- | -------- | ------ |
| C-1 | 3 次点击内完成事件记录     | 8  | Should     | Sprint 2 | ⬜ Backlog |
| C-2 | 记录页与儿童模式视觉分离     | 3  | Should     | Sprint 2 | ⬜ Backlog |
| C-3 | 按周自动生成情绪趋势报告     | 13 | Won't (本周期) | —        | ⬜ Backlog |
| C-4 | 周报导出 PDF / 分享链接  | 5  | Won't (本周期) | —        | ⬜ Backlog |
| C-5 | 连续 3 天同一触发点提示家长  | 8  | Won't (本周期) | —        | ⬜ Backlog |
| C-6 | 儿童在私人日记空间记录情绪    | 5  | Could      | Sprint 2 | ⬜ Backlog |
| C-7 | 家长开启/关闭儿童私人日记    | 3  | Could      | Sprint 2 | ⬜ Backlog |

图例：⬜ Backlog · 🔵 Sprint Backlog · 🟡 In Progress · 🟣 Review · 🟢 Done

**本周期估点统计**
- **Must (Sprint 1)**：A-1+A-2+A-4+B-1+B-2 = **31 SP**
- **Should (Sprint 2 优先)**：C-1+C-2+B-3 = **14 SP**
- **Could (Sprint 2 拉伸)**：C-6+C-7+B-4 = **13 SP**
- **Won't (本周期)**：A-5+C-3+C-4+C-5 = 34 SP（不在 6/15 前实现）

> Sprint 2 仅 6 天、容量约 12–15 SP，将优先消化 Should 项；Could 项视 Sprint 1 实际 Velocity 在 Sprint 2 Planning 时决定。

---

## 6. 速度与燃尽 (Velocity & Burndown)

| Sprint            | 时长   | 承诺 SP    | 完成 SP | 实际 Velocity |
| ----------------- | ---- | -------- | ----- | ----------- |
| Sprint 0          | 2 周  | —        | —     | — (筹备性)     |
| Sprint 1          | 2 周  | 31 (MVP) | —     | —           |
| Sprint 2          | 6 天  | 12–15    | —     | —           |

> Sprint 0 已签收。初始 Velocity 假设 25 SP / 标准 2 周 Sprint。Sprint 1 承诺 31 SP 略高于假设，属偏激进，中段（6/2）燃尽检查时若偏差 > 20% 优先把 A-4 或 B-2 退回 Backlog。Burndown Chart 由 SM 在 Jira 中每日更新。

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
- [ ] 在 Jira 中补全全部 13 条 Story (A/B/C 模块) 并挂到对应 Epic
- [ ] 把前端 / 后端技术栈 + Jira 看板链接发到 Teams 私有频道 (老师要求)
- [ ] **6/7 Deliverable 3 提交**：需保证 MVP 核心 (A-2 拖拽拼句 + B-1/B-2 日程) 可演示 — 剩余 **6 天**
- [ ] Sprint 2 (6/10-6/15)：增强项 (C-1/C-2/B-3) + 缺陷收敛 + 演示文档定稿

---

## 9. 变更记录 (Change Log)

| 版本   | 日期         | 作者        | 变更                                          |
| ---- | ---------- | --------- | ------------------------------------------- |
| v1.0 | 2026-05-18 | Ke Hongyi | 进度文档初稿                                      |
| v1.1 | 2026-05-18 | Ke Hongyi | 配合 6/15 课程硬截止，压缩为 1 筹备 + 2 开发 Sprint；引入本周期 MoSCoW 取舍与 Won't 标注 |
| v1.2 | 2026-06-01 | Ke Hongyi | Sprint 0 正式签收；Sprint 1 进展更新（登录流程完成、前后端骨架就绪、A-1 进行中）；新增 H2 本地开发数据库说明 |
| v1.3 | 2026-06-01 | Ke Hongyi | H2 → PostgreSQL 17；注册功能完成；start.sh 一键启动脚本；更新 Sprint 1 任务状态 |
