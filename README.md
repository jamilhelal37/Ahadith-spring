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

Use `.env.example` as the local template. Copy it to `.env` beside `pom.xml`, then replace every placeholder:

```bash
cp .env.example .env
```

Required variables:

```text
SPRING_PROFILES_ACTIVE
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
PORT
JWT_SECRET
JWT_ACCESS_EXPIRATION
JWT_REFRESH_EXPIRATION
APP_MAIL_ENABLED
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
SPRING_MAIL_HOST
SPRING_MAIL_PORT
SPRING_MAIL_USERNAME
SPRING_MAIL_PASSWORD
APP_MAIL_FROM
APP_MAIL_FRONTEND_BASE_URL
```

Generate a strong local JWT secret with at least 64 random characters:

```bash
openssl rand -base64 64
```

Do not commit a real `.env` file or production secrets. Values should not be wrapped in quotes unless the value itself intentionally contains quote characters.

Docker Compose reads `.env` by itself. Direct IntelliJ and Maven execution load the same root `.env` through `spring.config.import=optional:file:./.env[.properties]` in `application.yml`. In IntelliJ, set the run configuration working directory to `$ProjectFileDir$` so `./.env` resolves beside `pom.xml`.

## Run Locally

Start PostgreSQL locally, create an `ahadith` database, put the required values in `.env`, then run:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Run Tests

Fast unit and slice tests run without Docker:

```bash
./mvnw test
```

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

The full suite runs unit tests plus PostgreSQL 16 integration tests through Testcontainers:

```bash
./mvnw verify
```

On Windows PowerShell:

```powershell
.\mvnw.cmd verify
```

H2 is used only for lightweight tests that do not depend on production PostgreSQL behavior. Tests named `*IT.java` run in Maven Failsafe during `verify`, start a PostgreSQL 16 container, apply the real Flyway migrations, and exercise PostgreSQL-specific search SQL, functions, triggers, and indexes. Docker must be running for `./mvnw verify`; if Docker is unavailable, the integration tests fail instead of being skipped.

## Build

```bash
./mvnw clean package
```

The application jar is generated under `target/`.

## CI

GitHub Actions runs `./mvnw --batch-mode verify` first, including PostgreSQL Testcontainers integration tests. The Docker image is built only after Maven verification succeeds, and the image is not pushed to a registry.

## Docker Compose

Build and run the application with PostgreSQL after creating `.env`:

```bash
docker compose up --build
```

Docker Compose reads `.env` automatically when present. The app service sets `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/ahadith` inside the container so PostgreSQL is reached through the Compose service name, while local Maven/IntelliJ runs can use `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ahadith`.

Services:

- App: `http://localhost:8080`
- PostgreSQL: `127.0.0.1:5432` for local development only
- Database: `ahadith`
- Username/password: from your untracked `.env`

Useful operations:

```bash
./mvnw verify
docker compose config
docker compose build
docker compose up -d
docker compose ps
docker compose logs
docker compose down
```

The PostgreSQL container uses `pg_isready`, and the app container checks `/actuator/health/readiness`. Only health endpoints are exposed through Actuator.

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
- Login protection tracks failed attempts by normalized email and client IP. Proxy header trust is disabled in application configuration.
- Use a strong `JWT_SECRET` of at least 64 characters in production.
- Production SQL logging is disabled in `application-prod.yml`.

## Mobile Public Catalog

These endpoints are public and do not require an `Authorization` header.

### List Muhaddiths

```http
GET /muhaddiths
```

```json
[
  {
    "serialNumber": 1,
    "id": "11111111-1111-1111-1111-111111111111",
    "name": "الإمام البخاري",
    "about": "نبذة عن المحدث"
  }
]
```

### List Rawis

```http
GET /rawis
```

```json
[
  {
    "serialNumber": 1,
    "id": "22222222-2222-2222-2222-222222222222",
    "name": "أبو هريرة",
    "about": null
  }
]
```

### List Books

```http
GET /books
```

```json
[
  {
    "serialNumber": 1,
    "id": "33333333-3333-3333-3333-333333333333",
    "name": "صحيح البخاري",
    "muhaddithId": "11111111-1111-1111-1111-111111111111",
    "muhaddithName": "الإمام البخاري"
  },
  {
    "serialNumber": 2,
    "id": "44444444-4444-4444-4444-444444444444",
    "name": "كتاب بلا محدث",
    "muhaddithId": null,
    "muhaddithName": null
  }
]
```

### Browse Book Ahadith

```http
GET /books/{bookId}/ahadith?page=0&size=50
```

