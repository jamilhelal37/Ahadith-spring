create extension if not exists pg_trgm;
create extension if not exists pgcrypto;

create type public.gender as enum ('male', 'female');
create type public.user_type as enum ('admin', 'member', 'scholar');
create type public.hadith_type as enum ('marfu', 'mawquf', 'qudsi', 'atharSahaba');
create type public.search_source as enum ('hadith', 'fake_hadith');
create type public.pro_upgrade_status as enum (
  'pending_documents',
  'under_review',
  'approved',
  'rejected'
);


create type public.user_status as enum (
  'pending_confirmation',
  'active',
  'disabled'
);
create type public.notification_type as enum (
  'daily_hadith',
  'fake_hadith',
  'general'
);
create table if not exists public.users (
  id uuid primary key default gen_random_uuid(),
  name text,
  email text not null unique,
  password text not null,
  avatar_url text,
  status public.user_status not null default 'pending_confirmation',
  gender public.gender,
  type public.user_type not null default 'member',
  birth_date date,
  created_at timestamptz not null default current_timestamp,
  updated_at timestamptz not null default current_timestamp
);

create table if not exists public.ruling (
  id uuid primary key default gen_random_uuid(),
  name text not null unique,
  created_by uuid,
  updated_by uuid,
  created_at timestamptz not null default CURRENT_TIMESTAMP,
  updated_at timestamptz not null default CURRENT_TIMESTAMP,

  constraint fk_ruling_created_by
    foreign key (created_by)
    references public.users(id)
    on delete set null
    on update cascade,

  constraint fk_ruling_updated_by
    foreign key (updated_by)
    references public.users(id)
    on delete set null
    on update cascade
);

create table if not exists public.muhaddiths (
  id uuid primary key default gen_random_uuid(),
  name text not null unique,
  gender public.gender not null,
  about text not null,
  created_by uuid,
  updated_by uuid,
  created_at timestamptz not null default CURRENT_TIMESTAMP,
  updated_at timestamptz not null default CURRENT_TIMESTAMP,

  constraint fk_muhaddiths_created_by
    foreign key (created_by)
    references public.users(id)
    on delete set null
    on update cascade,

  constraint fk_muhaddiths_updated_by
    foreign key (updated_by)
    references public.users(id)
    on delete set null
    on update cascade
);

create table if not exists public.rawis (
  id uuid primary key default gen_random_uuid(),
  name text not null unique,
  gender public.gender not null,
  about text not null,
  created_by uuid,
  updated_by uuid,
  created_at timestamptz not null default CURRENT_TIMESTAMP,
  updated_at timestamptz not null default CURRENT_TIMESTAMP,

  constraint fk_rawis_created_by
    foreign key (created_by)
    references public.users(id)
    on delete set null
    on update cascade,

  constraint fk_rawis_updated_by
    foreign key (updated_by)
    references public.users(id)
    on delete set null
    on update cascade
);

create table if not exists public.explaining (
  id uuid primary key default gen_random_uuid(),
  text text not null,
   normal_text text,
  search_text text,
  created_by uuid,
  updated_by uuid,
  created_at timestamptz not null default CURRENT_TIMESTAMP,
  updated_at timestamptz not null default CURRENT_TIMESTAMP,

  constraint fk_explaining_created_by
    foreign key (created_by)
    references public.users(id)
    on delete set null
    on update cascade,

  constraint fk_explaining_updated_by
    foreign key (updated_by)
    references public.users(id)
    on delete set null
    on update cascade
);

create table if not exists public.books (
  id uuid primary key default gen_random_uuid(),
  name text not null unique,
  muhaddith uuid,
  created_by uuid,
  updated_by uuid,
  created_at timestamptz not null default CURRENT_TIMESTAMP,
  updated_at timestamptz not null default CURRENT_TIMESTAMP,

  constraint fk_books_muhaddith
    foreign key (muhaddith)
    references public.muhaddiths(id)
    on delete set null
    on update cascade,

  constraint fk_books_created_by
    foreign key (created_by)
    references public.users(id)
    on delete set null
    on update cascade,

  constraint fk_books_updated_by
    foreign key (updated_by)
    references public.users(id)
    on delete set null
    on update cascade
);

