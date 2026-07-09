create extension if not exists pg_trgm;

create index if not exists idx_explaining_search_text_trgm
on public.explaining using gin(search_text gin_trgm_ops);
