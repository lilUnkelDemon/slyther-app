# Slyther — Vollständige Projektdokumentation (Spring Boot 3, JWT)

> Diese Datei kann als `README.md` des Projekts verwendet werden. Enthält Überblick, Quick Start, Konfiguration, Datenbank-Setup, Modul-Anleitung, Audit-Logging in der Datenbank und API-Beispiele.

## Inhaltsverzeichnis
- [Überblick](#überblick)
- [Voraussetzungen](#voraussetzungen)
- [Stack & Features](#stack--features)
- [Projektstruktur](#projektstruktur)
- [Konfiguration (Profile & Properties)](#konfiguration-profile--properties)
  - [JWT-Konfiguration](#jwt-konfiguration)
  - [Rate Limiting (Login & Passwort vergessen)](#rate-limiting-login--passwort-vergessen)
  - [CORS](#cors)
  - [Swagger / OpenAPI](#swagger--openapi)
- [Datenbank-Setup](#datenbank-setup)
  - [MariaDB / MySQL](#mariadb--mysql)
  - [PostgreSQL](#postgresql)
  - [Automatische/Manuelle Migrationen](#automatischemanuelle-migrationen)
- [Audit-Logs (in DB gespeichert)](#audit-logs-in-db-gespeichert)
- [Projekt ausführen](#projekt-ausführen)
  - [Entwicklung starten](#entwicklung-starten)
  - [JAR bauen & in Produktion starten](#jar-bauen--in-produktion-starten)
- [Daten-Seeding](#daten-seeding)
- [Auth- & Test-APIs](#auth--test-apis)
  - [Register / Login / Refresh / Logout](#register--login--refresh--logout)
  - [Passwort vergessen / Zurücksetzen](#passwort-vergessen--zurücksetzen)
  - [Beispiele für geschützte Endpunkte](#beispiele-für-geschützte-endpunkte)
- [Neues Modul hinzufügen (Entity/Repository/Controller) + Logs + Auth](#neues-modul-hinzufügen-entityrepositorycontroller--logs--auth)
- [Hinweise für Produktion](#hinweise-für-produktion)
- [Fehlerbehebung](#fehlerbehebung)

---

## Überblick
Spring-Boot-3-Backend mit JWT (Access/Refresh) und folgenden Funktionen:
- Benutzer & Rollen (`ROLE_USER`, `ROLE_ADMIN`, `ROLE_SUDO`)
- Kurzlebige Access Tokens + rotierende Refresh Tokens (**gehasht** in der DB)
- API-Request-Auditing in Tabelle **`action_logs`**
- Rate Limiting für sensible Routen (Login / Passwort-vergessen)
- CORS, Swagger (nur im Dev-Profil), Actuator (`health`, `info`)

## Voraussetzungen
- **JDK 17**
- **Gradle Wrapper** (`./gradlew` ist enthalten)
- Datenbank: **MariaDB/MySQL** oder **PostgreSQL**
- (Optional) **Docker** zum schnellen Start einer DB

## Stack & Features
- Spring Boot 3.5.x (Web, Security, Data JPA, Validation, Actuator)
- JWT via `io.jsonwebtoken` (JJWT)
- Springdoc OpenAPI für Swagger UI (im Profil `dev`)
- Lombok zur Reduktion von Boilerplate
- Treiber für MariaDB/MySQL und PostgreSQL
- Migration `V1__init.sql` (kompatibel zu Flyway)

> Hinweis: Mit `flyway-core` werden Migrationen unter `src/main/resources/db/migration` automatisch ausgeführt. Andernfalls einmalig manuell ausführen.

## Projektstruktur
```
src/
 ├─ main/
 │   ├─ java/ir/momeni/slyther/
 │   │   ├─ SlytherApplication.java         ← Einstieg
 │   │   ├─ config/
 │   │   │   ├─ SecurityConfig.java         ← Security-Chain, Filter, öffentliche Pfade
 │   │   │   ├─ WebConfig.java              ← CORS + AuditInterceptor
 │   │   │   ├─ CryptoConfig.java           ← PasswordEncoder (BCrypt 12)
 │   │   │   ├─ OpenApiConfig.java          ← Swagger/OpenAPI
 │   │   │   ├─ AppProperties.java          ← Bindet `app.*` Properties
 │   │   │   └─ DataSeeder.java             ← Initiale Rollen/Benutzer
 │   │   ├─ security/
 │   │   │   ├─ JwtService.java             ← JWT erzeugen/parsen (HS256)
 │   │   │   ├─ JwtAuthFilter.java          ← Bearer lesen, SecurityContext setzen
 │   │   │   └─ RateLimitFilter.java        ← Limitiert `/api/auth/login` & `/api/auth/forgot-password`
 │   │   ├─ auth/ (Controller, Services, DTOs)
 │   │   ├─ user/ (Entity + Repository)
 │   │   ├─ role/ (Entity + Repository)
 │   │   ├─ session/ (Entity + Repository + Service)
 │   │   ├─ audit/ (Entity + Repository + Web)
 │   │   ├─ common/ (BaseEntity, ApiError, ...)
 │   │   └─ testapi/TestController.java     ← Testendpunkte nach Rollen
 │   └─ resources/
 │       ├─ application.yml                 ← Basis (Standardprofil: prod)
 │       ├─ application-dev.yml             ← Dev + Swagger
 │       ├─ application-mysql.yml           ← MariaDB/MySQL
 │       ├─ application-postgres.yml        ← PostgreSQL
 │       └─ db/migration/V1__init.sql       ← Initiale DDL
 └─ test/
```

## Konfiguration (Profile & Properties)
- `spring.profiles.active` verwenden. Empfehlung für Dev: **`dev,mysql`** oder **`dev,postgres`**.
- `application.yml`: Swagger & detaillierte Fehlerausgaben sind in Prod deaktiviert.
- `application-dev.yml`: Swagger aktiv, ausführlichere Logs.

### JWT-Konfiguration
`AppProperties` liest `app.security.jwt.*`:
- `app.security.jwt.issuer` (erforderlich)
- `app.security.jwt.secret` (erforderlich, zufälliger starker String)
- `app.security.jwt.accessExpMins` (Minuten)
- `app.security.jwt.refreshExpDays` (Tage)

Beispiel:
```yaml
app:
  security:
    jwt:
      issuer: slyther
      secret: "REPLACE_WITH_A_LONG_RANDOM_SECRET"
      accessExpMins: 30
      refreshExpDays: 30
```
ENV-Äquivalente:
```
APP_SECURITY_JWT_ISSUER=slyther
APP_SECURITY_JWT_SECRET=IHR_STARKES_GEHEIMNIS
APP_SECURITY_JWT_ACCESSEXPMINS=30
APP_SECURITY_JWT_REFRESHEXPDAYS=30
```

### Rate Limiting (Login & Passwort vergessen)
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
Gilt für:
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
- Nur im **dev**-Profil.
- UI: **`/swagger-ui.html`**.

## Datenbank-Setup
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
**SQL (ohne Docker):**
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
**SQL (ohne Docker):**
```sql
CREATE USER slyther_app WITH PASSWORD 'seftzanet';
CREATE DATABASE slyther_db OWNER slyther_app;
GRANT ALL PRIVILEGES ON DATABASE slyther_db TO slyther_app;
```

### Automatische/Manuelle Migrationen
- `V1__init.sql` erstellt: `roles`, `users`, `user_roles`, `sessions`, `action_logs`, `password_reset_tokens`, ...
- Mit Flyway automatisch beim Start; sonst einmalig manuell ausführen.

## Audit-Logs (in DB gespeichert)
- `AuditInterceptor` in `WebConfig` für alle `"/api/**"`-Routen.
- Tabelle **`action_logs`** (Auszug):
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
- Sensible Werte werden maskiert (z. B. `password=***`, `Authorization: Bearer ***`).

## Projekt ausführen
### Entwicklung starten
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

### JAR bauen & in Produktion starten
```bash
./gradlew clean bootJar
java -jar build/libs/slyther-0.0.2.jar \
  --spring.profiles.active=prod,mysql \
  --server.port=8080
```
In Produktion die `APP_SECURITY_*` Variablen unbedingt setzen.

## Daten-Seeding
Wenn leer, erstellt `DataSeeder`:
- `user` / **`123456`** / `ROLE_USER`
- `admin` / **`123456`** / `ROLE_ADMIN`
- `sudo` / **`123456`** / `ROLE_USER`, `ROLE_ADMIN`, `ROLE_SUDO`

> In Produktion Passwörter ändern und Seeding deaktivieren.

## Auth- & Test-APIs
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
  Antwort:
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

### Passwort vergessen / Zurücksetzen
- **POST `/api/auth/forgot-password`**
  ```json
  { "username": "alice" }
  ```
  (Im Dev-Profil wird `resetToken_for_dev_only` zurückgegeben; in echt per E-Mail/SMS versenden.)
- **POST `/api/auth/reset-password`**
  ```json
  { "token": "<token-from-forgot>", "newPassword": "NewP@ss1" }
  ```

### Beispiele für geschützte Endpunkte
- Öffentlich:
  - **GET `/api/test/public`** → kein Token
- Erfordert `ROLE_USER`:
  - **GET `/api/test/user`** (Header `Authorization: Bearer <accessToken>`)
- Erfordert `ROLE_ADMIN`:
  - **GET `/api/test/admin`**
- Erfordert `ROLE_SUDO`:
  - **GET `/api/test/sudo`**

`curl`:
```bash
ACCESS=...
curl -H "Authorization: Bearer $ACCESS" http://localhost:8080/api/test/user
```

## Neues Modul hinzufügen (Entity/Repository/Controller) + Logs + Auth
Einfaches Beispiel **Notes** mit Login-Pflicht und Audit-Logs.

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

import ir.momeni.slyther.note.entity(Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> { }
```

### 3) Migration (Flyway) oder SQL
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

### 5) Controller — Login-Pflicht + automatisches & manuelles Logging
- `@PreAuthorize("isAuthenticated()")` erzwingt Login (sonst **401**).
- Automatische **Request**-Logs über `AuditInterceptor` für alle `/api/**`-Routen.
- Manuelle **Business**-Logs via `ActionLogService` möglich.

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

        // Optionales Business-Log
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

### 6) Sicherheit & Fehler
- Fehlendes/ungültiges Token → **401**.
- Angemeldet aber ohne Rolle → **403**.
- Fehlerformat als JSON (`ApiError`) via `GlobalExceptionHandler`.

### 7) Checkliste
- [ ] Entity + Repository anlegen
- [ ] Flyway-Migration hinzufügen oder SQL manuell ausführen
- [ ] Controller mit `@PreAuthorize` schreiben
- [ ] `@AuthenticationPrincipal` / `Authentication` nutzen
- [ ] Routen unter `/api/...` belassen → automatisches Auditing aktiv
- [ ] Optionale Business-Logs via `ActionLogService`

## Hinweise für Produktion
- Detaillierte Fehlerausgabe und Swagger sind in Prod aus.
- **HTTPS** und starkes JWT-Secret verwenden; sinnvolle Token-Laufzeiten wählen.
- Hinter Reverse Proxy: `X-Forwarded-*`/`X-Real-IP` werden berücksichtigt.
- BCrypt-Stärke 12; je nach Hardware anpassen.
- Indizes für Token-Hashes & zentrale Relationen vorhanden.
- Actuator: `/actuator/health`, `/actuator/info`.

## Fehlerbehebung
- **DB nicht erreichbar / Treiber fehlt:** Profil prüfen (`dev,mysql`|`dev,postgres`) und DB starten.
- **JWT funktioniert nicht:** `APP_SECURITY_JWT_SECRET` & `APP_SECURITY_JWT_ISSUER` setzen.
- **Swagger nicht sichtbar:** Profil `dev` aktivieren und `/swagger-ui.html` aufrufen.
- **429 bei Login/Forgot:** Rate Limit erreicht; Werte unter `app.security.ratelimit` erhöhen.