create table if not exists public.topics (
  id uuid primary key default gen_random_uuid(),
  name text not null unique,
  created_by uuid,
  updated_by uuid,
  created_at timestamptz not null default CURRENT_TIMESTAMP,
  updated_at timestamptz not null default CURRENT_TIMESTAMP,

  constraint fk_topics_created_by
    foreign key (created_by)
    references public.users(id)
    on delete set null
    on update cascade,

  constraint fk_topics_updated_by
    foreign key (updated_by)
    references public.users(id)
    on delete set null
    on update cascade
);


create table if not exists public.ahadith (
  id uuid primary key default gen_random_uuid(),
  sub_valid uuid null,
  explaining uuid null,
  type public.hadith_type not null default 'marfu',
  text text not null,
  normal_text text,
  search_text text,
  search_vector tsvector,
  hadith_number integer not null,
  ruling uuid null,
  rawi uuid null,
  book uuid null,
  sanad text null,
  created_by uuid null,
  updated_by uuid null,
  created_at timestamptz not null default current_timestamp,
  updated_at timestamptz not null default current_timestamp,

  constraint fk_ahadith_sub_valid
    foreign key (sub_valid)
    references public.ahadith(id)
    on delete set null
    on update cascade,

  constraint fk_ahadith_explaining
    foreign key (explaining)
    references public.explaining(id)
    on delete set null
    on update cascade,

  constraint fk_ahadith_ruling
    foreign key (ruling)
    references public.ruling(id)
    on delete set null
    on update cascade,

 

  constraint fk_ahadith_rawi
    foreign key (rawi)
    references public.rawis(id)
    on delete set null
    on update cascade,

  constraint fk_ahadith_book
    foreign key (book)
    references public.books(id)
    on delete set null
    on update cascade,

  constraint fk_ahadith_created_by
    foreign key (created_by)
    references public.users(id)
    on delete set null
    on update cascade,

  constraint fk_ahadith_updated_by
    foreign key (updated_by)
    references public.users(id)
    on delete set null
    on update cascade,
  constraint chk_hadith_number_positive
    check (hadith_number > 0),

  constraint uq_ahadith_book_hadith_number
   unique (book, hadith_number)
);

create table if not exists public.fake_ahadith (
  id uuid primary key default gen_random_uuid(),
  sub_valid uuid null,
  text text not null,
  normal_text text,
  search_text text,
  ruling uuid null,
  created_by uuid null,
  updated_by uuid null,
  created_at timestamptz not null default current_timestamp,
  updated_at timestamptz not null default current_timestamp,

  constraint fk_fake_ahadith_sub_valid
    foreign key (sub_valid)
    references public.ahadith(id)
    on delete set null
    on update cascade,

  constraint fk_fake_ahadith_ruling
    foreign key (ruling)
    references public.ruling(id)
    on delete set null
    on update cascade,

  constraint fk_fake_ahadith_created_by
    foreign key (created_by)
    references public.users(id)
    on delete set null
    on update cascade,

  constraint fk_fake_ahadith_updated_by
    foreign key (updated_by)
    references public.users(id)
    on delete set null
    on update cascade
);


create table if not exists public.comments (
  id uuid primary key default gen_random_uuid(),
  hadith uuid not null,
  user_id uuid not null,
  text text not null,
  created_at timestamptz not null default current_timestamp,
  updated_at timestamptz not null default current_timestamp,

  constraint fk_comments_hadith
    foreign key (hadith)
    references public.ahadith(id)
    on delete cascade
    on update cascade,

  constraint fk_comments_user
    foreign key (user_id)
    references public.users(id)
    on delete cascade
    on update cascade
);

create table if not exists public.favorites (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null,
  hadith uuid not null,
  created_at timestamptz not null default current_timestamp,
  updated_at timestamptz not null default current_timestamp,

  constraint fk_favorites_user
    foreign key (user_id)
    references public.users(id)
    on delete cascade
    on update cascade,

  constraint fk_favorites_hadith
    foreign key (hadith)
    references public.ahadith(id)
    on delete cascade
    on update cascade,

  constraint uq_favorites_user_hadith unique (user_id, hadith)
);



create table if not exists public.questions (
  id uuid primary key default gen_random_uuid(),
  hadith_id uuid null,
  asker uuid not null,
  asker_text text not null,
  is_active boolean not null default false,
  answer_text text null,
  updated_by uuid null,
  created_at timestamptz not null default current_timestamp,
  updated_at timestamptz not null default current_timestamp,

  constraint fk_questions_asker
    foreign key (asker)
    references public.users(id)
    on delete cascade
    on update cascade,


  constraint fk_questions_updated_by
    foreign key (updated_by)
    references public.users(id)
    on delete set null
    on update cascade,
  constraint fk_questions_hadith
    foreign key (hadith_id)
    references public.ahadith(id)
    on delete set null
    on update cascade
);
create table if not exists public.search_history (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null,
  search_text text not null,
  search_source public.search_source not null default 'hadith',
  created_at timestamptz not null default current_timestamp,
  updated_at timestamptz not null default current_timestamp,

  constraint fk_search_history_user
    foreign key (user_id )
    references public.users(id)
    on delete cascade
    on update cascade
);


