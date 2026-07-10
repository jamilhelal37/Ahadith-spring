# Ahadith API

Spring Boot API for browsing and managing Hadith content, authentication, user profile features, scholar workflows, and admin operations.

## Tech Stack

- Java 21
- Spring Boot 3.3.2
- Spring Security with JWT
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Maven Wrapper
- Cloudinary integration for profile images
- Docker / Docker Compose

## Requirements

- Java 21
- Docker and Docker Compose, if running with containers
- PostgreSQL 16, if running without Docker

## Environment Variables

Use `.env.example` as the local template:

```env
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ahadith
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
PORT=8080
JWT_SECRET=change-this-secret-to-at-least-64-characters-long-for-hs256-security
JWT_ACCESS_EXPIRATION=1h
JWT_REFRESH_EXPIRATION=7d
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-gmail-address@gmail.com
SPRING_MAIL_PASSWORD=your-google-app-password
SPRING_MAIL_TEST_CONNECTION=false
APP_MAIL_ENABLED=true
APP_MAIL_FROM=your-gmail-address@gmail.com
APP_MAIL_FRONTEND_BASE_URL=https://app.example.com
APP_MAIL_VERIFICATION_BASE_URL=
APP_MAIL_VERIFICATION_PATH=/verify-email
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=
SEARCH_HISTORY_MAX_PER_USER=50
LOGIN_MAX_FAILURES=5
LOGIN_LOCK_DURATION=15m
```

Do not commit a real `.env` file or production secrets. Spring Boot does not read `.env` automatically; Docker Compose, your shell, IntelliJ run configuration, or the deployment platform must load those variables into the process environment.

## Run Locally

Start PostgreSQL locally, create an `ahadith` database, then run:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Run Tests

```bash
./mvnw test
```

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

## Build

```bash
./mvnw clean package
```

The application jar is generated under `target/`.

## Docker Compose

Build and run the application with PostgreSQL:

```bash
docker compose up --build
```

Services:

- App: `http://localhost:8080`
- PostgreSQL: `localhost:5432`
- Database: `ahadith`
- Username/password: `postgres` / `postgres`

Stop services:

```bash
docker compose down
```

Remove database volume:

```bash
docker compose down -v
```

## Main API Areas

- Public content: `/ahadith`, `/books`, `/rawis`, `/rulings`, `/topics`, `/muhaddiths`, `/explaining`, `/fake-ahadith`
- Authentication: `/auth/register`, `/auth/login`, `/auth/refresh`
- Email/account recovery: `/auth/verify-email`, `/auth/resend-verification`, `/auth/forgot-password`, `/auth/reset-password`
- Current user: `/me/**`
- Current user sessions: `/auth/logout`, `/me/logout-all`
- Scholar workflows: `/scholar/**`
- Admin operations: `/admin/**`
- Swagger/OpenAPI: `/swagger-ui/**`, `/v3/api-docs/**`

## Security Notes

- Roles are `MEMBER`, `SCHOLAR`, and `ADMIN`, mapped directly to `ROLE_MEMBER`, `ROLE_SCHOLAR`, and `ROLE_ADMIN`.
- `/admin/**` requires `ROLE_ADMIN`; `SCHOLAR` does not receive admin access.
- `/scholar/**` requires `ROLE_SCHOLAR` or `ROLE_ADMIN`.
- Public GET content endpoints remain open.
- Registration creates a `pending_confirmation` user and sends a one-time verification email. Login is allowed only after email verification sets the user to `active`.
- Access tokens are short-lived JWTs. Refresh tokens are rotated and stored only as hashes in `refresh_token_sessions`.
- `/auth/refresh` uses the `refreshToken` request body. Replay of a revoked refresh token revokes all refresh sessions for that user.
- `/auth/logout` revokes the supplied refresh token. `/me/logout-all` revokes all refresh sessions for the authenticated user.
- Forgot/reset password uses one-time hashed reset tokens and revokes all refresh sessions after a successful reset.
- Login protection tracks failed attempts by normalized email and client IP. Proxy headers are used only when `TRUSTED_PROXY_HEADERS=true`.
- Use a strong `JWT_SECRET` of at least 64 characters in production.
- Production SQL logging is disabled in `application-prod.yml`.

## Production Configuration

Production must run with `SPRING_PROFILES_ACTIVE=prod`. `application-prod.yml` uses `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`; Flyway and JPA share that datasource. Sensitive production values have no safe fallback and startup fails when required JWT, mail, or Cloudinary settings are missing.

JWT lifetimes use Spring duration syntax:

- `JWT_ACCESS_EXPIRATION=1h`
- `JWT_REFRESH_EXPIRATION=7d`

The owner may keep direct local development values in an untracked `src/main/resources/application-dev.yml`. Do not commit real `.env` files or secrets.

