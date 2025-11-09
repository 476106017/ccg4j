-- 对战记录表
CREATE TABLE IF NOT EXISTS battle_record (
    id BIGSERIAL PRIMARY KEY,
    winner_id BIGINT NULL,                    -- 获胜者ID（AI为NULL）
    loser_id BIGINT NULL,                     -- 失败者ID（AI为NULL）
    mode VARCHAR(50) NOT NULL,                -- 对战模式：normal, borderland
    winner_deck TEXT,                          -- 获胜者卡组（逗号分隔的卡牌代码）
    loser_deck TEXT,                           -- 失败者卡组（逗号分隔的卡牌代码）
    winner_leader VARCHAR(100),                -- 获胜者职业
    loser_leader VARCHAR(100),                 -- 失败者职业
    duration INTEGER,                          -- 对战持续时间（秒）
    total_turns INTEGER,                       -- 总回合数
    end_reason VARCHAR(50),                    -- 结束原因：hp_zero, concede, timeout, special
    battle_details TEXT,                       -- 对战详情（记录关键操作）
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- 外键约束
    CONSTRAINT fk_winner FOREIGN KEY (winner_id) REFERENCES user_account(id) ON DELETE SET NULL,
    CONSTRAINT fk_loser FOREIGN KEY (loser_id) REFERENCES user_account(id) ON DELETE SET NULL
);

-- 创建索引以提升查询性能
CREATE INDEX idx_battle_winner ON battle_record(winner_id);
CREATE INDEX idx_battle_loser ON battle_record(loser_id);
CREATE INDEX idx_battle_mode ON battle_record(mode);
CREATE INDEX idx_battle_created ON battle_record(created_at DESC);

-- 添加注释
COMMENT ON TABLE battle_record IS '对战记录表';
COMMENT ON COLUMN battle_record.winner_id IS '获胜者用户ID，AI对战时为NULL';
COMMENT ON COLUMN battle_record.loser_id IS '失败者用户ID，AI对战时为NULL';
COMMENT ON COLUMN battle_record.mode IS '对战模式：normal-常规匹配, borderland-弥留之国';
COMMENT ON COLUMN battle_record.end_reason IS '结束原因：hp_zero-生命归零, concede-投降, timeout-超时, special-特殊胜利';