`page` starts at `0`. The default `size` is `50`, and values larger than `50` are clamped to `50`.

```json
{
  "items": [
    {
      "id": "55555555-5555-5555-5555-555555555555",
      "text": "نص الحديث",
      "hadithNumber": 1,
      "type": "marfu",
      "book": {
        "id": "33333333-3333-3333-3333-333333333333",
        "name": "صحيح البخاري"
      },
      "rawi": {
        "id": "22222222-2222-2222-2222-222222222222",
        "name": "أبو هريرة"
      },
      "ruling": null,
      "muhaddith": {
        "id": "11111111-1111-1111-1111-111111111111",
        "name": "الإمام البخاري"
      },
      "topics": [],
      "hasExplanation": false
    }
  ],
  "pagination": {
    "page": 0,
    "size": 50,
    "totalItems": 250,
    "totalPages": 5,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

Catalog lists are ordered in the database by name ascending and then `id` ascending. Book ahadith are ordered in the database by `hadithNumber` ascending, then `createdAt` ascending, then `id` ascending, which keeps pagination stable across mobile infinite-scroll requests.

## Production Configuration

Production must run with `SPRING_PROFILES_ACTIVE=prod`. `application-prod.yml` uses the environment variable names listed above; Flyway and JPA share `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`. Sensitive production values have no safe fallback and startup fails when required JWT, mail, or Cloudinary settings are missing.

JWT lifetimes use Spring duration syntax:

- `JWT_ACCESS_EXPIRATION=1h`
- `JWT_REFRESH_EXPIRATION=7d`

Keep direct local development values in the untracked root `.env` file. Do not commit real `.env` files or secrets.

## Email Configuration

The project uses Spring Boot mail auto-configuration. `spring-boot-starter-mail` provides `JavaMailSender` when `SPRING_MAIL_HOST` is configured. For local Gmail SMTP, keep the real values in environment variables loaded from an untracked `.env`, your shell, Docker Compose, or the IDE run configuration:

- `SPRING_MAIL_HOST=smtp.gmail.com`
- `SPRING_MAIL_PORT=587`
- `SPRING_MAIL_USERNAME=<gmail address>`
- `SPRING_MAIL_PASSWORD=<Google App Password>`
- `APP_MAIL_FROM=<same verified sender address>`
- `APP_MAIL_FRONTEND_BASE_URL=<frontend URL>`

Use a Google App Password, not the normal Gmail account password. The App Password must never be committed or printed in logs.

For production, set environment variables:

- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `APP_MAIL_FROM`
- `APP_MAIL_FRONTEND_BASE_URL`

Mail connection testing is disabled in application configuration. Common failures:

- `JavaMailSender` missing: `SPRING_MAIL_HOST` is not loaded into the Spring Environment.
- Authentication failed: use a Google App Password and verify the account has 2-Step Verification enabled.
- `.env` changes ignored: load `.env` through Docker Compose, shell variables, or the IDE run configuration.

Verification emails link to the temporary same-origin page:

- Local: `http://localhost:8080/verify-email?token=...`
- Deployed app: `${APP_MAIL_FRONTEND_BASE_URL}/verify-email?token=...`

The click flow is: email link -> `GET /verify-email` -> static page JavaScript -> `POST /auth/verify-email` -> success/error message. No second manual verification action is required. The page removes the token from the visible address bar after reading it and does not store it in browser storage.

Password-reset emails continue to use `${APP_MAIL_FRONTEND_BASE_URL}/reset-password?token=...`. Verification emails use the same base URL with `/verify-email`.

The temporary verification page is served from `src/main/resources/static` by the Spring Boot API. Replace it later by changing `app.mail.verification-base-url` and/or the email template once the web or mobile team provides the final UI. The raw one-time token is sent only in the email link; the database stores only its hash.

For Render Web Services:

- Set `SPRING_PROFILES_ACTIVE=prod`.
- Render supplies `PORT`; the app listens on that port and binds to `0.0.0.0`.
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

Flyway migrations are consolidated into a fresh-database baseline:

- `V1__Create_Tables.sql`: final table structure, data types, constraints, relationships, account/security tables, review fields, notification recipients, and avatar metadata.
- `V2__indexes_triggers_functions.sql`: PostgreSQL extensions, Arabic normalization/search functions, triggers, full-text/trigram indexes, functional indexes, session/security indexes, and pagination indexes.
- `V3__seed_core_hadith_data.sql`: seed/reference data only.

Databases previously initialized with the old V1-V7 history must be recreated before using this consolidated baseline. Do not run `flyway repair`, add placeholder migrations, or configure Flyway to ignore missing migrations for this change.
