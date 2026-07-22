alter table public.upgrade_requests
    add column if not exists document_asset_id text null,
    add column if not exists document_public_id text null,
    add column if not exists document_resource_type text null,
    add column if not exists document_delivery_type text null,
    add column if not exists document_format text null,
    add column if not exists document_original_name text null,
    add column if not exists document_size_bytes bigint null;

alter table public.upgrade_requests
    add constraint chk_upgrade_requests_document_size_non_negative
    check (document_size_bytes is null or document_size_bytes >= 0);

create index if not exists idx_upgrade_requests_document_public_id
    on public.upgrade_requests(document_public_id)
    where document_public_id is not null;

create index if not exists idx_upgrade_requests_user_created_at
    on public.upgrade_requests(user_id, created_at desc, id);
