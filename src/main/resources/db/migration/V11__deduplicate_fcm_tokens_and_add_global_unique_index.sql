delete from public.user_fcm_tokens token
using (
    select ctid
    from (
        select ctid,
               row_number() over (
                   partition by fcm_token
                   order by last_seen desc nulls last,
                            updated_at desc nulls last,
                            created_at desc nulls last,
                            id desc
               ) as duplicate_rank
        from public.user_fcm_tokens
    ) ranked_tokens
    where ranked_tokens.duplicate_rank > 1
) duplicates
where token.ctid = duplicates.ctid;

alter table public.user_fcm_tokens
    drop constraint if exists user_fcm_tokens_user_id_fcm_token_key;

create unique index if not exists uq_user_fcm_tokens_fcm_token
    on public.user_fcm_tokens (fcm_token);
