-- 1. 家长行为记录表 (C-1)
CREATE TABLE IF NOT EXISTS behavior_logs (
    id SERIAL PRIMARY KEY,
    family_id VARCHAR(50) NOT NULL,
    emotion_level INT NOT NULL CHECK (emotion_level BETWEEN 1 AND 5),
    trigger_tag VARCHAR(100) NOT NULL,
    recorded_at TIMESTAMP NOT NULL,
    elapsed_seconds INT NOT NULL -- 快速记录操作耗时审计 (AC: 中位数<=15秒)
);

-- 2. 儿童私密日记表 (C-6)
CREATE TABLE IF NOT EXISTS child_diaries (
    id SERIAL PRIMARY KEY,
    family_id VARCHAR(50) NOT NULL,
    emotion_card VARCHAR(50) NOT NULL,    -- 核心隐私：儿童选择的情绪卡片
    color_doodle TEXT NOT NULL,           -- 核心隐私：彩色涂鸦敏感详情 (Base64/Blob)
    created_date DATE NOT NULL UNIQUE     -- 每天仅限一条
);

-- 3. 系统全局配置开关表 (C-7)
CREATE TABLE IF NOT EXISTS system_configs (
    family_id VARCHAR(50) PRIMARY KEY,
    diary_feature_enabled BOOLEAN DEFAULT FALSE -- 儿童空间主开关，默认必须为 off
);

-- ========================================================
-- Row Level Security (RLS) 核心隐私阻断策略 (ADR-2 / WP-3 强需求)
-- ========================================================
ALTER TABLE behavior_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE child_diaries ENABLE ROW LEVEL SECURITY;

-- 阻断规则 A: 任何角色绝不允许跨家庭(family_id)访问数据
CREATE POLICY wp3_family_isolation_policy ON behavior_logs 
    USING (family_id = current_setting('request.jwt.claim.family_id', true));

-- 阻断规则 B: 强制隔离儿童日记明细。如果 JWT 中的 role 是 'PARENT'，直接拒绝读取明细行 (C-6/C-7)
CREATE POLICY wp3_child_diary_privacy_policy ON child_diaries
    FOR SELECT
    USING (
        family_id = current_setting('request.jwt.claim.family_id', true) 
        AND current_setting('request.jwt.claim.role', true) = 'CHILD'
    );