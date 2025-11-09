-- 弥留之国战斗记录表
CREATE TABLE IF NOT EXISTS borderland_battle_log (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(20) NOT NULL,
    player1_name VARCHAR(100) NOT NULL,
    player2_name VARCHAR(100) NOT NULL,
    winner_name VARCHAR(100),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    punishment_seconds INTEGER,
    CONSTRAINT chk_event_type CHECK (event_type IN ('match', 'victory', 'defeat'))
);

-- 创建索引加速查询
CREATE INDEX IF NOT EXISTS idx_borderland_battle_log_timestamp ON borderland_battle_log(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_borderland_battle_log_event_type ON borderland_battle_log(event_type);

-- 添加注释
COMMENT ON TABLE borderland_battle_log IS '弥留之国战斗记录表';
COMMENT ON COLUMN borderland_battle_log.event_type IS '事件类型：match-匹配, victory-胜利, defeat-失败';
COMMENT ON COLUMN borderland_battle_log.player1_name IS '玩家1名称';
COMMENT ON COLUMN borderland_battle_log.player2_name IS '玩家2名称';
COMMENT ON COLUMN borderland_battle_log.winner_name IS '胜者名称（仅victory/defeat时有值）';
COMMENT ON COLUMN borderland_battle_log.timestamp IS '时间戳';
COMMENT ON COLUMN borderland_battle_log.punishment_seconds IS '惩罚时间(秒)';
