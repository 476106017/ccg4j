-- Workshop Feature Schema

-- 1. Workshop Cards
create table if not exists workshop_card (
    id               bigserial primary key,
    author_id        bigint      not null references user_account(id) on delete cascade,
    name             text        not null,
    description      text,
    cost             int         not null default 0,
    attack           int         not null default 0,
    health           int         not null default 0,
    card_type        text        not null, -- 'FOLLOWER', 'SPELL', 'AMULET', 'EQUIPMENT'
    job              text        not null, -- 'NEUTRAL', 'ELF', 'ROYAL', 'WITCH', 'DRAGON', 'NECRO', 'VAMPIRE', 'BISHOP', 'NEMESIS'
    race             text,
    countdown        int,
    image_url        text,
    status           text        not null default 'SUBMITTED' check (status in ('SUBMITTED', 'IMPLEMENTED')),
    likes            int         not null default 0,
    created_at       timestamptz not null default now(),
    updated_at       timestamptz not null default now()
);

create index if not exists idx_workshop_card_author on workshop_card(author_id);
create index if not exists idx_workshop_card_status on workshop_card(status);
create index if not exists idx_workshop_card_likes on workshop_card(likes desc);

-- 2. Workshop Comments
create table if not exists workshop_comment (
    id               bigserial primary key,
    card_id          bigint      not null references workshop_card(id) on delete cascade,
    author_id        bigint      not null references user_account(id) on delete cascade,
    content          text        not null,
    created_at       timestamptz not null default now()
);

create index if not exists idx_workshop_comment_card on workshop_comment(card_id);
create index if not exists idx_workshop_comment_author on workshop_comment(author_id);

-- 3. Workshop Votes (Likes)
create table if not exists workshop_vote (
    id          bigserial primary key,
    user_id     bigint      not null references user_account(id) on delete cascade,
    card_id     bigint      not null references workshop_card(id) on delete cascade,
    created_at  timestamptz not null default now(),
    unique (user_id, card_id)
);

create index if not exists idx_workshop_vote_card on workshop_vote(card_id);
