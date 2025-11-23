-- Activity System
alter table user_account add column if not exists activity_score int default 0;
alter table user_account add column if not exists last_login_date date;
alter table user_account add column if not exists last_daily_win_date date;

-- Block System
create table if not exists user_block (
    id              bigserial primary key,
    user_id         bigint      not null references user_account(id) on delete cascade,
    blocked_user_id bigint      not null references user_account(id) on delete cascade,
    created_at      timestamptz not null default now(),
    unique (user_id, blocked_user_id)
);

create index if not exists idx_user_block_user on user_block(user_id);
