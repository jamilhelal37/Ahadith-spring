# Ahadith API

Spring Boot API for browsing and managing Hadith content, authentication, user profile features, scholar workflows, and admin operations.

## Tech Stack

- Java 21
- Spring Boot 4.1.0
- Spring Security with JWT
- Spring Data JPA / Hibernate
- PostgreSQL 16 with pgvector
- Flyway
- Maven Wrapper
- Cloudinary integration for profile images and authenticated upgrade PDFs
- Resend email integration
- Docker / Docker Compose

## Requirements

- Java 21
- Docker and Docker Compose for the full `verify` suite and the optional local database
- A PostgreSQL 16 database with the `vector` extension (Neon supports pgvector) for the normal development workflow

## Environment Variables

Use `.env.example` as the local template and put the Neon JDBC URL and credentials in `.env`. The JDBC URL should enable TLS with `sslmode=require`. Do not commit real `.env` files, production secrets, API keys, or tokens.

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
APP_GOOGLE_AUTH_ENABLED
GOOGLE_AUTH_CLIENT_IDS
APP_UPGRADE_DOCUMENT_MAX_SIZE
APP_UPGRADE_DOCUMENT_MAX_PAGES
APP_UPGRADE_DOCUMENT_DOWNLOAD_TTL
APP_RATE_LIMIT_UPGRADE_REQUEST_CREATE_CAPACITY
APP_RATE_LIMIT_UPGRADE_REQUEST_CREATE_WINDOW
```

Generate a strong local JWT secret with at least 64 random characters:

```bash
openssl rand -base64 64
```

## Run Locally

The normal local workflow connects to the Neon datasource configured in `.env`; it does not start Docker PostgreSQL:

```bash
./mvnw spring-boot:run
```

`./start-local.sh` performs the same Neon-first startup with configuration checks and automatic local port selection. It never replaces the datasource from `.env`.

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

`start-neon.ps1` is also available on Windows and uses the same `SPRING_DATASOURCE_*` variables from `.env` without starting Docker.

To explicitly start the optional local PostgreSQL service for development, run:

```bash
docker compose up -d postgres
```

The Compose `app` service is an explicit all-local stack and connects to that PostgreSQL service. It is separate from the normal Neon workflow.

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

## Authentication

Email/password login continues to use:

```http
POST /api/v1/auth/login
```

Google login is available through:

```http
POST /api/v1/auth/google
```

Request:

```json
{
  "idToken": "GOOGLE_ID_TOKEN"
}
```

The client sends only a Google ID Token. The backend verifies the token with Google, links or creates the local user, and returns the same project JWT response shape used by email/password login:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {}
}
```

Configure Google login with:

```text
APP_GOOGLE_AUTH_ENABLED=false
GOOGLE_AUTH_CLIENT_IDS=
```

`GOOGLE_AUTH_CLIENT_IDS` is a comma-separated allowlist of accepted Google OAuth client IDs, for example web and Android client IDs. Use the Google Web Client ID as the primary audience. No client secret is required or used for direct ID Token verification.

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

Search supports four compatible modes:

- `EXACT`: existing normalized phrase matching; explanation text is optional through `includeExplanation`.
- `FLEXIBLE`: existing PostgreSQL Arabic full-text search.
- `SEMANTIC`: BGE-M3 dense embeddings and pgvector cosine similarity.
- `HYBRID`: `FLEXIBLE` and `SEMANTIC` candidate rankings combined with Reciprocal Rank Fusion (RRF).

All existing filters are applied before candidate limits in both text and vector searches. Authenticated users get one compatible `search_history` entry; anonymous users do not create history. `HYBRID` automatically falls back to `FLEXIBLE` if the embedding HTTP service is unavailable. `SEMANTIC` instead returns the normal API `503 Service Unavailable` error response.

`ahadith.search_vector` is PostgreSQL's `tsvector` used by `FLEXIBLE`. `hadith_embeddings.embedding` is the separate 1024-dimensional AI vector used by semantic search. They are intentionally different and neither is exposed by the public API.

### Semantic search setup

This repository contains only the Spring Boot and database side of semantic search. It retains the pgvector schema, vector persistence and search SQL, filtering, RRF, reindex/backfill, and after-commit embedding generation. It does not build or run BGE-M3.

Deployment architecture:

```text
Frontend (React / Flutter)
  -> Spring Boot
       -> PostgreSQL / pgvector
       -> HTTPS -> external BGE-M3 HTTP service
```

The external HTTP service must expose `GET /health` and `POST /embed`. Spring sends:

```json
{
  "texts": ["أحاديث عن الرحمة بالحيوان"]
}
```

The `POST /embed` response contract is:

```json
{
  "model": "BAAI/bge-m3",
  "modelVersion": "...",
  "dimension": 1024,
  "embeddings": [[...]]
}
```

