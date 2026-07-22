create index if not exists idx_login_attempts_updated_at
on public.login_attempts(updated_at);

create index if not exists idx_password_reset_tokens_expires_consumed
on public.password_reset_tokens(expires_at, consumed_at);

create index if not exists idx_email_verification_tokens_expires_consumed
on public.email_verification_tokens(expires_at, consumed_at);
