-- 好友关系表
create table if not exists friend_relationship (
    id           bigserial primary key,
    user_id      bigint      not null references user_account(id) on delete cascade,
    friend_id    bigint      not null references user_account(id) on delete cascade,
    status       text        not null check (status in ('PENDING', 'ACCEPTED', 'BLOCKED')),
    created_at   timestamptz not null default now(),
    updated_at   timestamptz not null default now(),
    unique (user_id, friend_id)
);

create index if not exists idx_friend_relationship_user on friend_relationship(user_id);
create index if not exists idx_friend_relationship_friend on friend_relationship(friend_id);

-- 聊天记录表
create table if not exists chat_message (
    id           bigserial primary key,
    sender_id    bigint      not null references user_account(id) on delete cascade,
    receiver_id  bigint      references user_account(id) on delete cascade, -- null for global chat
    content      text        not null,
    type         text        not null check (type in ('GLOBAL', 'PRIVATE', 'SYSTEM')),
    created_at   timestamptz not null default now()
);

create index if not exists idx_chat_message_sender on chat_message(sender_id);
create index if not exists idx_chat_message_receiver on chat_message(receiver_id);
create index if not exists idx_chat_message_created_at on chat_message(created_at);
