drop trigger if exists trg_login_attempts_updated_at
on public.login_attempts;

create trigger trg_login_attempts_updated_at
before update on public.login_attempts
for each row
execute function public.set_updated_at();
