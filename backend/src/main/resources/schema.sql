CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(10) NOT NULL CHECK (role IN ('child', 'parent')),
    family_id VARCHAR(50) NOT NULL,
    pin_hash VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pictogram_card (
    id BIGSERIAL PRIMARY KEY,
    family_id VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    image_url VARCHAR(500),
    label_i18n VARCHAR(255),
    is_custom BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sentence (
    id BIGSERIAL PRIMARY KEY,
    family_id VARCHAR(50) NOT NULL,
    sender_role VARCHAR(10) NOT NULL CHECK (sender_role IN ('child', 'parent')),
    card_ids BIGINT[] NOT NULL DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_sentence_family_time ON sentence(family_id, created_at);

CREATE TABLE IF NOT EXISTS schedule_template (
    id BIGSERIAL PRIMARY KEY,
    family_id VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    steps BIGINT[] NOT NULL DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS schedule_instance (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES schedule_template(id),
    date DATE NOT NULL,
    completed_step_ids BIGINT[] NOT NULL DEFAULT '{}'
);

CREATE TABLE IF NOT EXISTS behavior_event (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT NOT NULL REFERENCES users(id),
    child_id BIGINT NOT NULL REFERENCES users(id),
    intensity INTEGER NOT NULL CHECK (intensity BETWEEN 1 AND 5),
    trigger_tags TEXT[] NOT NULL DEFAULT '{}',
    note_encrypted TEXT,
    occurred_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS weekly_report (
    id BIGSERIAL PRIMARY KEY,
    child_id BIGINT NOT NULL REFERENCES users(id),
    week_start DATE NOT NULL,
    metrics_json JSONB,
    pdf_url VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS diary_entry (
    id BIGSERIAL PRIMARY KEY,
    child_id BIGINT NOT NULL REFERENCES users(id),
    emotion_card_id BIGINT,
    doodle_url TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS system_configs (
    family_id VARCHAR(50) PRIMARY KEY,
    diary_feature_enabled BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS report_share (
    token VARCHAR(64) PRIMARY KEY,
    family_id VARCHAR(50) NOT NULL,
    week_start DATE NOT NULL,
    payload_json TEXT NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS alert_dismissal (
    id BIGSERIAL PRIMARY KEY,
    family_id VARCHAR(50) NOT NULL,
    trigger_tag TEXT NOT NULL,
    dismissed_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_alert_dismissal_family_tag
    ON alert_dismissal(family_id, trigger_tag);
