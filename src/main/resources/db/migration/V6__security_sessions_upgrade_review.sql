alter table public.upgrade_requests
    add column if not exists review_notes text,
    add column if not exists rejection_reason text,
    add column if not exists reviewed_at timestamptz;

alter table public.notifications
    add column if not exists user_id uuid;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'fk_notifications_user'
    ) then
        alter table public.notifications
            add constraint fk_notifications_user
            foreign key (user_id)
            references public.users(id)
            on delete cascade;
    end if;
end $$;

create table if not exists public.refresh_token_sessions (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references public.users(id) on delete cascade,
    token_hash text not null unique,
    token_id text not null unique,
    family_id uuid not null,
    issued_at timestamptz not null,
    expires_at timestamptz not null,
    revoked_at timestamptz,
    replaced_by_token_id text,
    user_agent text,
    ip_address text,
    created_at timestamptz not null default current_timestamp
);

create table if not exists public.email_verification_tokens (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references public.users(id) on delete cascade,
    token_hash text not null unique,
    expires_at timestamptz not null,
    consumed_at timestamptz,
    last_sent_at timestamptz not null,
    created_at timestamptz not null default current_timestamp
);

create table if not exists public.password_reset_tokens (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references public.users(id) on delete cascade,
    token_hash text not null unique,
    expires_at timestamptz not null,
    consumed_at timestamptz,
    created_at timestamptz not null default current_timestamp
);

create table if not exists public.login_attempts (
    id uuid primary key default gen_random_uuid(),
    email_key text not null,
    ip_address text,
    failed_count integer not null default 0,
    locked_until timestamptz,
    last_failed_at timestamptz,
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp,
    constraint uq_login_attempts_email_ip unique (email_key, ip_address)
);

create index if not exists idx_refresh_token_sessions_user_id on public.refresh_token_sessions(user_id);
create index if not exists idx_refresh_token_sessions_family_id on public.refresh_token_sessions(family_id);
create index if not exists idx_refresh_token_sessions_expires_at on public.refresh_token_sessions(expires_at);
create index if not exists idx_email_verification_tokens_user_id on public.email_verification_tokens(user_id);
create index if not exists idx_email_verification_tokens_expires_at on public.email_verification_tokens(expires_at);
create index if not exists idx_password_reset_tokens_expires_at on public.password_reset_tokens(expires_at);
create index if not exists idx_upgrade_requests_status_created_at on public.upgrade_requests(status, created_at desc);
create index if not exists idx_questions_asker_created_at on public.questions(asker, created_at desc);
create index if not exists idx_comments_user_created_at on public.comments(user_id, created_at desc);
create index if not exists idx_search_history_user_created_at on public.search_history(user_id, created_at desc);
create index if not exists idx_notifications_user_created_at on public.notifications(user_id, created_at desc);
