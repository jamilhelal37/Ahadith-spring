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
LOCAL_POSTGRES_DB
LOCAL_POSTGRES_USERNAME
LOCAL_POSTGRES_PASSWORD
JWT_SECRET
JWT_ACCESS_EXPIRATION
JWT_REFRESH_EXPIRATION
APP_MAIL_ENABLED
APP_MAIL_PROVIDER
RESEND_API_KEY
APP_MAIL_FROM
APP_MAIL_FRONTEND_BASE_URL
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
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

Docker Compose reads `.env` automatically when present. The Compose PostgreSQL service uses `LOCAL_POSTGRES_DB`, `LOCAL_POSTGRES_USERNAME`, and `LOCAL_POSTGRES_PASSWORD`, so local containers do not depend on the real `SPRING_DATASOURCE_*` values. The app service sets `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/${LOCAL_POSTGRES_DB:-ahadith}` inside the container so PostgreSQL is reached through the Compose service name.

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

The project sends email through the Resend HTTP API. SMTP is not used, which keeps production compatible with Render Free where SMTP ports are blocked.

For Render, configure:

```text
SPRING_PROFILES_ACTIVE=prod
APP_MAIL_ENABLED=true
APP_MAIL_PROVIDER=resend
RESEND_API_KEY=<secret>
APP_MAIL_FROM=no-reply@mail.jamilhelal.me
APP_MAIL_FRONTEND_BASE_URL=https://api.jamilhelal.me
```

`RESEND_API_KEY` must remain a secret. Do not commit it to Git, place it in README examples as a real value, or print it in logs.

For local development, keep `APP_MAIL_ENABLED=false` unless you intentionally want to test Resend with a private key in your untracked `.env`.

Verification emails link to the temporary same-origin page:

- Local: `http://localhost:8080/verify-email?token=...`
- Deployed app: `https://api.jamilhelal.me/verify-email?token=...`

Password-reset emails use `${APP_MAIL_FRONTEND_BASE_URL}/reset-password?token=...`.

Common failures:

- `RESEND_API_KEY` missing: set it as a Render secret environment variable.
- Sender rejected: verify `mail.jamilhelal.me` in Resend and use `APP_MAIL_FROM=no-reply@mail.jamilhelal.me`.
- `.env` changes ignored: load `.env` through Docker Compose, shell variables, or the IDE run configuration.

The click flow is: email link -> `GET /verify-email` -> static page JavaScript -> `POST /auth/verify-email` -> success/error message. No second manual verification action is required. The page removes the token from the visible address bar after reading it and does not store it in browser storage.

The temporary verification page is served from `src/main/resources/static` by the Spring Boot API. Replace it later by changing `app.mail.verification-base-url` and/or the email template once the web or mobile team provides the final UI. The raw one-time token is sent only in the email link; the database stores only its hash.

For Render Web Services:

- Set `SPRING_PROFILES_ACTIVE=prod`.
- Render supplies `PORT`; the app listens on that port and binds to `0.0.0.0`.
- Set datasource, JWT, Resend, Cloudinary, and mail sender variables in the Render Dashboard. Use `sync: false` for secrets if you manage them through infrastructure files.

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

## API v1

The canonical API base path is now `/api/v1`. Legacy routes remain available temporarily for existing Postman collections and clients, but new clients should use v1.

| Legacy endpoint | API v1 endpoint | Status |
| --- | --- | --- |
| `/auth/**` | `/api/v1/auth/**` | Deprecated alias |
| `/ahadith/**` | `/api/v1/ahadith/**` | Deprecated alias |
| `/books/**` | `/api/v1/books/**` | Deprecated alias |
| `/rawis/**` | `/api/v1/rawis/**` | Deprecated alias |
| `/rulings/**` | `/api/v1/rulings/**` | Deprecated alias |
| `/topics/**` | `/api/v1/topics/**` | Deprecated alias |
| `/muhaddiths/**` | `/api/v1/muhaddiths/**` | Deprecated alias |
| `/filterslist` | `/api/v1/search/filters` | Deprecated alias |
| `/ahadith/search/filters` | `/api/v1/search/filters` | Deprecated alias |
| `/me/**` | `/api/v1/me/**` | Deprecated alias |
| `/scholar/**` | `/api/v1/scholar/**` | Deprecated alias |
| `/admin/**` | `/api/v1/admin/**` | Deprecated alias |

`/actuator/**` is intentionally not under `/api/v1`. The browser verification page remains `GET /verify-email`; its JavaScript posts to `/api/v1/auth/verify-email`.

Example:

```bash
curl -X POST http://localhost:8080/api/v1/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com"}'
```

Postman should use `{{baseUrl}}/api/v1` for new requests and keep old collections only during the transition period.

## DTO Contracts

Admin request DTO relationships are nested reference objects, not flattened ids:

```json
{
  "book": { "id": "00000000-0000-0000-0000-000000000000" },
  "rawi": { "id": "00000000-0000-0000-0000-000000000000" },
  "ruling": { "id": "00000000-0000-0000-0000-000000000000" }
}
```

Response DTOs use nested reference DTOs such as `BookReferenceResponseDto`, `RawiReferenceResponseDto`, `MuhaddithReferenceResponseDto`, `PublicHadithSummaryResponseDto`, `PublicBookResponseDto`, `PublicRawiListItemDto`, `PublicMuhaddithListItemDto`, `HadithSearchItemDto`, `PublicHadithDetailsDto`, and `FiltersListResponseDto`. JPA entity schemas are not part of the public OpenAPI contract.

## CORS

Production must set explicit origins:

```text
APP_CORS_ALLOWED_ORIGINS=https://jamilhelal.me,https://www.jamilhelal.me
```

Allowed methods are `GET,POST,PUT,PATCH,DELETE,OPTIONS`. Allowed request headers are `Authorization,Content-Type,Accept,X-Request-Id`; exposed headers are `X-Request-Id`. Credentials are disabled by default because authentication uses Bearer tokens in the `Authorization` header.

## Rate Limits

The current implementation uses bounded in-memory Caffeine caches and is suitable for one Render instance. Use a shared Redis-backed implementation before scaling to multiple instances.

| Policy | Default |
| --- | --- |
| register | 5 requests / 15 minutes / IP |
| login | 20 requests / minute / IP |
| forgot-password | 3 requests / 15 minutes / IP and 3 requests / hour / email hash |
| resend-verification | 3 requests / 15 minutes / IP plus the existing 5 minute per-user resend delay |
| verify-email | 10 requests / 15 minutes / IP |
| reset-password | 10 requests / 15 minutes / IP |
| refresh | 30 requests / minute / IP |
| public hadith search | 60 requests / minute / IP |

Exceeded limits return `429` with the standard error response and `Retry-After`.

## Error Codes

Common API errors use the shared error response shape. Expected statuses include `401` unauthenticated, `403` forbidden, `404` not found, `409` conflict, and `429` rate limited.

## Render Variables

Add these variables for the new security and CORS behavior:

```text
APP_CORS_ALLOWED_ORIGINS=https://jamilhelal.me,https://www.jamilhelal.me
APP_CORS_ALLOW_CREDENTIALS=false
APP_CORS_MAX_AGE=1h
APP_SECURITY_TRUSTED_PROXY_HEADERS=true
APP_RATE_LIMIT_ENABLED=true
APP_RATE_LIMIT_CACHE_MAX_SIZE=10000
APP_RATE_LIMIT_CACHE_EXPIRE_AFTER_ACCESS=2h
```

Individual rate limit values can be overridden with the `APP_RATE_LIMIT_*` variables defined in `application.yml`.
