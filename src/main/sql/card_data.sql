CREATE TABLE IF NOT EXISTS card_data (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    data_json TEXT NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE card_data IS '数据驱动卡牌存储表';
COMMENT ON COLUMN card_data.id IS '卡牌ID';
COMMENT ON COLUMN card_data.name IS '卡牌名称';
COMMENT ON COLUMN card_data.data_json IS '卡牌JSON数据';
COMMENT ON COLUMN card_data.enabled IS '是否启用';