create table if not exists public.similar_ahadith (
  id uuid primary key default gen_random_uuid(),
  main_hadith uuid null,
  sim_hadith uuid null,
  created_by uuid,
  updated_by uuid,
  created_at timestamptz not null default current_timestamp,
  updated_at timestamptz not null default current_timestamp,

  constraint fk_similar_main
    foreign key (main_hadith)
    references public.ahadith(id)
    on delete set null
    on update cascade,

  constraint fk_similar_sim
    foreign key (sim_hadith)
    references public.ahadith(id)
    on delete set null
    on update cascade,

  constraint fk_similar_ahadith_created_by
    foreign key (created_by)
    references public.users(id)
    on delete set null
    on update cascade,

  constraint fk_similar_ahadith_updated_by
    foreign key (updated_by)
    references public.users(id)
    on delete set null
    on update cascade,

  constraint uq_similar_ahadith_main_sim 
   unique (main_hadith, sim_hadith),

  constraint chk_not_self
    check (main_hadith <> sim_hadith)
);

create table if not exists public.topic_classes (
  id uuid primary key default gen_random_uuid(),
  topic uuid not null,
  hadith uuid not null,
  created_by uuid,
  updated_by uuid,
  created_at timestamptz not null default current_timestamp,
  updated_at timestamptz not null default current_timestamp,

  constraint fk_topic_classes_topic
    foreign key (topic)
    references public.topics(id)
    on delete cascade
    on update cascade,

  constraint fk_topic_classes_hadith
    foreign key (hadith)
    references public.ahadith(id)
    on delete cascade
    on update cascade,

  constraint fk_topic_classes_created_by
    foreign key (created_by)
    references public.users(id)
    on delete set null
    on update cascade,

  constraint fk_topic_classes_updated_by
    foreign key (updated_by)
    references public.users(id)
    on delete set null
    on update cascade,

  constraint uq_topic_class_topic_hadith unique (topic, hadith)
);

create table if not exists public.upgrade_requests (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null,
  status public.pro_upgrade_status not null default 'pending_documents',
  file_path text null,
  reviewed_by uuid null,
  notes text null,
  created_at timestamptz not null default current_timestamp,
  updated_at timestamptz not null default current_timestamp,

  constraint fk_upgrade_user
    foreign key (user_id)
    references public.users(id)
    on delete cascade,

  constraint fk_upgrade_reviewed_by
    foreign key (reviewed_by)
    references public.users(id)
    on delete set null   
);
create unique index if not exists uq_upgrade_requests_one_open_request
    on public.upgrade_requests(user_id)
    where status in ('pending_documents', 'under_review');


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
  created_at timestamptz not null default now()
);



create table if not exists public.user_fcm_tokens (
  id uuid not null default gen_random_uuid(),
  user_id uuid not null,
  fcm_token text not null,
  created_at timestamptz not null default current_timestamp,
  updated_at timestamptz not null default current_timestamp,
  last_seen timestamptz not null default current_timestamp,

  constraint user_fcm_tokens_pkey primary key (id),

  constraint user_fcm_tokens_user_id_fcm_token_key
    unique (user_id, fcm_token),

  constraint fk_user_fcm_tokens_user
    foreign key (user_id)
    references public.users(id)
    on delete cascade
    on update cascade
);



create table if not exists public.notifications (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  body text not null,
  type notification_type not null,
  hadith_id uuid null,
  fake_hadith_id uuid null,
  created_by uuid null,
  created_at timestamptz not null default current_timestamp,
  updated_at timestamptz not null default current_timestamp,

  constraint fk_notifications_hadith
    foreign key (hadith_id)
    references public.ahadith(id)
    on delete set null
    on update cascade,

  constraint fk_notifications_fake_hadith
    foreign key (fake_hadith_id)
    references public.fake_ahadith(id)
    on delete set null
    on update cascade,

  constraint fk_notifications_created_by
    foreign key (created_by)
    references public.users(id)
    on delete set null
    on update cascade );