## Email Configuration

The project uses Spring Boot mail auto-configuration. `spring-boot-starter-mail` provides `JavaMailSender` when `spring.mail.host` is configured. For local Gmail SMTP, keep the real values in untracked local configuration or environment variables:

- `spring.mail.host=smtp.gmail.com`
- `spring.mail.port=587`
- `spring.mail.username=<gmail address>`
- `spring.mail.password=<Google App Password>`
- `spring.mail.test-connection=false` for normal development runs
- `app.mail.enabled=true`
- `app.mail.from=<same verified sender address>`
- `app.mail.frontend-base-url=<password reset frontend URL>`
- `app.mail.verification-base-url=http://localhost:8080`
- `app.mail.verification-path=/verify-email`

Use a Google App Password, not the normal Gmail account password. The App Password must never be committed or printed in logs.

For production, set environment variables:

- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `APP_MAIL_ENABLED`
- `APP_MAIL_FROM`
- `APP_MAIL_FRONTEND_BASE_URL`
- `APP_MAIL_VERIFICATION_BASE_URL`
- `APP_MAIL_VERIFICATION_PATH`

To test Gmail connectivity locally, temporarily set `spring.mail.test-connection=true` in the local `application-dev.yml`, start with the `dev` profile, verify startup reaches `Started AhadithApplication`, then set it back to `false`. Common failures:

- `JavaMailSender` missing: `app.mail.enabled=true` but `spring.mail.host` is not loaded into the Spring Environment.
- Authentication failed: use a Google App Password and verify the account has 2-Step Verification enabled.
- `.env` changes ignored: load `.env` through Docker Compose, shell variables, or the IDE run configuration.

Verification emails link to the temporary same-origin page:

- Local: `http://localhost:8080/verify-email?token=...`
- Render: `${RENDER_EXTERNAL_URL}/verify-email?token=...`

The click flow is: email link -> `GET /verify-email` -> static page JavaScript -> `POST /auth/verify-email` -> success/error message. No second manual verification action is required. The page removes the token from the visible address bar after reading it and does not store it in browser storage.

On Render, `APP_MAIL_VERIFICATION_BASE_URL` is optional because Render provides `RENDER_EXTERNAL_URL`. Set `APP_MAIL_VERIFICATION_BASE_URL` later if a custom domain should replace the Render URL. Keep `APP_MAIL_VERIFICATION_PATH=/verify-email`.

Password-reset emails continue to use `${APP_MAIL_FRONTEND_BASE_URL}/reset-password?token=...`. Do not point `APP_MAIL_FRONTEND_BASE_URL` at the API unless that is also where the reset-password UI is served.

The temporary verification page is served from `src/main/resources/static` by the Spring Boot API. Replace it later by changing `app.mail.verification-base-url` and/or the email template once the web or mobile team provides the final UI. The raw one-time token is sent only in the email link; the database stores only its hash.

For Render Web Services:

- Set `SPRING_PROFILES_ACTIVE=prod`.
- Render supplies `PORT`; the app listens on that port and binds to `0.0.0.0`.
- Render supplies `RENDER_EXTERNAL_URL`; do not hardcode the `*.onrender.com` hostname in source code.
- Set datasource, JWT, Gmail SMTP, Cloudinary, and mail sender variables in the Render Dashboard. Use `sync: false` for secrets if you manage them through infrastructure files.

## Upgrade Requests

Members create scholar upgrade requests with `POST /me/upgrade-requests`. The server associates the request with the authenticated user and rejects duplicate open requests with `409`. Current/history endpoints are `GET /me/upgrade-requests/current` and `GET /me/upgrade-requests`.

Admins review requests with `PATCH /admin/upgrade-requests/{id}/review` using `decision=APPROVE` or `REJECT`. Approval promotes the user from `member` to `scholar`, writes an activity log, and creates a user notification in the same transaction.

## Admin Pagination

Admin collection endpoints under `/admin/**` accept `page`, `size`, and `sort`. Defaults are `page=0` and `size=20`; maximum size is `100`. Sort fields are allowlisted per resource. Responses use the existing `items` plus pagination metadata shape.

## Search Modes

Hadith search preserves both selectable modes:

- `EXACT`: normalizes Arabic input and matches directly against `search_text`.
- `FLEXIBLE`: uses PostgreSQL full-text search with the existing search vector/ranking behavior.

Filtering, sorting, and pagination run in PostgreSQL for the modern search endpoint.

## Migrations

`V6__security_sessions_upgrade_review.sql` adds refresh-token sessions, email verification tokens, password-reset tokens, login-attempt tracking, upgrade review fields, notification recipients, and indexes used by ownership and admin pagination queries. Apply it with normal Flyway deployment; existing V1-V5 migrations are unchanged.
