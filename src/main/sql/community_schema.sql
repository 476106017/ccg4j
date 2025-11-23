-- Community Feature Schema

-- 1. Channels
create table if not exists community_channel (
    id               bigserial primary key,
    name             text        not null,
    description      text,
    type             text        not null check (type in ('PUBLIC', 'PRIVATE')),
    owner_id         bigint      not null references user_account(id) on delete cascade,
    is_pinned        boolean     not null default false,
    is_restricted    boolean     not null default false,
    heat             int         not null default 0,
    level            int         not null default 1,
    created_at       timestamptz not null default now(),
    updated_at       timestamptz not null default now(),
    last_activity_at timestamptz not null default now()
);

create index if not exists idx_community_channel_owner on community_channel(owner_id);
create index if not exists idx_community_channel_last_activity on community_channel(last_activity_at desc);
create index if not exists idx_community_channel_heat on community_channel(heat desc);

-- 2. Posts
create table if not exists community_post (
    id            bigserial primary key,
    channel_id    bigint      not null references community_channel(id) on delete cascade,
    author_id     bigint      not null references user_account(id) on delete cascade,
    title         text        not null,
    content       text        not null,
    upvotes       int         not null default 0,
    downvotes     int         not null default 0,
    created_at    timestamptz not null default now(),
    updated_at    timestamptz not null default now(),
    last_reply_at timestamptz not null default now()
);

create index if not exists idx_community_post_channel on community_post(channel_id);
create index if not exists idx_community_post_author on community_post(author_id);
create index if not exists idx_community_post_last_reply on community_post(last_reply_at desc);

-- 3. Replies
create table if not exists community_reply (
    id         bigserial primary key,
    post_id    bigint      not null references community_post(id) on delete cascade,
    parent_id  bigint      references community_reply(id) on delete cascade,
    author_id  bigint      not null references user_account(id) on delete cascade,
    content    text        not null,
    upvotes    int         not null default 0,
    downvotes  int         not null default 0,
    created_at timestamptz not null default now()
);

create index if not exists idx_community_reply_post on community_reply(post_id);
create index if not exists idx_community_reply_author on community_reply(author_id);
create index if not exists idx_community_reply_parent on community_reply(parent_id);

-- 4. Votes
create table if not exists community_vote (
    id          bigserial primary key,
    user_id     bigint      not null references user_account(id) on delete cascade,
    target_type text        not null check (target_type in ('POST', 'REPLY')),
    target_id   bigint      not null,
    vote_type   int         not null check (vote_type in (1, -1)),
    created_at  timestamptz not null default now(),
    unique (user_id, target_type, target_id)
);

create index if not exists idx_community_vote_target on community_vote(target_type, target_id);

-- 5. Bans
create table if not exists community_ban (
    id          bigserial primary key,
    channel_id  bigint      not null references community_channel(id) on delete cascade,
    user_id     bigint      not null references user_account(id) on delete cascade,
    operator_id bigint      not null references user_account(id) on delete cascade,
    end_time    timestamptz not null,
    created_at  timestamptz not null default now()
);

create index if not exists idx_community_ban_channel_user on community_ban(channel_id, user_id);

-- 6. Whitelist
create table if not exists community_channel_whitelist (
    id         bigserial primary key,
    channel_id bigint      not null references community_channel(id) on delete cascade,
    user_id    bigint      not null references user_account(id) on delete cascade,
    created_at timestamptz not null default now(),
    unique (channel_id, user_id)
);
