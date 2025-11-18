-- ==========================================
-- 数据库初始化脚本
-- CCG4J (Card Collection Game for Java)
-- ==========================================
-- 执行顺序：
-- 1. 用户账号相关表
-- 2. 卡牌和卡组相关表
-- 3. 弥留之国相关表
-- 4. 对战记录相关表
-- 5. 系统配置表
-- 6. Spring Session表
-- ==========================================

-- ==========================================
-- 1. 用户账号表
-- ==========================================

-- 用户账号，与 Supabase/PostgreSQL 兼容
CREATE TABLE IF NOT EXISTS user_account (
    id           BIGSERIAL PRIMARY KEY,
    username     TEXT        NOT NULL UNIQUE,
    password     TEXT        NOT NULL,
    tickets      INTEGER     NOT NULL DEFAULT 0,
    arcane_dust  INTEGER     NOT NULL DEFAULT 0,
    match_rating INTEGER     NOT NULL DEFAULT 1000,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_account_updated_at
    ON user_account (updated_at);

COMMENT ON TABLE user_account IS '用户账号表';
COMMENT ON COLUMN user_account.tickets IS '抽卡券数量';
COMMENT ON COLUMN user_account.arcane_dust IS '奥术之尘（分解卡牌获得）';
COMMENT ON COLUMN user_account.match_rating IS '天梯积分';


-- ==========================================
-- 2. 卡牌和卡组相关表
-- ==========================================

-- 卡牌收藏
CREATE TABLE IF NOT EXISTS user_card_collection (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES user_account(id)
                           ON DELETE CASCADE,
    card_code  TEXT        NOT NULL,
    quantity   INTEGER     NOT NULL DEFAULT 0,
    UNIQUE (user_id, card_code)
);

CREATE INDEX IF NOT EXISTS idx_user_card_collection_user
    ON user_card_collection (user_id);

COMMENT ON TABLE user_card_collection IS '用户卡牌收藏表';
COMMENT ON COLUMN user_card_collection.card_code IS '卡牌唯一代码';
COMMENT ON COLUMN user_card_collection.quantity IS '拥有数量';


-- 用户卡组表
CREATE TABLE IF NOT EXISTS user_deck (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT      NOT NULL REFERENCES user_account(id)
                             ON DELETE CASCADE,
    deck_name    TEXT        NOT NULL,
    deck_data    TEXT        NOT NULL,  -- 卡组数据，格式如 "卡牌code1,卡牌code2,..."
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_deck_user_id
    ON user_deck (user_id);

COMMENT ON TABLE user_deck IS '用户卡组表';
COMMENT ON COLUMN user_deck.deck_name IS '卡组名称';
COMMENT ON COLUMN user_deck.deck_data IS '卡组数据，逗号分隔的卡牌code列表';


-- ==========================================
-- 3. 弥留之国相关表
-- ==========================================

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

COMMENT ON TABLE borderland_visa IS '弥留之国签证表';
COMMENT ON COLUMN borderland_visa.status IS '签证状态';
COMMENT ON COLUMN borderland_visa.days_remaining IS '剩余天数';
COMMENT ON COLUMN borderland_visa.deck_data IS '卡组数据';
COMMENT ON COLUMN borderland_visa.punishment_end_time IS '惩罚结束时间';


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

COMMENT ON TABLE borderland_export_record IS '弥留之国带出卡牌记录表';
COMMENT ON COLUMN borderland_export_record.card_code IS '带出的卡牌代码';


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

CREATE INDEX IF NOT EXISTS idx_borderland_battle_log_timestamp ON borderland_battle_log(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_borderland_battle_log_event_type ON borderland_battle_log(event_type);

COMMENT ON TABLE borderland_battle_log IS '弥留之国战斗记录表';
COMMENT ON COLUMN borderland_battle_log.event_type IS '事件类型：match-匹配, victory-胜利, defeat-失败';
COMMENT ON COLUMN borderland_battle_log.player1_name IS '玩家1名称';
COMMENT ON COLUMN borderland_battle_log.player2_name IS '玩家2名称';
COMMENT ON COLUMN borderland_battle_log.winner_name IS '胜者名称（仅victory/defeat时有值）';
COMMENT ON COLUMN borderland_battle_log.timestamp IS '时间戳';
COMMENT ON COLUMN borderland_battle_log.punishment_seconds IS '惩罚时间(秒)';


-- ==========================================
-- 4. 对战记录表
-- ==========================================

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

CREATE INDEX idx_battle_winner ON battle_record(winner_id);
CREATE INDEX idx_battle_loser ON battle_record(loser_id);
CREATE INDEX idx_battle_mode ON battle_record(mode);
CREATE INDEX idx_battle_created ON battle_record(created_at DESC);

COMMENT ON TABLE battle_record IS '对战记录表';
COMMENT ON COLUMN battle_record.winner_id IS '获胜者用户ID，AI对战时为NULL';
COMMENT ON COLUMN battle_record.loser_id IS '失败者用户ID，AI对战时为NULL';
COMMENT ON COLUMN battle_record.mode IS '对战模式：normal-常规匹配, borderland-弥留之国';
COMMENT ON COLUMN battle_record.end_reason IS '结束原因：hp_zero-生命归零, concede-投降, timeout-超时, special-特殊胜利';


-- ==========================================
-- 5. 系统配置表
-- ==========================================

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

CREATE INDEX IF NOT EXISTS idx_config_dict_group ON config_dict(config_group);
CREATE INDEX IF NOT EXISTS idx_config_dict_key ON config_dict(config_key);


-- ==========================================
-- 6. Spring Session 表
-- ==========================================

-- Spring Session JDBC 表结构 (PostgreSQL)
-- 用于存储HTTP Session到数据库

-- Session主表
CREATE TABLE IF NOT EXISTS SPRING_SESSION (
    PRIMARY_ID CHAR(36) NOT NULL,
    SESSION_ID CHAR(36) NOT NULL,
    CREATION_TIME BIGINT NOT NULL,
    LAST_ACCESS_TIME BIGINT NOT NULL,
    MAX_INACTIVE_INTERVAL INT NOT NULL,
    EXPIRY_TIME BIGINT NOT NULL,
    PRINCIPAL_NAME VARCHAR(100),
    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);

CREATE UNIQUE INDEX IF NOT EXISTS SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

COMMENT ON TABLE SPRING_SESSION IS 'Spring Session主表，存储Session元数据';
COMMENT ON COLUMN SPRING_SESSION.SESSION_ID IS 'Session唯一标识';
COMMENT ON COLUMN SPRING_SESSION.CREATION_TIME IS '创建时间（时间戳）';
COMMENT ON COLUMN SPRING_SESSION.LAST_ACCESS_TIME IS '最后访问时间（时间戳）';
COMMENT ON COLUMN SPRING_SESSION.MAX_INACTIVE_INTERVAL IS '最大非活动时间（秒）';
COMMENT ON COLUMN SPRING_SESSION.EXPIRY_TIME IS '过期时间（时间戳）';
COMMENT ON COLUMN SPRING_SESSION.PRINCIPAL_NAME IS '用户名';


-- Session属性表
CREATE TABLE IF NOT EXISTS SPRING_SESSION_ATTRIBUTES (
    SESSION_PRIMARY_ID CHAR(36) NOT NULL,
    ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES BYTEA NOT NULL,
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) 
        REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
);

COMMENT ON TABLE SPRING_SESSION_ATTRIBUTES IS 'Spring Session属性表，存储Session中的属性';
COMMENT ON COLUMN SPRING_SESSION_ATTRIBUTES.ATTRIBUTE_NAME IS '属性名称';
COMMENT ON COLUMN SPRING_SESSION_ATTRIBUTES.ATTRIBUTE_BYTES IS '属性值（二进制格式）';


-- ==========================================
-- 初始化完成
-- ==========================================

-- 说明：
-- 1. 此脚本包含了所有必要的数据库表和索引
-- 2. 使用 IF NOT EXISTS 确保可以重复执行而不出错
-- 3. 外键关系已正确设置，确保数据一致性
-- 4. 索引已优化，提升查询性能
-- 5. 添加了详细的注释说明
-- 6. 已包含初始配置数据
