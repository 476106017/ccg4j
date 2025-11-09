-- 为已有的 user_account 表添加新字段
-- 如果表不存在则跳过

-- 添加 arcane_dust 字段（奥术之尘）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'user_account' AND column_name = 'arcane_dust'
    ) THEN
        ALTER TABLE user_account ADD COLUMN arcane_dust integer NOT NULL DEFAULT 0;
    END IF;
END $$;

-- 添加 match_rating 字段（匹配分）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'user_account' AND column_name = 'match_rating'
    ) THEN
        ALTER TABLE user_account ADD COLUMN match_rating integer NOT NULL DEFAULT 1000;
    END IF;
END $$;

-- 验证字段已添加
SELECT column_name, data_type, column_default 
FROM information_schema.columns 
WHERE table_name = 'user_account' 
  AND column_name IN ('arcane_dust', 'match_rating')
ORDER BY column_name;
