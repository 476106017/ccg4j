-- 用户卡组表
create table if not exists user_deck (
    id           bigserial primary key,
    user_id      bigint      not null references user_account(id)
                             on delete cascade,
    deck_name    text        not null,
    deck_data    text        not null,  -- 卡组数据，格式如 "卡牌code1,卡牌code2,..."
    created_at   timestamptz not null default now(),
    updated_at   timestamptz not null default now()
);

create index if not exists idx_user_deck_user_id
    on user_deck (user_id);

-- 添加注释
comment on table user_deck is '用户卡组表';
comment on column user_deck.deck_name is '卡组名称';
comment on column user_deck.deck_data is '卡组数据，逗号分隔的卡牌code列表';
