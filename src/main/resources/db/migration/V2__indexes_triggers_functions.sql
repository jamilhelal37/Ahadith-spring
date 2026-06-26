create extension if not exists pgcrypto;
-- =========================
-- CORE FK / RELATION INDEXES
-- =========================
create index on public.ahadith(book, rawi);
create index on public.ahadith(book, ruling);
create index on public.comments(user_id, created_at desc);

create index if not exists idx_books_muhaddith
on public.books(muhaddith);

create index if not exists idx_ahadith_sub_valid
on public.ahadith(sub_valid);
create index on public.activity_log(created_at desc);
create index on public.activity_log(actor_user_id, created_at desc);
create index if not exists idx_ahadith_explaining
on public.ahadith(explaining);

create index if not exists idx_ahadith_ruling
on public.ahadith(ruling);

create index if not exists idx_ahadith_rawi
on public.ahadith(rawi);

create index if not exists idx_ahadith_book
on public.ahadith(book);

create index if not exists idx_ahadith_book_hadith_number
on public.ahadith(book, hadith_number);

-- =========================
-- SEARCH INDEXES (AHADITH)
-- =========================

create index if not exists idx_ahadith_search_vector
on public.ahadith using gin(search_vector);

create index if not exists idx_ahadith_search_text_trgm
on public.ahadith using gin(search_text gin_trgm_ops);


-- =========================
-- SEARCH INDEXES (FAKE AHADITH)
-- =========================

create index if not exists idx_fake_ahadith_sub_valid
on public.fake_ahadith(sub_valid);

create index if not exists idx_fake_ahadith_ruling
on public.fake_ahadith(ruling);

create index if not exists idx_fake_ahadith_search_text_trgm
on public.fake_ahadith using gin(search_text gin_trgm_ops);


-- =========================
-- COMMENTS
-- =========================

create index if not exists idx_comments_hadith
on public.comments(hadith);

create index if not exists idx_comments_user
on public.comments(user_id);

create index if not exists idx_comments_hadith_created_at
on public.comments(hadith, created_at desc);

-- =========================
-- FAVORITES
-- =========================

create index if not exists idx_favorites_hadith
on public.favorites(hadith);

create index if not exists idx_favorites_user_hadith
on public.favorites(user_id, hadith);

-- =========================
-- QUESTIONS
-- =========================

create index if not exists idx_questions_asker
on public.questions(asker);

create index if not exists idx_questions_hadith
on public.questions(hadith_id);

create index if not exists idx_questions_hadith_created_at
on public.questions(hadith_id, created_at desc);

-- =========================
-- SEARCH HISTORY
-- =========================

create index if not exists idx_search_history_user
on public.search_history(user_id);

create index if not exists idx_search_history_created_at
on public.search_history(created_at);

-- =========================
-- SIMILAR / TOPICS
-- =========================

create index if not exists idx_similar_ahadith_main
on public.similar_ahadith(main_hadith);

create index if not exists idx_similar_ahadith_sim
on public.similar_ahadith(sim_hadith);

create index if not exists idx_topic_classes_topic
on public.topic_classes(topic);

create index if not exists idx_topic_classes_hadith
on public.topic_classes(hadith);

-- =========================
-- UPGRADE REQUESTS
-- =========================

create index if not exists idx_upgrade_requests_user
on public.upgrade_requests(user_id);

create index if not exists idx_upgrade_requests_reviewed_by
on public.upgrade_requests(reviewed_by);

create index if not exists idx_upgrade_requests_status
on public.upgrade_requests(status);

create index if not exists idx_upgrade_requests_created_at
on public.upgrade_requests(created_at);

create index if not exists idx_upgrade_requests_status_created_at
on public.upgrade_requests(status, created_at desc);

create index if not exists idx_upgrade_requests_user_status
on public.upgrade_requests(user_id, status);

-- =========================
-- ACTIVITY LOG
-- =========================

create index if not exists idx_activity_log_actor
on public.activity_log(actor_user_id);

create index if not exists idx_activity_log_table_name
on public.activity_log(table_name);

create index if not exists idx_activity_log_record_id
on public.activity_log(record_id);

create index if not exists idx_activity_log_created_at
on public.activity_log(created_at desc);

create index if not exists idx_activity_log_actor_created_at
on public.activity_log(actor_user_id, created_at desc);

-- =========================
-- NOTIFICATIONS 
-- =========================

create index if not exists idx_notifications_type
on public.notifications(type);

create index if not exists idx_notifications_created_at
on public.notifications(created_at desc);

create index if not exists idx_notifications_type_created_at
on public.notifications(type, created_at desc);

create index if not exists idx_notifications_hadith_id
on public.notifications(hadith_id);

create index if not exists idx_notifications_fake_hadith_id
on public.notifications(fake_hadith_id);

-- =========================
-- FCM TOKENS
-- =========================

create index if not exists idx_user_fcm_tokens_last_seen
on public.user_fcm_tokens(last_seen);

-- =========================
-- FUNCTIONS
-- =========================

create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;


create or replace function public.remove_arabic_diacritics(p_text text)
returns text
language plpgsql
immutable
strict
as $$
begin
  return translate(
    p_text,
    'ًٌٍَُِّْٰٱـۖۗۘۙۚۛۜ۝۞ۣ۟۠ۡۢۤۥۦۧۨ۩۪ۭ۫۬ۮۯ',
    ''
  );
end;
$$;


create or replace function public.arab_norm(p_text text)
returns text
language plpgsql
immutable
strict
as $$
declare
  v_text text;
