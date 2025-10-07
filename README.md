# Slyther — Full Project Documentation (Spring Boot 3, JWT)

> This file can be used as the project's `README.md`. It includes an overview, quick start, configuration, database setup, module walkthrough, database audit logging, and API usage examples.

## Table of Contents
- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Stack & Features](#stack--features)
- [Project Structure](#project-structure)
- [Configuration (Profiles & Properties)](#configuration-profiles--properties)
    - [JWT Configuration](#jwt-configuration)
    - [Rate Limiting (Login & Forgot Password)](#rate-limiting-login--forgot-password)
    - [CORS](#cors)
    - [Swagger / OpenAPI](#swagger--openapi)
- [Database Setup](#database-setup)
    - [MariaDB / MySQL](#mariadb--mysql)
    - [PostgreSQL](#postgresql)
    - [Automatic/Manual Migrations](#automaticmanual-migrations)
- [Audit Logs (Persisted in DB)](#audit-logs-persisted-in-db)
- [Running the Project](#running-the-project)
    - [Run in Development](#run-in-development)
    - [Build JAR & Run in Production](#build-jar--run-in-production)
- [Data Seeding](#data-seeding)
- [Auth & Test APIs](#auth--test-apis)
    - [Register / Login / Refresh / Logout](#register--login--refresh--logout)
    - [Forgot / Reset Password](#forgot--reset-password)
    - [Examples for Protected Endpoints](#examples-for-protected-endpoints)
- [How to Add a New Module (Entity/Repository/Controller) + Logs + Auth](#how-to-add-a-new-module-entityrepositorycontroller--logs--auth)
- [Production Notes](#production-notes)
- [Troubleshooting](#troubleshooting)

---

## Overview
This project is a Spring Boot 3 backend with JWT authentication (Access/Refresh) featuring:
- Users & roles (`ROLE_USER`, `ROLE_ADMIN`, `ROLE_SUDO`)
- Short-lived access tokens + rotating refresh tokens (refresh tokens are **hashed** in DB)
- API request auditing persisted to the **`action_logs`** table
- Rate limiting for sensitive endpoints (login / forgot-password)
- CORS config, Swagger in development, and Actuator (`health`, `info`)

## Prerequisites
- **JDK 17**
- **Gradle Wrapper** (included: `./gradlew`)
- A database: **MariaDB/MySQL** or **PostgreSQL**
- (Optional) **Docker** to spin up a DB quickly

## Stack & Features
- Spring Boot 3.5.x (Web, Security, Data JPA, Validation, Actuator)
- JWT using `io.jsonwebtoken` (JJWT)
- Springdoc OpenAPI for Swagger UI (enabled in the `dev` profile)
- Lombok to reduce boilerplate
- MariaDB/MySQL and PostgreSQL drivers
- `V1__init.sql` migration (Flyway-compatible)

> Tip: If you add the `flyway-core` dependency, migrations in `src/main/resources/db/migration` run automatically. Otherwise, run the SQL manually once.

## Project Structure
```
src/
 ├─ main/
 │   ├─ java/ir/momeni/slyther/
 │   │   ├─ SlytherApplication.java         ← App entrypoint
 │   │   ├─ config/
 │   │   │   ├─ SecurityConfig.java         ← Security chain, filters, public paths
 │   │   │   ├─ WebConfig.java              ← CORS + AuditInterceptor
 │   │   │   ├─ CryptoConfig.java           ← PasswordEncoder (BCrypt 12)
 │   │   │   ├─ OpenApiConfig.java          ← Swagger/OpenAPI
 │   │   │   ├─ AppProperties.java          ← Binds `app.*` properties
 │   │   │   └─ DataSeeder.java             ← Initial roles/users
 │   │   ├─ security/
 │   │   │   ├─ JwtService.java             ← Create/parse JWT (HS256)
 │   │   │   ├─ JwtAuthFilter.java          ← Extract Bearer, set SecurityContext
 │   │   │   └─ RateLimitFilter.java        ← Limit `/api/auth/login` & `/api/auth/forgot-password`
 │   │   ├─ auth/
 │   │   │   ├─ controller/AuthController.java
 │   │   │   ├─ service/AuthService.java
 │   │   │   ├─ service/PasswordResetService.java
 │   │   │   └─ dto/*.java
 │   │   ├─ user/ (entity + repository)
 │   │   ├─ role/ (entity + repository)
 │   │   ├─ session/ (entity + repository + service)
 │   │   ├─ audit/ (entity + repository + web)
 │   │   ├─ common/ (BaseEntity, ApiError, ...)
 │   │   └─ testapi/TestController.java     ← Role-based test endpoints
 │   └─ resources/
 │       ├─ application.yml                 ← Base config (default profile: prod)
 │       ├─ application-dev.yml             ← Dev profile + Swagger
 │       ├─ application-mysql.yml           ← MariaDB/MySQL datasource
 │       ├─ application-postgres.yml        ← PostgreSQL datasource
 │       └─ db/migration/V1__init.sql       ← Initial DDL
 └─ test/
```

## Configuration (Profiles & Properties)
- Use `spring.profiles.active`. Recommended for dev: **`dev,mysql`** or **`dev,postgres`**.
- In `application.yml`, Swagger and verbose error messages are disabled for production.
- In `application-dev.yml`, Swagger is enabled and logging is more verbose.

### JWT Configuration
`AppProperties` maps the following keys under `app.security.jwt.*`:
- `app.security.jwt.issuer` (required)
- `app.security.jwt.secret` (required, strong random string)
- `app.security.jwt.accessExpMins` (access token lifetime in minutes)
- `app.security.jwt.refreshExpDays` (refresh token lifetime in days)

Example (or set via ENV):
```yaml
app:
  security:
    jwt:
      issuer: slyther
      secret: "REPLACE_WITH_A_LONG_RANDOM_SECRET"
      accessExpMins: 30
      refreshExpDays: 30
```
ENV equivalents (relaxed binding):
```
APP_SECURITY_JWT_ISSUER=slyther
APP_SECURITY_JWT_SECRET=YOUR_LONG_RANDOM_SECRET
APP_SECURITY_JWT_ACCESSEXPMINS=30
APP_SECURITY_JWT_REFRESHEXPDAYS=30
```

### Rate Limiting (Login & Forgot Password)
```yaml
app:
  security:
    ratelimit:
      login:
        maxRequests: 5
        windowSeconds: 60
      forgotPassword:
        maxRequests: 5
        windowSeconds: 60
```
Applied to:
- `POST /api/auth/login`
- `POST /api/auth/forgot-password`

### CORS
```yaml
app:
  cors:
    allowedOrigins:
      - http://localhost:3000
      - http://127.0.0.1:3000
    allowedMethods: [GET, POST, PUT, DELETE, PATCH, OPTIONS]
```

### Swagger / OpenAPI
- Enabled only in **dev**.
- UI path: **`/swagger-ui.html`**.

## Database Setup
### MariaDB / MySQL
**Docker:**
```bash
docker run -d --name slyther-mysql -p 3306:3306 \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=slyther_db \
  -e MARIADB_USER=slyther_app \
  -e MARIADB_PASSWORD=seftzanet \
  mariadb:11
```
**SQL (no Docker):**
```sql
CREATE DATABASE slyther_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'slyther_app'@'%' IDENTIFIED BY 'seftzanet';
GRANT ALL PRIVILEGES ON slyther_db.* TO 'slyther_app'@'%';
FLUSH PRIVILEGES;
```

### PostgreSQL
**Docker:**
```bash
docker run -d --name slyther-pg -p 5432:5432 \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=slyther_db \
  -e POSTGRES_USER=slyther_app \
  postgres:16
```
**SQL (no Docker):**
```sql
CREATE USER slyther_app WITH PASSWORD 'seftzanet';
CREATE DATABASE slyther_db OWNER slyther_app;
GRANT ALL PRIVILEGES ON DATABASE slyther_db TO slyther_app;
```

### Automatic/Manual Migrations
- `src/main/resources/db/migration/V1__init.sql` creates: `roles`, `users`, `user_roles`, `sessions`, `action_logs`, `password_reset_tokens`, ...
- With Flyway it runs automatically on startup; otherwise execute manually once.

## Audit Logs (Persisted in DB)
- Implemented by `AuditInterceptor` wired in `WebConfig` for `"/api/**"`.
- Stored in **`action_logs`** (excerpt from DDL):
```sql
CREATE TABLE IF NOT EXISTS action_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  createdAt DATETIME(6) NULL,
  updatedAt DATETIME(6) NULL,
  username VARCHAR(150) NULL,
  method VARCHAR(16) NOT NULL,
  path VARCHAR(255) NOT NULL,
  ip VARCHAR(46) NULL,
  userAgent VARCHAR(255) NULL,
  status INT NOT NULL,
  success TINYINT(1) NOT NULL,
  errorMessage VARCHAR(512) NULL,
  INDEX idx_log_path (path),
  INDEX idx_log_username (username)
);
```
- Sensitive values are masked (e.g., `password=***`, `Authorization: Bearer ***`).

## Running the Project
### Run in Development
```bash
export SPRING_PROFILES_ACTIVE=dev,mysql
export APP_SECURITY_JWT_ISSUER=slyther
export APP_SECURITY_JWT_SECRET=$(openssl rand -hex 64)
export APP_SECURITY_JWT_ACCESSEXPMINS=30
export APP_SECURITY_JWT_REFRESHEXPDAYS=30

./gradlew bootRun
```
PowerShell:
```powershell
$env:SPRING_PROFILES_ACTIVE="dev,mysql"
$env:APP_SECURITY_JWT_ISSUER="slyther"
$env:APP_SECURITY_JWT_SECRET="REPLACE_WITH_A_LONG_RANDOM_SECRET"
$env:APP_SECURITY_JWT_ACCESSEXPMINS="30"
$env:APP_SECURITY_JWT_REFRESHEXPDAYS="30"

./gradlew bootRun
```

### Build JAR & Run in Production
```bash
./gradlew clean bootJar
java -jar build/libs/slyther-0.0.2.jar \
  --spring.profiles.active=prod,mysql \
  --server.port=8080
```
Set `APP_SECURITY_*` env vars appropriately in production.

## Data Seeding
If DB is empty, `DataSeeder` creates:
- `user` / **`123456`** / `ROLE_USER`
- `admin` / **`123456`** / `ROLE_ADMIN`
- `sudo` / **`123456`** / `ROLE_USER`, `ROLE_ADMIN`, `ROLE_SUDO`

> Change these passwords and disable the seeder in production.

## Auth & Test APIs
Base URL: `http://localhost:8080`

### Register / Login / Refresh / Logout
- **POST `/api/auth/register`**
  ```json
  { "username": "alice", "password": "P@ssw0rd" }
  ```
- **POST `/api/auth/login`**
  ```json
  { "username": "alice", "password": "P@ssw0rd" }
  ```
  Response:
  ```json
  {
    "tokenType": "Bearer",
    "accessToken": "...",
    "refreshToken": "...",
    "expiresInSeconds": 1800
  }
  ```
- **POST `/api/auth/refresh`**
  ```json
  { "refreshToken": "<your_refresh_token>" }
  ```
- **POST `/api/auth/logout`**
  ```json
  { "refreshToken": "<your_refresh_token>" }
  ```

### Forgot / Reset Password
- **POST `/api/auth/forgot-password`**
  ```json
  { "username": "alice" }
  ```
  (In dev you get `resetToken_for_dev_only` for testing; in real life send via email/SMS.)
- **POST `/api/auth/reset-password`**
  ```json
  { "token": "<token-from-forgot>", "newPassword": "NewP@ss1" }
  ```

### Examples for Protected Endpoints
- Public:
    - **GET `/api/test/public`** → no token
- Requires `ROLE_USER`:
    - **GET `/api/test/user`** (header `Authorization: Bearer <accessToken>`)
- Requires `ROLE_ADMIN`:
    - **GET `/api/test/admin`**
- Requires `ROLE_SUDO`:
    - **GET `/api/test/sudo`**

`curl`:
```bash
ACCESS=...
curl -H "Authorization: Bearer $ACCESS" http://localhost:8080/api/test/user
```

## How to Add a New Module (Entity/Repository/Controller) + Logs + Auth
Add a simple domain (e.g., **Notes**) that requires login and writes audit logs.

### 1) Entity
```java
// src/main/java/ir/momeni/slyther/note/entity/Note.java
package ir.momeni.slyther.note.entity;

import ir.momeni.slyther.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "notes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Note extends BaseEntity {
    @Column(nullable = false, length = 180)
    private String title;

    @Column(length = 2000)
    private String body;
}
```

### 2) Repository
```java
// src/main/java/ir/momeni/slyther/note/repository/NoteRepository.java
package ir.momeni.slyther.note.repository;

import ir.momeni.slyther.note.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> { }
```

### 3) Migration (Flyway) or Manual SQL
```sql
-- MySQL / MariaDB
CREATE TABLE IF NOT EXISTS notes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  createdAt DATETIME(6) NULL,
  updatedAt DATETIME(6) NULL,
  title VARCHAR(180) NOT NULL,
  body TEXT NULL
) ENGINE=InnoDB;
```
```sql
-- PostgreSQL
CREATE TABLE IF NOT EXISTS notes (
  id BIGSERIAL PRIMARY KEY,
  "createdAt" TIMESTAMP(6) NULL,
  "updatedAt" TIMESTAMP(6) NULL,
  title VARCHAR(180) NOT NULL,
  body TEXT NULL
);
```

### 4) DTO
```java
// src/main/java/ir/momeni/slyther/note/dto/CreateNoteDto.java
package ir.momeni.slyther.note.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateNoteDto(@NotBlank String title, String body) { }
```

### 5) Controller — Requires Login + Automatic & Manual Logging
- `@PreAuthorize("isAuthenticated()")` forces authentication (unauthenticated → **401**).
- Automatic **request** logs come from `AuditInterceptor` for all `/api/**` routes.
- Manual **business** logs can be persisted via `ActionLogService`.

```java
// src/main/java/ir/momeni/slyther/note/controller/NoteController.java
package ir.momeni.slyther.note.controller;

import ir.momeni.slyther.audit.entity.ActionLog;
import ir.momeni.slyther.audit.service.ActionLogService;
import ir.momeni.slyther.note.dto.CreateNoteDto;
import ir.momeni.slyther.note.entity.Note;
import ir.momeni.slyther.note.repository.NoteRepository;
import ir.momeni.slyther.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {
    private final NoteRepository repo;
    private final ActionLogService logService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public Note create(@RequestBody @Valid CreateNoteDto dto,
                       HttpServletRequest req,
                       Authentication authentication,
                       @AuthenticationPrincipal User currentUser) {
        var note = repo.save(Note.builder().title(dto.title()).body(dto.body()).build());

        // Optional business log
        String username = authentication != null ? authentication.getName() : null;
        logService.save(ActionLog.builder()
                .username(username)
                .method("POST")
                .path(req.getRequestURI())
                .ip(req.getHeader("X-Forwarded-For") != null ? req.getHeader("X-Forwarded-For").split(",")[0].trim() : req.getRemoteAddr())
                .userAgent(req.getHeader("User-Agent"))
                .status(201)
                .success(true)
                .build());

        return note;
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public List<Note> list() { return repo.findAll(); }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public String me(@AuthenticationPrincipal User me) { return me != null ? me.getUsername() : "anonymous"; }
}
```

### 6) Security & Errors
- Missing/invalid tokens → **401** (set by security filters).
- Authenticated but insufficient role → **403**.
- Errors are returned as JSON (`ApiError`) by `GlobalExceptionHandler`.

### 7) Quick Checklist
- [ ] Create Entity + Repository
- [ ] Add Flyway migration or run SQL manually
- [ ] Write Controller with `@PreAuthorize`
- [ ] Use `@AuthenticationPrincipal` / `Authentication` as needed
- [ ] Keep routes under `/api/...` to benefit from automatic auditing
- [ ] Optionally persist business logs via `ActionLogService`

## Production Notes
- Production disables verbose error output and Swagger.
- Use **HTTPS** and a long random JWT secret; choose sensible token lifetimes.
- If behind a reverse proxy, `X-Forwarded-*` and `X-Real-IP` are considered by the rate-limit filter.
- BCrypt strength is 12; adjust per hardware.
- DB indexes exist for token hashes and main relations.
- Actuator exposes `/actuator/health` and `/actuator/info`.

## Troubleshooting
- **DB unreachable / driver missing:** ensure the right profile (`dev,mysql`|`dev,postgres`) and your DB container/service is running.
- **JWT not working:** set `APP_SECURITY_JWT_SECRET` and `APP_SECURITY_JWT_ISSUER`.
- **Swagger not visible:** enable the `dev` profile and visit `/swagger-ui.html`.
- **429 on login/forgot:** rate limit reached; increase values under `app.security.ratelimit`.