Each item in `embeddings` must be a 1024-dimensional vector, in the same order as the request's `texts`. Configure the external URL before enabling semantic search. For example, a GitHub Codespaces deployment can be supplied to Spring or Render as:

```text
APP_EMBEDDING_SERVICE_URL=https://<codespace>-8001.app.github.dev
```

Do not commit a live Codespaces URL. Render should provide it as an environment variable. A normal non-Docker Spring process defaults to `http://localhost:8001`. Docker Compose deliberately requires `APP_EMBEDDING_SERVICE_URL` in `.env`, because `localhost` inside the app container is not the Docker host on Linux. Use an external HTTPS URL or another explicitly reachable address.

To run PostgreSQL/pgvector and the Spring API locally, copy `.env.example` to `.env`, fill the existing required settings and the external embedding URL, then run:

```bash
docker compose up --build
```

After Flyway has created `hadith_embeddings`, perform the initial idempotent backfill with an admin JWT:

```bash
curl -X POST 'http://localhost:8080/api/v1/admin/hadith-embeddings/reindex?force=false' \
  -H 'Authorization: Bearer ADMIN_JWT'
```

Status is available from `GET /api/v1/admin/hadith-embeddings/status`. Use `force=true` to regenerate every vector. Creation and text-only updates schedule embedding generation after the database transaction commits; failed work remains missing/stale for the next backfill.

Semantic configuration:

```text
APP_SEMANTIC_SEARCH_ENABLED=true
APP_EMBEDDING_SERVICE_URL=http://localhost:8001
APP_EMBEDDING_MODEL=BAAI/bge-m3
APP_EMBEDDING_MODEL_VERSION=1.3.5
APP_EMBEDDING_BATCH_SIZE=16
APP_SEMANTIC_MIN_SIMILARITY=0.45
APP_SEMANTIC_CANDIDATE_LIMIT=100
APP_SEMANTIC_MAX_CANDIDATE_LIMIT=1000
APP_HYBRID_RRF_K=60
APP_EMBEDDING_CONNECT_TIMEOUT=10s
APP_EMBEDDING_READ_TIMEOUT=120s
```

`APP_SEMANTIC_CANDIDATE_LIMIT` preserves the first-page ranking pool. Later pages request enough ranked candidates for `(page + 1) * size`, bounded by `APP_SEMANTIC_MAX_CANDIDATE_LIMIT` so retrieval is never unbounded. For `SEMANTIC` and `HYBRID`, pagination is applied after vector ranking/RRF and `totalItems`/`totalPages` describe this bounded ranked candidate pool, not an exact count of every matching database row.

Example hybrid request:

```bash
curl -X POST http://localhost:8080/api/v1/ahadith/search \
  -H 'Content-Type: application/json' \
  -d '{"query":"أحاديث عن الرحمة بالحيوان","mode":"HYBRID","bookIds":[],"rawiIds":[],"rulingIds":[],"topicIds":[],"page":0,"size":20,"sort":"RELEVANCE"}'
```

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

Hadith update:

```http
PUT /api/v1/admin/ahadith/{id}
```

Relationship fields remain nested reference objects:

```json
{
  "book": { "id": "00000000-0000-0000-0000-000000000000" },
  "rawi": { "id": "00000000-0000-0000-0000-000000000000" },
  "ruling": { "id": "00000000-0000-0000-0000-000000000000" },
  "explaining": { "id": "00000000-0000-0000-0000-000000000000" },
  "subValid": { "id": "00000000-0000-0000-0000-000000000000" }
}
```

`PUT /api/v1/admin/ahadith/{id}` uses update DTO semantics, not full replacement. Omitted fields and fields sent as `null` are unchanged because MapStruct ignores null properties during updates. Fields sent with valid non-null values are updated. Relationship fields are updated when the request sends a reference object with a valid `id`. There is no `PATCH` endpoint for updating a hadith.

Admin user management is available only to admins:

```http
GET /api/v1/admin/users?q=&status=&type=&page=0&size=20&sort=createdAt,desc
GET /api/v1/admin/users/{id}
PUT /api/v1/admin/users/{id}/status
PUT /api/v1/admin/users/{id}/type
```

User search supports case-insensitive name/email search, `status` filtering, `type` filtering, and `SearchResponse` pagination. The default sort is stable by `createdAt DESC` then `id`; `size` is capped at `100`. User responses do not expose `password`, `tokenVersion`, or token data.

Status update example:

```bash
curl -X PUT "$API_BASE/api/v1/admin/users/<id>/status" \
  -H "Authorization: Bearer <admin-access-token>" \
  -H "Content-Type: application/json" \
  -d '{"status":"disabled"}'
```

