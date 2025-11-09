-- 系统配置字典表
CREATE TABLE IF NOT EXISTS config_dict (
    id SERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value VARCHAR(500) NOT NULL,
    config_type VARCHAR(50) NOT NULL DEFAULT 'string',
    config_group VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 添加注释
COMMENT ON TABLE config_dict IS '系统配置字典表';
COMMENT ON COLUMN config_dict.config_key IS '配置键，唯一标识';
COMMENT ON COLUMN config_dict.config_value IS '配置值';
COMMENT ON COLUMN config_dict.config_type IS '配置类型：string, int, boolean, json';
COMMENT ON COLUMN config_dict.config_group IS '配置分组';
COMMENT ON COLUMN config_dict.description IS '配置说明';

-- 插入游戏配置
INSERT INTO config_dict (config_key, config_value, config_type, config_group, description) VALUES
    ('game.ai_match_wait_seconds', '10', 'int', 'game', 'AI搜寻等待时间（秒），玩家在此期间可被其他玩家猎杀'),
    ('game.turn_timeout_seconds', '300', 'int', 'game', '正常回合超时时间（秒），玩家需在此时间内完成回合'),
    ('game.short_rope_seconds', '30', 'int', 'game', '短引线超时时间（秒），特定情况下的快速回合时间')
ON CONFLICT (config_key) DO NOTHING;

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_config_dict_group ON config_dict(config_group);
CREATE INDEX IF NOT EXISTS idx_config_dict_key ON config_dict(config_key);
