create table if not exists public.activity_log (
  id uuid primary key default gen_random_uuid(),
  actor_user_id uuid,
  actor_name text,
  actor_email text,
  actor_avatar_url text,
  message text not null,
  table_name text,
  record_id uuid,
  old_data jsonb,
  new_data jsonb,
  created_at timestamptz not null default current_timestamp
);

create index if not exists idx_activity_log_created_at
  on public.activity_log (created_at desc);

create index if not exists idx_activity_log_table_record
  on public.activity_log (table_name, record_id);