begin
  v_text := translate(
    p_text,
    'ًٌٍَُِّْٰٱـۖۗۘۙۚۛۜ۝۞ۣ۟۠ۡۢۤۥۦۧۨ۩۪ۭ۫۬ۮۯ',
    ''
  );

  v_text := replace(v_text, 'أ', 'ا');
  v_text := replace(v_text, 'إ', 'ا');
  v_text := replace(v_text, 'آ', 'ا');
  v_text := replace(v_text, 'ٱ', 'ا');
  v_text := replace(v_text, 'ى', 'ي');
  v_text := replace(v_text, 'ة', 'ه');
  v_text := replace(v_text, 'ؤ', 'و');
  v_text := replace(v_text, 'ئ', 'ي');
  v_text := replace(v_text, 'ء', '');

  v_text := regexp_replace(v_text, '[^\p{L}\p{N}\s]', ' ', 'g');
  v_text := regexp_replace(v_text, '\s+', ' ', 'g');
  v_text := btrim(v_text);

  return v_text;
end;
$$;

create or replace function public.set_arabic_text_variants()
returns trigger
language plpgsql
as $$
begin 
  if new.text is not null then
    new.normal_text := public.remove_arabic_diacritics(new.text);
    new.search_text := public.arab_norm(new.normal_text);
  end if;

  return new;
end;
$$;
create or replace function public.set_ahadith_search_vector()
returns trigger
language plpgsql
as $$
begin
  new.search_vector :=
    to_tsvector('arabic', public.arab_norm(new.text));
  return new;
end;
$$;

-- =========================
-- TRIGGERS
-- =========================

drop trigger if exists trg_ahadith_text_variants on public.ahadith;
create trigger trg_ahadith_text_variants
before insert or update of text
on public.ahadith
for each row
execute function public.set_arabic_text_variants();


drop trigger if exists trg_fake_ahadith_text_variants on public.fake_ahadith;
create trigger trg_fake_ahadith_text_variants
before insert or update of text
on public.fake_ahadith
for each row
execute function public.set_arabic_text_variants();


drop trigger if exists trg_explaining_text_variants on public.explaining;
create trigger trg_explaining_text_variants
before insert or update of text
on public.explaining
for each row
execute function public.set_arabic_text_variants();

drop trigger if exists trg_ahadith_search_vector on public.ahadith;
create trigger trg_ahadith_search_vector
before insert or update of text
on public.ahadith
for each row
execute function public.set_ahadith_search_vector();

-- =========================
-- TRIGGERS (UPDATED_AT)
-- =========================

drop trigger if exists trg_users_updated_at on public.users;
create trigger trg_users_updated_at
before update on public.users
for each row execute function public.set_updated_at();

drop trigger if exists trg_ruling_updated_at on public.ruling;
create trigger trg_ruling_updated_at
before update on public.ruling
for each row execute function public.set_updated_at();

drop trigger if exists trg_muhaddiths_updated_at on public.muhaddiths;
create trigger trg_muhaddiths_updated_at
before update on public.muhaddiths
for each row execute function public.set_updated_at();

drop trigger if exists trg_rawis_updated_at on public.rawis;
create trigger trg_rawis_updated_at
before update on public.rawis
for each row execute function public.set_updated_at();

drop trigger if exists trg_explaining_updated_at on public.explaining;
create trigger trg_explaining_updated_at
before update on public.explaining
for each row execute function public.set_updated_at();

drop trigger if exists trg_books_updated_at on public.books;
create trigger trg_books_updated_at
before update on public.books
for each row execute function public.set_updated_at();

drop trigger if exists trg_topics_updated_at on public.topics;
create trigger trg_topics_updated_at
before update on public.topics
for each row execute function public.set_updated_at();

drop trigger if exists trg_ahadith_updated_at on public.ahadith;
create trigger trg_ahadith_updated_at
before update on public.ahadith
for each row execute function public.set_updated_at();

drop trigger if exists trg_fake_ahadith_updated_at on public.fake_ahadith;
create trigger trg_fake_ahadith_updated_at
before update on public.fake_ahadith
for each row execute function public.set_updated_at();

drop trigger if exists trg_comments_updated_at on public.comments;
create trigger trg_comments_updated_at
before update on public.comments
for each row execute function public.set_updated_at();

drop trigger if exists trg_questions_updated_at on public.questions;
create trigger trg_questions_updated_at
before update on public.questions
for each row execute function public.set_updated_at();

drop trigger if exists trg_similar_ahadith_updated_at on public.similar_ahadith;
create trigger trg_similar_ahadith_updated_at
before update on public.similar_ahadith
for each row execute function public.set_updated_at();

drop trigger if exists trg_topic_classes_updated_at on public.topic_classes;
create trigger trg_topic_classes_updated_at
before update on public.topic_classes
for each row execute function public.set_updated_at();

drop trigger if exists trg_upgrade_requests_updated_at on public.upgrade_requests;
create trigger trg_upgrade_requests_updated_at
before update on public.upgrade_requests
for each row execute function public.set_updated_at();

drop trigger if exists trg_favorites_updated_at on public.favorites;
create trigger trg_favorites_updated_at
before update on public.favorites
for each row execute function public.set_updated_at();

drop trigger if exists trg_user_fcm_tokens_updated_at on public.user_fcm_tokens;
create trigger trg_user_fcm_tokens_updated_at
before update on public.user_fcm_tokens
for each row execute function public.set_updated_at();