Only `active` and `disabled` are accepted by the status endpoint. Status changes increment `tokenVersion`, revoke all refresh sessions for the target user, and invalidate old access tokens. Admins cannot disable their own account.

Type update example:

```bash
curl -X PUT "$API_BASE/api/v1/admin/users/<id>/type" \
  -H "Authorization: Bearer <admin-access-token>" \
  -H "Content-Type: application/json" \
  -d '{"type":"scholar"}'
```

Accepted user types are `member`, `scholar`, and `admin`. Type changes increment `tokenVersion`, revoke all refresh sessions for the target user, and do not change old upgrade requests. Admins cannot change their own account type.

## Scholar Upgrade Requests

Members create upgrade requests by uploading a PDF from the backend. The client must not send `status`, `filePath`, Cloudinary public IDs, URLs, or asset IDs.

```http
POST /api/v1/me/upgrade-requests
Content-Type: multipart/form-data
```

Parts:

```text
document=@credentials.pdf;type=application/pdf
notes=optional text
```

Example:

```bash
curl -X POST "$API_BASE/api/v1/me/upgrade-requests" \
  -H "Authorization: Bearer <access-token>" \
  -F "document=@credentials.pdf;type=application/pdf" \
  -F "notes=Optional review notes"
```

Successful creation returns `201 Created`, sets `status` to `under_review`, and stores only Cloudinary document metadata.

```json
{
  "id": "00000000-0000-0000-0000-000000000000",
  "status": "under_review",
  "notes": "Optional review notes",
  "reviewNotes": null,
  "rejectionReason": null,
  "documentAvailable": true,
  "documentOriginalName": "credentials.pdf",
  "documentSizeBytes": 12345,
  "reviewedAt": null,
  "createdAt": "2026-07-22T19:00:00",
  "updatedAt": "2026-07-22T19:00:00"
}
```

Member endpoints:

```http
GET /api/v1/me/upgrade-requests
GET /api/v1/me/upgrade-requests/current
GET /api/v1/me/upgrade-requests/{id}/document
```

Admin endpoints:

```http
GET    /api/v1/admin/upgrade-requests
GET    /api/v1/admin/upgrade-requests/{id}
GET    /api/v1/admin/upgrade-requests/{id}/document
PATCH  /api/v1/admin/upgrade-requests/{id}/review
DELETE /api/v1/admin/upgrade-requests/{id}
```

Review example:

```bash
curl -X PATCH "$API_BASE/api/v1/admin/upgrade-requests/<id>/review" \
  -H "Authorization: Bearer <admin-access-token>" \
  -H "Content-Type: application/json" \
  -d '{"decision":"APPROVE","reviewNotes":"Credentials verified"}'
```

Temporary document links are returned only by the `/document` endpoints, use `Cache-Control: no-store`, and expire after `APP_UPGRADE_DOCUMENT_DOWNLOAD_TTL` (default `5m`). Do not persist these links in web or mobile clients. Normal list/detail responses never expose Cloudinary `publicId`, `assetId`, or URLs.

Document validation accepts PDF only, checks content type, extension, PDF magic bytes, parses the file with PDFBox, rejects encrypted files, caps size with `APP_UPGRADE_DOCUMENT_MAX_SIZE`, and caps pages with `APP_UPGRADE_DOCUMENT_MAX_PAGES`.

Cloudinary uploads use `resource_type=raw`, `type=authenticated`, and server-generated public IDs under `upgrade-requests/{userId}/{randomUuid}`. For PDF delivery on Cloudinary free plans, enable: Settings -> Security -> Allow delivery of PDF and ZIP files.

## Public Text Pagination

These public text list endpoints return `SearchResponse<PublicTextDto>`:

```http
GET /api/v1/explaining?page=0&size=20
GET /api/v1/fake-ahadith?page=0&size=20
```

`page` starts at `0`; `size` must be between `1` and `50`. Invalid pagination returns the shared `ErrorResponseDto` with `requestId`.

## Security Notes

- Roles are `MEMBER`, `SCHOLAR`, and `ADMIN`, mapped to Spring authorities.
- `/api/v1/admin/**` requires admin.
- `/api/v1/scholar/**` requires scholar or admin.
- Public GET catalog endpoints and `POST /api/v1/ahadith/search` are public.
- Access tokens are JWTs with a `tokenVersion` claim.
- Successful password reset increments `users.token_version`, revokes refresh sessions, consumes reset tokens, and writes an activity log.
- Refresh tokens are rotated and stored only as hashes in `refresh_token_sessions`.
- Google login accepts Google ID Tokens only, uses Google's `sub` as the linked identity, never stores Google tokens, and never returns `googleSubject`.
- JWT errors use the shared error response with `status`, `error`, `message`, `path`, `timestamp`, and `requestId`.

## Current User Account

Authenticated members, scholars, and admins can update their own profile fields:

