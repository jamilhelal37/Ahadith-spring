alter table public.users
    add column google_subject varchar(255);

create unique index if not exists uq_users_google_subject
    on public.users (google_subject)
    where google_subject is not null;

alter table public.users
    alter column password drop not null;
