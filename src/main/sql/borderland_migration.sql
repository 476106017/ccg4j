-- 弥留之国签证表
CREATE TABLE IF NOT EXISTS borderland_visa (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    days_remaining INT NOT NULL DEFAULT 10,
    deck_data TEXT,
    punishment_end_time TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_borderland_visa_user_id ON borderland_visa(user_id);
CREATE INDEX IF NOT EXISTS idx_borderland_visa_status ON borderland_visa(status);
CREATE INDEX IF NOT EXISTS idx_borderland_visa_created_at ON borderland_visa(created_at DESC);

-- 弥留之国带出卡牌记录表
CREATE TABLE IF NOT EXISTS borderland_export_record (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    card_code VARCHAR(100) NOT NULL,
    visa_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_borderland_export_user_id ON borderland_export_record(user_id);
CREATE INDEX IF NOT EXISTS idx_borderland_export_visa_id ON borderland_export_record(visa_id);
CREATE INDEX IF NOT EXISTS idx_borderland_export_created_at ON borderland_export_record(created_at DESC);

-- 为user_account表添加match_rating字段（如果不存在）
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name='user_account' AND column_name='match_rating'
    ) THEN
        ALTER TABLE user_account ADD COLUMN match_rating INT DEFAULT 1000;
        COMMENT ON COLUMN user_account.match_rating IS '天梯积分';
    END IF;
END $$;
