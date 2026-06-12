# 🗄️ VisiTalk — Database ER Diagram

PostgreSQL 17 (Neon) · 11 tables · source of truth: `backend/src/main/resources/schema.sql`

> **For your slide:** use [er-diagram.svg](er-diagram.svg) — it's **vector**, so it
> stays crisp at any zoom (no blurry text). A hi-res [er-diagram.png](er-diagram.png)
> (3136×1108) is also included. The ```mermaid``` block below is the editable source.

![VisiTalk ER diagram](er-diagram.svg)

```mermaid
erDiagram
    users ||--o{ behavior_event : "parent_id"
    users ||--o{ behavior_event : "child_id"
    users ||--o{ weekly_report : "child_id"
    users ||--o{ diary_entry : "child_id"
    schedule_template ||--o{ schedule_instance : "template_id"
    pictogram_card }o--o{ sentence : "card_ids[]"
    pictogram_card }o--o{ schedule_template : "steps[]"

    users {
        bigint id PK
        varchar email UK
        varchar password_hash
        varchar role "child | parent"
        varchar family_id "tenant key"
        varchar pin_hash
        timestamp created_at
    }
    pictogram_card {
        bigint id PK
        varchar family_id
        varchar category
        varchar image_url "emoji:.. or /uploads/.."
        varchar label_i18n
        bool is_custom
        int sort_order
    }
    sentence {
        bigint id PK
        varchar family_id
        varchar sender_role
        bigint_array card_ids
        timestamp created_at
    }
    schedule_template {
        bigint id PK
        varchar family_id
        varchar name
        bigint_array steps
        timestamp created_at
    }
    schedule_instance {
        bigint id PK
        bigint template_id FK
        date date
        bigint_array completed_step_ids
    }
    behavior_event {
        bigint id PK
        bigint parent_id FK
        bigint child_id FK
        int intensity "1..5"
        text_array trigger_tags
        text note_encrypted
        timestamp occurred_at
    }
    weekly_report {
        bigint id PK
        bigint child_id FK
        date week_start
        jsonb metrics_json
        varchar pdf_url
    }
    diary_entry {
        bigint id PK
        bigint child_id FK
        bigint emotion_card_id
        text doodle_url
        timestamp created_at
    }
    system_configs {
        varchar family_id PK
        bool diary_feature_enabled
    }
    report_share {
        varchar token PK
        varchar family_id
        date week_start
        text payload_json
        timestamp expires_at
    }
    alert_dismissal {
        bigint id PK
        varchar family_id
        text trigger_tag
        timestamp dismissed_at
    }
```

## Reading the diagram

- **Solid foreign keys** (`REFERENCES`): `behavior_event.parent_id / child_id`,
  `weekly_report.child_id`, `diary_entry.child_id` → `users.id`;
  `schedule_instance.template_id` → `schedule_template.id`.
- **Array references** (Postgres `bigint[]`, dashed many-to-many):
  `sentence.card_ids[]` and `schedule_template.steps[]` point at `pictogram_card.id`.
- **`family_id` is the multi-tenant partition key.** Every family-scoped table
  (`users`, `pictogram_card`, `sentence`, `schedule_template`, `system_configs`,
  `report_share`, `alert_dismissal`) carries it, so all queries filter by family —
  one database, isolated per family.

## The three feature modules map onto the tables

| Module | Tables |
|---|---|
| **A — PECS / communication** | `pictogram_card`, `sentence` |
| **B — Visual schedule** | `schedule_template`, `schedule_instance` |
| **C — Behavior / report / diary** | `behavior_event`, `weekly_report`, `diary_entry`, `alert_dismissal`, `report_share` |
| Cross-cutting | `users` (auth + family), `system_configs` (per-family toggles) |
