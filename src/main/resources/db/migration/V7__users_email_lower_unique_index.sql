do $$
begin
    if exists (
        select 1
        from public.users
        where email is not null
        group by lower(trim(email))
        having count(*) > 1
    ) then
        raise exception 'Cannot create case-insensitive users email unique index: duplicate normalized emails exist';
    end if;
end $$;

create unique index if not exists uq_users_email_lower
    on public.users (lower(trim(email)))
    where email is not null;
