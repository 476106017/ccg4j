-- 用户账号，与 Supabase/PostgreSQL 兼容
create table if not exists user_account (
    id           bigserial primary key,
    username     text        not null unique,
    password     text        not null,
    tickets      integer     not null default 0,
    arcane_dust  integer     not null default 0,
    match_rating integer     not null default 1000,
    created_at   timestamptz not null default now(),
    updated_at   timestamptz not null default now()
);

create index if not exists idx_user_account_updated_at
    on user_account (updated_at);

-- 卡牌收藏
create table if not exists user_card_collection (
    id         bigserial primary key,
    user_id    bigint      not null references user_account(id)
                           on delete cascade,
    card_code  text        not null,
    quantity   integer     not null default 0,
    unique (user_id, card_code)
);

create index if not exists idx_user_card_collection_user
    on user_card_collection (user_id);