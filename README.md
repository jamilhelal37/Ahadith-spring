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
DB_URL=jdbc:postgresql://localhost:5432/ahadith
DB_USERNAME=postgres
DB_PASSWORD=postgres
SPRING_PROFILES_ACTIVE=dev
PORT=8080
JWT_SECRET=change-this-secret-to-at-least-64-characters-long-for-hs256-security
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=
```

Do not commit a real `.env` file or production secrets.

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
- Current user: `/me/**`
- Scholar/supervisor workflows: `/scholar/**`
- Admin operations: `/admin/**`
- Swagger/OpenAPI: `/swagger-ui/**`, `/v3/api-docs/**`

## Security Notes

- `/admin/**` requires the admin authority.
- `/scholar/**` requires supervisor/scholar access or admin authority.
- Public GET content endpoints remain open.
- `/auth/refresh` uses the `refreshToken` request body and does not require an access token.
- Use a strong `JWT_SECRET` in non-local environments.
- Production SQL logging is disabled in `application-prod.yml`.
