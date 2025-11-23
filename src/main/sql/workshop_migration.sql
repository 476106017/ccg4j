-- 为 workshop_card 表添加新字段和唯一性约束
-- 请在数据库中手动执行此脚本

ALTER TABLE workshop_card
ADD COLUMN IF NOT EXISTS race text,
ADD COLUMN IF NOT EXISTS countdown int;

-- 添加卡牌名称唯一性约束
ALTER TABLE workshop_card
ADD CONSTRAINT workshop_card_name_unique UNIQUE (name);
