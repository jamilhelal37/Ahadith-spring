# Ahadith API

Spring Boot API for browsing and managing Hadith content, authentication, user profile features, scholar workflows, and admin operations.

## Tech Stack

- Java 21
- Spring Boot 4.1.0
- Spring Security with JWT
- Spring Data JPA / Hibernate
- PostgreSQL 16
- Flyway
- Maven Wrapper
- Cloudinary integration for profile images
- Resend email integration
- Docker / Docker Compose

## Requirements

- Java 21
- Docker and Docker Compose for full `verify` and local containers
- PostgreSQL 16 if running without Docker

## Environment Variables

Use `.env.example` as the local template. Do not commit real `.env` files, production secrets, API keys, or tokens.

Required production variables include:

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
APP_MAIL_PROVIDER
RESEND_API_KEY
APP_MAIL_FROM
APP_MAIL_FRONTEND_BASE_URL
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
APP_CORS_ALLOWED_ORIGINS
APP_CORS_ALLOW_CREDENTIALS
APP_CORS_MAX_AGE
APP_SECURITY_TRUSTED_PROXY_HEADERS
```

Generate a strong local JWT secret with at least 64 random characters:

```bash
openssl rand -base64 64
```

## Run Locally

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Tests

Fast tests:

```bash
./mvnw test
```

Full suite with PostgreSQL Testcontainers:

```bash
./mvnw verify
```

Docker must be running for integration tests named `*IT.java`; they are not skipped automatically when Docker is unavailable.

## Build

```bash
./mvnw clean package
docker build -t ahadith:local .
```

## API v1

The canonical API base path is `/api/v1`. Legacy aliases remain temporarily only where controllers still expose them.

| Legacy alias | Canonical endpoint | Status |
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
| `/me/search-history/**` | `/api/v1/me/search-history/**` | Deprecated alias |
| `/scholar/**` | `/api/v1/scholar/**` | Deprecated alias |
| `/admin/**` | `/api/v1/admin/**` | Deprecated alias |

Removed search-engine aliases:

```text
GET /search
GET /me/search
GET /api/v1/search
GET /api/v1/me/search
```

## Public Catalog Examples

Public rawi and muhaddith list items contain only `serialNumber`, `name`, and `about`:

```json
{
  "serialNumber": 1,
  "name": "الإمام البخاري",
  "about": "نبذة عن المحدث"
}
```

Books use nested references:

```json
{
  "id": "33333333-3333-3333-3333-333333333333",
  "name": "صحيح البخاري",
  "muhaddith": {
    "id": "11111111-1111-1111-1111-111111111111",
    "name": "الإمام البخاري"
  }
}
```

## Hadith Search

Modern search:

```http
POST /api/v1/ahadith/search
```

```json
{
  "query": "النية",
  "mode": "FLEXIBLE",
  "includeExplanation": true,
  "bookIds": ["33333333-3333-3333-3333-333333333333"],
  "page": 0,
  "size": 20,
  "sort": "RELEVANCE"
}
```

Search runs in PostgreSQL. Authenticated users get one compatible `search_history` entry; anonymous users do not create history.

History endpoints:

```http
GET    /api/v1/me/search-history
GET    /api/v1/me/search-history/search?keyword=...
DELETE /api/v1/me/search-history
DELETE /api/v1/me/search-history/{id}
```

## Admin APIs

Create responses use canonical `Location` headers under `/api/v1/admin/**`.

Books support:

```http
DELETE /api/v1/admin/books/{id}
```

Successful deletion returns `204`; a missing book returns `404`.

Hadith partial update:

```http
PATCH /api/v1/admin/ahadith/{id}
```

Relationship fields remain nested reference objects:

```json
{
  "book": { "id": "00000000-0000-0000-0000-000000000000" },
  "rawi": { "id": "00000000-0000-0000-0000-000000000000" },
  "ruling": { "id": "00000000-0000-0000-0000-000000000000" },
  "explaining": null,
  "subValid": null
}
```

For `PATCH`, an omitted field is unchanged, a present `null` clears a nullable relationship, and a present `{ "id": "..." }` links the referenced row.

## Security Notes

- Roles are `MEMBER`, `SCHOLAR`, and `ADMIN`, mapped to Spring authorities.
- `/api/v1/admin/**` requires admin.
- `/api/v1/scholar/**` requires scholar or admin.
- Public GET catalog endpoints and `POST /api/v1/ahadith/search` are public.
- Access tokens are JWTs with a `tokenVersion` claim.
- Successful password reset increments `users.token_version`, revokes refresh sessions, consumes reset tokens, and writes an activity log.
- Refresh tokens are rotated and stored only as hashes in `refresh_token_sessions`.
- JWT errors use the shared error response with `status`, `error`, `message`, `path`, `timestamp`, and `requestId`.

## Render, CORS, And Proxy

Do not put production values or secrets in the repository. Configure these names in Render without documenting their real values:

```text
APP_CORS_ALLOWED_ORIGINS=<comma-separated production origins>
APP_CORS_ALLOW_CREDENTIALS=false
APP_CORS_MAX_AGE=1h
APP_SECURITY_TRUSTED_PROXY_HEADERS=true
```

`X-Forwarded-For` is used only when `APP_SECURITY_TRUSTED_PROXY_HEADERS=true`; otherwise the application uses `remoteAddr`. When the prod profile starts with proxy headers disabled, the app logs a warning and continues.

## Email

Email is sent through Resend. Keep `RESEND_API_KEY` secret. Password-reset emails use `${APP_MAIL_FRONTEND_BASE_URL}/reset-password?token=...`. The database stores only token hashes.

## Actuator

`/actuator/health` and `/actuator/health/**` are public. `/actuator/info`, `/actuator/metrics/**`, and `/actuator/prometheus` require admin.

## Migrations

- `V1__Create_Tables.sql`
- `V2__indexes_triggers_functions.sql`
- `V3__seed_core_hadith_data.sql`
- `V4__create_activity_log.sql`
- `V5__add_user_token_version.sql`

Do not edit already-applied migrations. New database changes must use a later migration.

## OpenAPI

OpenAPI exposes API v1 paths only. Public endpoints are not marked with bearer security; `/api/v1/me/**`, `/api/v1/scholar/**`, and `/api/v1/admin/**` are marked with `bearer-jwt`. JPA entities, `password`, and `searchVector` are not intended as API schemas.