```http
PUT /api/v1/me
```

```json
{
  "name": "User Name",
  "gender": "male",
  "birthDate": "2000-01-01"
}
```

Only `name`, `gender`, and `birthDate` are updated. `name` is trimmed before saving. Attempts to send `email`, `password`, `type`, `status`, `avatarUrl`, `avatarPublicId`, or `tokenVersion` are ignored because those fields are not part of the update DTO. Profile updates do not revoke sessions.

Authenticated users can change their password:

```http
PUT /api/v1/me/password
```

```json
{
  "currentPassword": "old-password",
  "newPassword": "new-password"
}
```

The current password must match, the new password must satisfy the configured password policy, and it must differ from the current password. A successful password change increments `tokenVersion`, revokes all refresh sessions, invalidates old access tokens, and returns:

```json
{
  "message": "Password changed successfully"
}
```

## Render, CORS, And Proxy

Do not put production values or secrets in the repository. Configure these names in Render without documenting their real values:

```text
APP_CORS_ALLOWED_ORIGINS=<comma-separated production origins>
APP_CORS_ALLOW_CREDENTIALS=false
APP_CORS_MAX_AGE=1h
APP_SECURITY_TRUSTED_PROXY_HEADERS=true
APP_API_LEGACY_SUNSET=
APP_UPGRADE_DOCUMENT_MAX_SIZE=10MB
APP_UPGRADE_DOCUMENT_MAX_PAGES=20
APP_UPGRADE_DOCUMENT_DOWNLOAD_TTL=5m
APP_RATE_LIMIT_UPGRADE_REQUEST_CREATE_CAPACITY=5
APP_RATE_LIMIT_UPGRADE_REQUEST_CREATE_WINDOW=1d
```

`X-Forwarded-For` is used only when `APP_SECURITY_TRUSTED_PROXY_HEADERS=true`; otherwise the application uses `remoteAddr`. Behind Render, the application assumes the trusted proxy cleans forwarded headers before passing requests. When the prod profile starts with proxy headers disabled, the app logs a warning and continues. Legacy aliases include `Deprecation: true`; `Sunset` is emitted only when `APP_API_LEGACY_SUNSET` is configured.

## Email

Email is sent through Resend. Keep `RESEND_API_KEY` secret. Password-reset emails use `${APP_MAIL_FRONTEND_BASE_URL}/reset-password?token=...`. Verification links can use `APP_MAIL_VERIFICATION_BASE_URL` when they need a different base URL. Configure Resend HTTP timeouts with `APP_MAIL_CONNECT_TIMEOUT` and `APP_MAIL_READ_TIMEOUT`. The database stores only token hashes.

Email verification tokens activate a user only when the current user status is `pending_confirmation`. Users already `active` or `disabled` are not activated by a verification token. Rejections use the generic `Invalid or expired verification token` message so account state is not exposed.

## Cleanup

Scheduled cleanup removes old login attempts, expired refresh sessions, and expired or consumed email/password tokens in batches. Configure it with:

```text
APP_CLEANUP_ENABLED
APP_CLEANUP_INITIAL_DELAY
APP_CLEANUP_FIXED_DELAY
APP_CLEANUP_TOKEN_RETENTION
APP_CLEANUP_LOGIN_ATTEMPT_RETENTION
APP_CLEANUP_BATCH_SIZE
```

## Actuator

`/actuator/health` and `/actuator/health/**` are public. `/actuator/info`, `/actuator/metrics/**`, and `/actuator/prometheus` require admin.

## Migrations

- `V1__Create_Tables.sql`
- `V2__indexes_triggers_functions.sql`
- `V3__seed_core_hadith_data.sql`
- `V4__create_activity_log.sql`
- `V5__add_user_token_version.sql`
- `V6__security_cleanup_indexes.sql`
- `V7__add_upgrade_request_document_metadata.sql`
- `V8__fix_upgrade_request_relation.sql`
- `V9__fix_missing_seed_rawi_relations.sql`
- `V10__add_google_identity_to_users.sql`

Seed data in `V3__seed_core_hadith_data.sql` is for development and demo use only. It is not the final production database and is not an authoritative religious reference; it is expected to be replaced or expanded later. `V9__fix_missing_seed_rawi_relations.sql` fixes missing rawi relations in older seeded records without replacing relations that were already corrected. `V10__add_google_identity_to_users.sql` adds nullable Google identity linkage and allows password to be null for Google-created users.

Do not edit already-applied migrations. New database changes must use a later migration.

## OpenAPI

OpenAPI exposes API v1 paths only. Public endpoints are not marked with bearer security; `/api/v1/me/**`, `/api/v1/scholar/**`, and `/api/v1/admin/**` are marked with `bearer-jwt`. JPA entities, `password`, and `searchVector` are not intended as API schemas.
