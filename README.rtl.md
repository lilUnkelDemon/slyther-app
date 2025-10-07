<!-- Pure HTML + dir attributes only (no CSS). Reliable RTL with LTR code blocks. -->
<div dir="rtl" lang="fa">
<p dir="rtl" align="right"><!-- RTL enabled: lines are prefixed with RLM (U+200F) to render right-to-left in GitHub/MD viewers. --></p>
<h1 dir="rtl" align="right">Slyther — مستندات کامل پروژه (Spring Boot 3, JWT)</h1>
<p dir="rtl" align="right">> این فایل به‌عنوان <span dir="ltr"><code>README.md</code></span> پروژه قابل استفاده است. شامل معرفی، راه‌اندازی سریع، پیکربندی، نحوه‌ی ساخت دیتابیس، شرح ماژول‌ها، لاگ‌برداری در دیتابیس، و مثالِ مصرف API.</p>
<h2 dir="rtl" align="right">فهرست</h2>
<ul dir="rtl">
<li dir="rtl" align="right">[معرفی](#معرفی)</li>
<li dir="rtl" align="right">[پیش‌نیازها](#پیش‌نیازها)</li>
<li dir="rtl" align="right">[استک و قابلیت‌ها](#استک-و-قابلیتها)</li>
<li dir="rtl" align="right">[ساختار پوشه‌ها و فایل‌های مهم](#ساختار-پوشهها-و-فایلهای-مهم)</li>
<li dir="rtl" align="right">[پیکربندی (Profiles & Properties)](#پیکربندی-profiles--properties)</li>
<li dir="rtl" align="right">[پیکربندی JWT](#پیکربندی-jwt)</li>
<li dir="rtl" align="right">[Rate Limiting (ورود و فراموشی رمز)](#rate-limiting-ورود-و-فراموشی-رمز)</li>
<li dir="rtl" align="right">[CORS](#cors)</li>
<li dir="rtl" align="right">[Swagger / OpenAPI](#swagger--openapi)</li>
<li dir="rtl" align="right">[راه‌اندازی پایگاه‌داده](#راهاندازی-پایگاهداده)</li>
<li dir="rtl" align="right">[MariaDB / MySQL](#mariadb--mysql)</li>
<li dir="rtl" align="right">[PostgreSQL](#postgresql)</li>
<li dir="rtl" align="right">[اجرای خودکار/دستی اسکریپت مهاجرت](#اجرای-خودکاردستی-اسکریپت-مهاجرت)</li>
<li dir="rtl" align="right">[Audit Logs (ذخیره لاگ در DB)](#audit-logs-ذخیره-لاگ-در-db)</li>
<li dir="rtl" align="right">[بالا آوردن پروژه](#بالا-آوردن-پروژه)</li>
<li dir="rtl" align="right">[Run در حالت توسعه](#run-در-حالت-توسعه)</li>
<li dir="rtl" align="right">[ساخت JAR و اجرای Production](#ساخت-jar-و-اجرای-production)</li>
<li dir="rtl" align="right">[دانه‌ریزی داده‌ها (Seed)](#دانهریزی-دادهها-seed)</li>
<li dir="rtl" align="right">[API احراز هویت و تست](#api-احراز-هویت-و-تست)</li>
<li dir="rtl" align="right">[Register / Login / Refresh / Logout](#register--login--refresh--logout)</li>
<li dir="rtl" align="right">[فراموشی و تغییر رمز](#فراموشی-و-تغییر-رمز)</li>
<li dir="rtl" align="right">[مثال درخواست به مسیرهای محافظت‌شده](#مثال-درخواست-به-مسیرهای-محافظت‌شده)</li>
<li dir="rtl" align="right">[راهنمای افزودن ماژول جدید (Entity/Repository/Controller) + لاگ + احراز هویت](#راهنمای-افزودن-ماژول-جدید-entityrepositorycontroller--لاگ--احراز-هویت)</li>
<li dir="rtl" align="right">[یادداشت‌های تولید (Production Notes)](#یادداشتهای-تولید-production-notes)</li>
<li dir="rtl" align="right">[مشکلات رایج](#مشکلات-رایج)</li>
</ul>
<p dir="rtl" align="right">---</p>
<h2 dir="rtl" align="right">معرفی</h2>
<p dir="rtl" align="right">این پروژه یک بک‌اند Spring Boot 3 با احراز هویت JWT (Access/Refresh) است که موارد زیر را فراهم می‌کند:</p>
<ul dir="rtl">
<li dir="rtl" align="right">مدیریت کاربر/نقش‌ها (<span dir="ltr"><code>ROLE_USER</code></span>, <span dir="ltr"><code>ROLE_ADMIN</code></span>, <span dir="ltr"><code>ROLE_SUDO</code></span>)</li>
<li dir="rtl" align="right">توکن دسترسی (کوتاه‌مدت) + توکن رفرش چرخشی (ذخیره‌ی هش‌شده در DB)</li>
<li dir="rtl" align="right">ثبت لاگ درخواست‌های API (Audit) در **جدول <span dir="ltr"><code>action_logs</code></span>**</li>
<li dir="rtl" align="right">محدودسازی نرخ برای مسیرهای حساس ورود و فراموشی رمز</li>
<li dir="rtl" align="right">تنظیمات CORS، Swagger در حالت توسعه، و Actuator (health/info)</li>
</ul>
<h2 dir="rtl" align="right">پیش‌نیازها</h2>
<ul dir="rtl">
<li dir="rtl" align="right">**JDK 17**</li>
<li dir="rtl" align="right">**Gradle Wrapper** (داخل ریپو موجود است: <span dir="ltr"><code>./gradlew</code></span>)</li>
<li dir="rtl" align="right">یکی از دیتابیس‌ها: **MariaDB/MySQL** یا **PostgreSQL**</li>
<li dir="rtl" align="right">(اختیاری) **Docker** برای بالا آوردن دیتابیس سریع</li>
</ul>
<h2 dir="rtl" align="right">استک و قابلیت‌ها</h2>
<ul dir="rtl">
<li dir="rtl" align="right">Spring Boot 3.5.x (Web, Security, Data JPA, Validation, Actuator)</li>
<li dir="rtl" align="right">JWT با کتابخانه <span dir="ltr"><code>io.jsonwebtoken</code></span> (JJWT)</li>
<li dir="rtl" align="right">Springdoc OpenAPI برای Swagger UI (فعال در پروفایل dev)</li>
<li dir="rtl" align="right">Lombok برای کاهش boilerplate</li>
<li dir="rtl" align="right">درایورهای MariaDB/MySQL و PostgreSQL</li>
<li dir="rtl" align="right">اسکریپت مهاجرت دیتابیس <span dir="ltr"><code>V1__init.sql</code></span> (سازگار با Flyway)</li>
</ul>
<p dir="rtl" align="right">> نکته: اگر وابستگی <span dir="ltr"><code>flyway-core</code></span> را اضافه کرده باشید، اسکریپت‌های پوشه‌ی <span dir="ltr"><code>src/main/resources/db/migration</code></span> به‌صورت خودکار اجرا می‌شوند؛ در غیر این صورت خودتان یک بار اسکریپت را روی DB اجرا کنید.</p>
<h2 dir="rtl" align="right">ساختار پوشه‌ها و فایل‌های مهم</h2>
<div dir="ltr"><pre><code>
src/
 ├─ main/
 │   ├─ java/ir/momeni/slyther/
 │   │   ├─ SlytherApplication.java         ← ورودی برنامه
 │   │   ├─ config/
 │   │   │   ├─ SecurityConfig.java         ← زنجیره امنیت، فیلترها، مسیرهای سفید
 │   │   │   ├─ WebConfig.java              ← CORS + AuditInterceptor
 │   │   │   ├─ CryptoConfig.java           ← PasswordEncoder (BCrypt 12)
 │   │   │   ├─ OpenApiConfig.java          ← تعریف Swagger/OpenAPI
 │   │   │   ├─ AppProperties.java          ← نگاشت پروپرتی‌های `app.*`
 │   │   │   └─ DataSeeder.java             ← ساخت نقش‌ها/کاربران اولیه
 │   │   ├─ security/
 │   │   │   ├─ JwtService.java             ← تولید/بررسی JWT (HS256)
 │   │   │   ├─ JwtAuthFilter.java          ← استخراج Bearer و ست کردن SecurityContext
 │   │   │   └─ RateLimitFilter.java        ← محدودسازی نرخ روی /api/auth/login و /api/auth/forgot-password
 │   │   ├─ auth/
 │   │   │   ├─ controller/AuthController.java   ← API احراز هویت
 │   │   │   ├─ service/AuthService.java         ← ورود/ثبت‌نام/رفرش/خروج
 │   │   │   ├─ service/PasswordResetService.java← فراموشی/تغییر رمز با توکن یک‌بارمصرف
 │   │   │   └─ dto/*.java                       ← مدل‌های ورودی/خروجی Auth
 │   │   ├─ user/ entity+repository              ← موجودیت User (implements UserDetails)
 │   │   ├─ role/ entity+repository              ← موجودیت Role
 │   │   ├─ session/ entity+repository+service   ← مدیریت جلسات رفرش‌توکن (hash)
 │   │   ├─ audit/ entity+repository+web         ← ActionLog + Interceptor
 │   │   ├─ common/ BaseEntity, ApiError, ...    ← کلاس‌های مشترک
 │   │   └─ testapi/TestController.java          ← مسیرهای تست نقش‌ها
 │   └─ resources/
 │       ├─ application.yml                      ← تنظیمات عمومی (پروفایل پیش‌فرض: prod)
 │       ├─ application-dev.yml                  ← تنظیمات dev + فعال‌سازی Swagger
 │       ├─ application-mysql.yml                ← تنظیمات اتصال MariaDB/MySQL
 │       ├─ application-postgres.yml             ← تنظیمات اتصال PostgreSQL
 │       └─ db/migration/V1__init.sql            ← ایجاد جداول اولیه (roles, users, ...)
 └─ test/
</code></pre></div>
<h2 dir="rtl" align="right">پیکربندی (Profiles &amp; Properties)</h2>
<ul dir="rtl">
<li dir="rtl" align="right">پروفایل‌ها از طریق <span dir="ltr"><code>spring.profiles.active</code></span> کنترل می‌شوند. پیشنهاد برای توسعه: **<span dir="ltr"><code>dev,mysql</code></span>** یا **<span dir="ltr"><code>dev,postgres</code></span>**.</li>
<li dir="rtl" align="right">در <span dir="ltr"><code>application.yml</code></span>، Swagger و پیام خطاها برای Production محدود شده‌اند.</li>
<li dir="rtl" align="right">در <span dir="ltr"><code>application-dev.yml</code></span>، Swagger روشن است و لاگ‌ها verbose هستند. (اگر پکیج تست شما <span dir="ltr"><code>ir.momeni.slyther.testapi</code></span> است و نه <span dir="ltr"><code>test.controller</code></span>، آدرس را در <span dir="ltr"><code>packages-to-scan</code></span> تصحیح کنید.)</li>
</ul>
<h3 dir="rtl" align="right">پیکربندی JWT</h3>
<p dir="rtl" align="right">کلاس <span dir="ltr"><code>AppProperties</code></span> این کلیدها را از <span dir="ltr"><code>app.security.jwt.*</code></span> می‌خواند:</p>
<ul dir="rtl">
<li dir="rtl" align="right"><span dir="ltr"><code>app.security.jwt.issuer</code></span> (ضروری)</li>
<li dir="rtl" align="right"><span dir="ltr"><code>app.security.jwt.secret</code></span> (ضروری، یک رشته قوی/تصادفی)</li>
<li dir="rtl" align="right"><span dir="ltr"><code>app.security.jwt.accessExpMins</code></span> (مدت اعتبار Access Token بر حسب دقیقه)</li>
<li dir="rtl" align="right"><span dir="ltr"><code>app.security.jwt.refreshExpDays</code></span> (مدت اعتبار Refresh Token بر حسب روز)</li>
</ul>
<p dir="rtl" align="right">نمونه‌ی تنظیم (می‌توانید در ENV هم ست کنید، نگاه به مثال پایین):</p>
<div dir="ltr"><pre><code class="language-yaml">
# application-dev.yml یا به‌صورت ENV
app:
  security:
    jwt:
      issuer: slyther
      secret: &quot;REPLACE_WITH_A_LONG_RANDOM_SECRET&quot;
      accessExpMins: 30
      refreshExpDays: 30
</code></pre></div>
<p dir="rtl" align="right">**Environment Variables معادل** (Relaxed Binding):</p>
<div dir="ltr"><pre><code>
APP_SECURITY_JWT_ISSUER=slyther
APP_SECURITY_JWT_SECRET=...یک-راز-خیلی-قوی...
APP_SECURITY_JWT_ACCESSEXPMINS=30
APP_SECURITY_JWT_REFRESHEXPDAYS=30
</code></pre></div>
<h3 dir="rtl" align="right">Rate Limiting (ورود و فراموشی رمز)</h3>
<p dir="rtl" align="right">کلیدها در <span dir="ltr"><code>app.security.ratelimit.*</code></span>:</p>
<div dir="ltr"><pre><code class="language-yaml">
app:
  security:
    ratelimit:
      login:
        maxRequests: 5
        windowSeconds: 60
      forgotPassword:
        maxRequests: 5
        windowSeconds: 60
</code></pre></div>
<p dir="rtl" align="right">در <span dir="ltr"><code>RateLimitFilter</code></span> این سیاست‌ها روی مسیرهای زیر اعمال می‌شود:</p>
<ul dir="rtl">
<li dir="rtl" align="right"><span dir="ltr"><code>POST /api/auth/login</code></span></li>
<li dir="rtl" align="right"><span dir="ltr"><code>POST /api/auth/forgot-password</code></span></li>
</ul>
<h3 dir="rtl" align="right">CORS</h3>
<p dir="rtl" align="right">در <span dir="ltr"><code>app.cors.*</code></span> مبداها و متدها را مشخص کنید. نمونه:</p>
<div dir="ltr"><pre><code class="language-yaml">
app:
  cors:
    allowedOrigins:
      - http://localhost:3000
      - http://127.0.0.1:3000
    allowedMethods: [GET, POST, PUT, DELETE, PATCH, OPTIONS]
</code></pre></div>
<h3 dir="rtl" align="right">Swagger / OpenAPI</h3>
<ul dir="rtl">
<li dir="rtl" align="right">فقط در پروفایل **dev** فعال است.</li>
<li dir="rtl" align="right">مسیر UI: **<span dir="ltr"><code>/swagger-ui.html</code></span>**</li>
<li dir="rtl" align="right">اگر پکیج‌ها تغییر کرده، <span dir="ltr"><code>packages-to-scan</code></span> را هماهنگ کنید.</li>
</ul>
<h2 dir="rtl" align="right">راه‌اندازی پایگاه‌داده</h2>
<h3 dir="rtl" align="right">MariaDB / MySQL</h3>
<p dir="rtl" align="right">**با Docker**:</p>
<div dir="ltr"><pre><code class="language-bash">
docker run -d --name slyther-mysql -p 3306:3306 \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=slyther_db \
  -e MARIADB_USER=slyther_app \
  -e MARIADB_PASSWORD=seftzanet \
  mariadb:11
</code></pre></div>
<p dir="rtl" align="right">**بدون Docker (مثال SQL):**</p>
<div dir="ltr"><pre><code class="language-sql">
CREATE DATABASE slyther_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER &#x27;slyther_app&#x27;@&#x27;%&#x27; IDENTIFIED BY &#x27;seftzanet&#x27;;
GRANT ALL PRIVILEGES ON slyther_db.* TO &#x27;slyther_app&#x27;@&#x27;%&#x27;;
FLUSH PRIVILEGES;
</code></pre></div>
<p dir="rtl" align="right">در <span dir="ltr"><code>application-mysql.yml</code></span> آدرس و یوزر/پسورد تنظیم شده است. در صورت نیاز تغییر دهید.</p>
<h3 dir="rtl" align="right">PostgreSQL</h3>
<p dir="rtl" align="right">**با Docker**:</p>
<div dir="ltr"><pre><code class="language-bash">
docker run -d --name slyther-pg -p 5432:5432 \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=slyther_db \
  -e POSTGRES_USER=slyther_app \
  postgres:16
</code></pre></div>
<p dir="rtl" align="right">**بدون Docker (مثال SQL):**</p>
<div dir="ltr"><pre><code class="language-sql">
CREATE USER slyther_app WITH PASSWORD &#x27;seftzanet&#x27;;
CREATE DATABASE slyther_db OWNER slyther_app;
GRANT ALL PRIVILEGES ON DATABASE slyther_db TO slyther_app;
</code></pre></div>
<p dir="rtl" align="right">در <span dir="ltr"><code>application-postgres.yml</code></span> URL و یوزر/پسورد را مطابق محیط خود تنظیم کنید.</p>
<h3 dir="rtl" align="right">اجرای خودکار/دستی اسکریپت مهاجرت</h3>
<ul dir="rtl">
<li dir="rtl" align="right">اسکریپت ابتدایی در <span dir="ltr"><code>src/main/resources/db/migration/V1__init.sql</code></span> وجود دارد (جداول: <span dir="ltr"><code>roles</code></span>, <span dir="ltr"><code>users</code></span>, <span dir="ltr"><code>user_roles</code></span>, <span dir="ltr"><code>sessions</code></span>, <span dir="ltr"><code>action_logs</code></span>, <span dir="ltr"><code>password_reset_tokens</code></span>, ...).</li>
<li dir="rtl" align="right">اگر **Flyway** را دارید، با بالا آمدن برنامه اجرا می‌شود. اگر نه، همان اسکریپت را یک‌بار روی دیتابیس اجرا کنید.</li>
</ul>
<h2 dir="rtl" align="right">Audit Logs (ذخیره لاگ در DB)</h2>
<ul dir="rtl">
<li dir="rtl" align="right">**چطور؟** <span dir="ltr"><code>AuditInterceptor</code></span> در <span dir="ltr"><code>WebConfig</code></span> روی مسیرهای <span dir="ltr"><code>&quot;/api/**&quot;</code></span> سوار است و بعد از هر درخواست، رکوردی می‌سازد و با <span dir="ltr"><code>ActionLogService</code></span> در DB ذخیره می‌کند.</li>
<li dir="rtl" align="right">**کجا؟** جدول **<span dir="ltr"><code>action_logs</code></span>** (در <span dir="ltr"><code>V1__init.sql</code></span>):</li>
</ul>
<div dir="ltr"><pre><code class="language-sql">
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
</code></pre></div>
<ul dir="rtl">
<li dir="rtl" align="right">**چه فیلدهایی؟** <span dir="ltr"><code>username</code></span>, <span dir="ltr"><code>method</code></span>, <span dir="ltr"><code>path</code></span>, <span dir="ltr"><code>ip</code></span>, <span dir="ltr"><code>userAgent</code></span>, <span dir="ltr"><code>status</code></span>, <span dir="ltr"><code>success</code></span>, <span dir="ltr"><code>errorMessage</code></span> (با ماسک روی موارد حساس مانند <span dir="ltr"><code>password=</code></span> و <span dir="ltr"><code>Authorization: Bearer ...</code></span>).</li>
<li dir="rtl" align="right">**غیر فعال/استثنا کردن مسیرها؟**</li>
</ul>
<div dir="ltr"><pre><code class="language-java">
  // WebConfig.java
  registry.addInterceptor(auditInterceptor)
          .addPathPatterns(&quot;/api/**&quot;)
          .excludePathPatterns(&quot;/api/health&quot;); // مثال
</code></pre></div>
<h2 dir="rtl" align="right">بالا آوردن پروژه</h2>
<h3 dir="rtl" align="right">Run در حالت توسعه</h3>
<p dir="rtl" align="right">1) پروفایل‌ها و ENV را ست کنید (مثال برای MySQL):</p>
<div dir="ltr"><pre><code class="language-bash">
export SPRING_PROFILES_ACTIVE=dev,mysql
export APP_SECURITY_JWT_ISSUER=slyther
export APP_SECURITY_JWT_SECRET=$(openssl rand -hex 64)
export APP_SECURITY_JWT_ACCESSEXPMINS=30
export APP_SECURITY_JWT_REFRESHEXPDAYS=30
</code></pre></div>
<p dir="rtl" align="right">در ویندوز (PowerShell):</p>
<div dir="ltr"><pre><code class="language-powershell">
$env:SPRING_PROFILES_ACTIVE=&quot;dev,mysql&quot;
$env:APP_SECURITY_JWT_ISSUER=&quot;slyther&quot;
$env:APP_SECURITY_JWT_SECRET=&quot;REPLACE_WITH_A_LONG_RANDOM_SECRET&quot;
$env:APP_SECURITY_JWT_ACCESSEXPMINS=&quot;30&quot;
$env:APP_SECURITY_JWT_REFRESHEXPDAYS=&quot;30&quot;
</code></pre></div>
<p dir="rtl" align="right">2) اجرا:</p>
<div dir="ltr"><pre><code class="language-bash">
./gradlew bootRun
</code></pre></div>
<h3 dir="rtl" align="right">ساخت JAR و اجرای Production</h3>
<div dir="ltr"><pre><code class="language-bash">
./gradlew clean bootJar
java -jar build/libs/slyther-0.0.2.jar \
  --spring.profiles.active=prod,mysql \
  --server.port=8080
</code></pre></div>
<p dir="rtl" align="right">ENVهای <span dir="ltr"><code>APP_SECURITY_*</code></span> را حتماً در محیط Production ست کنید (راز قوی، مدت‌های مناسب، ...).</p>
<h2 dir="rtl" align="right">دانه‌ریزی داده‌ها (Seed)</h2>
<p dir="rtl" align="right">در <span dir="ltr"><code>DataSeeder</code></span> اگر رکوردی نباشد، نقش‌ها و کاربران زیر ساخته می‌شوند:</p>
<ul dir="rtl">
<li dir="rtl" align="right">کاربر <span dir="ltr"><code>user</code></span> با رمز **<span dir="ltr"><code>123456</code></span>** و نقش <span dir="ltr"><code>ROLE_USER</code></span></li>
<li dir="rtl" align="right">کاربر <span dir="ltr"><code>admin</code></span> با رمز **<span dir="ltr"><code>123456</code></span>** و نقش <span dir="ltr"><code>ROLE_ADMIN</code></span></li>
<li dir="rtl" align="right">کاربر <span dir="ltr"><code>sudo</code></span> با رمز **<span dir="ltr"><code>123456</code></span>** و نقش‌های <span dir="ltr"><code>ROLE_USER</code></span>, <span dir="ltr"><code>ROLE_ADMIN</code></span>, <span dir="ltr"><code>ROLE_SUDO</code></span></li>
<p dir="rtl" align="right">> **حتماً** برای محیط واقعی رمزها را تغییر دهید و دانه‌ریز را حذف/غیرفعال کنید.</p>
</ul>
<h2 dir="rtl" align="right">API احراز هویت و تست</h2>
<p dir="rtl" align="right">Base URL پیش‌فرض: <span dir="ltr"><code>http://localhost:8080</code></span></p>
<h3 dir="rtl" align="right">Register / Login / Refresh / Logout</h3>
<ul dir="rtl">
<li dir="rtl" align="right">**POST <span dir="ltr"><code>/api/auth/register</code></span>**</li>
</ul>
<div dir="ltr"><pre><code class="language-json">
  { &quot;username&quot;: &quot;alice&quot;, &quot;password&quot;: &quot;P@ssw0rd&quot; }
</code></pre></div>
<ul dir="rtl">
<li dir="rtl" align="right">**POST <span dir="ltr"><code>/api/auth/login</code></span>**</li>
</ul>
<div dir="ltr"><pre><code class="language-json">
  { &quot;username&quot;: &quot;alice&quot;, &quot;password&quot;: &quot;P@ssw0rd&quot; }
</code></pre></div>
<p dir="rtl" align="right">پاسخ:</p>
<div dir="ltr"><pre><code class="language-json">
  {
    &quot;tokenType&quot;: &quot;Bearer&quot;,
    &quot;accessToken&quot;: &quot;...&quot;,
    &quot;refreshToken&quot;: &quot;...&quot;,
    &quot;expiresInSeconds&quot;: 1800
  }
</code></pre></div>
<ul dir="rtl">
<li dir="rtl" align="right">**POST <span dir="ltr"><code>/api/auth/refresh</code></span>**</li>
</ul>
<div dir="ltr"><pre><code class="language-json">
  { &quot;refreshToken&quot;: &quot;&lt;your_refresh_token&gt;&quot; }
</code></pre></div>
<ul dir="rtl">
<li dir="rtl" align="right">**POST <span dir="ltr"><code>/api/auth/logout</code></span>**</li>
</ul>
<div dir="ltr"><pre><code class="language-json">
  { &quot;refreshToken&quot;: &quot;&lt;your_refresh_token&gt;&quot; }
</code></pre></div>
<p dir="rtl" align="right">رفرش‌توکن به‌صورت **hash** در جدول <span dir="ltr"><code>sessions</code></span> نگهداری می‌شود؛ با <span dir="ltr"><code>logout</code></span> یا چرخشِ موفق، باطل می‌گردد.</p>
<h3 dir="rtl" align="right">فراموشی و تغییر رمز</h3>
<ul dir="rtl">
<li dir="rtl" align="right">**POST <span dir="ltr"><code>/api/auth/forgot-password</code></span>**</li>
</ul>
<div dir="ltr"><pre><code class="language-json">
  { &quot;username&quot;: &quot;alice&quot; }
</code></pre></div>
<p dir="rtl" align="right">(در محیط dev، توکن یک‌بارمصرف برای تست در پاسخ با کلید <span dir="ltr"><code>resetToken_for_dev_only</code></span> برمی‌گردد. در واقعی باید ایمیل/SMS ارسال شود.)</p>
<ul dir="rtl">
<li dir="rtl" align="right">**POST <span dir="ltr"><code>/api/auth/reset-password</code></span>**</li>
</ul>
<div dir="ltr"><pre><code class="language-json">
  { &quot;token&quot;: &quot;&lt;token-from-forgot&gt;&quot;, &quot;newPassword&quot;: &quot;NewP@ss1&quot; }
</code></pre></div>
<p dir="rtl" align="right">توکن‌های فراموشی رمز نیز به‌صورت **hash** در جدول <span dir="ltr"><code>password_reset_tokens</code></span> ذخیره می‌شوند و یک‌بارمصرف هستند.</p>
<h3 dir="rtl" align="right">مثال درخواست به مسیرهای محافظت‌شده</h3>
<ul dir="rtl">
<li dir="rtl" align="right">مسیر عمومی:</li>
<li dir="rtl" align="right">**GET <span dir="ltr"><code>/api/test/public</code></span>** → بدون توکن</li>
<li dir="rtl" align="right">نیازمند نقش <span dir="ltr"><code>ROLE_USER</code></span>:</li>
<li dir="rtl" align="right">**GET <span dir="ltr"><code>/api/test/user</code></span>** (Header: <span dir="ltr"><code>Authorization: Bearer &lt;accessToken&gt;</code></span>)</li>
<li dir="rtl" align="right">نیازمند نقش <span dir="ltr"><code>ROLE_ADMIN</code></span>:</li>
<li dir="rtl" align="right">**GET <span dir="ltr"><code>/api/test/admin</code></span>**</li>
<li dir="rtl" align="right">نیازمند نقش <span dir="ltr"><code>ROLE_SUDO</code></span>:</li>
<li dir="rtl" align="right">**GET <span dir="ltr"><code>/api/test/sudo</code></span>**</li>
</ul>
<p dir="rtl" align="right">نمونه با <span dir="ltr"><code>curl</code></span>:</p>
<div dir="ltr"><pre><code class="language-bash">
ACCESS=... # مقدار accessToken از لاگین
curl -H &quot;Authorization: Bearer $ACCESS&quot; http://localhost:8080/api/test/user
</code></pre></div>
<h2 dir="rtl" align="right">راهنمای افزودن ماژول جدید (Entity/Repository/Controller) + لاگ + احراز هویت</h2>
<p dir="rtl" align="right">این بخش قدم‌به‌قدم نشان می‌دهد چطور یک دامنهٔ ساده (مثلاً «یادداشت‌ها / Notes») اضافه کنید که هم **نیاز به لاگین** داشته باشد و هم **لاگ در دیتابیس** ذخیره کند.</p>
<h3 dir="rtl" align="right">1) Entity</h3>
<div dir="ltr"><pre><code class="language-java">
// src/main/java/ir/momeni/slyther/note/entity/Note.java
package ir.momeni.slyther.note.entity;

import ir.momeni.slyther.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = &quot;notes&quot;)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Note extends BaseEntity {
@Column(nullable = false, length = 180)
private String title;

    @Column(length = 2000)
    private String body;
}
</code></pre></div>
<h3 dir="rtl" align="right">2) Repository</h3>
<div dir="ltr"><pre><code class="language-java">
// src/main/java/ir/momeni/slyther/note/repository/NoteRepository.java
package ir.momeni.slyther.note.repository;

import ir.momeni.slyther.note.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository&lt;Note, Long&gt; { }
</code></pre></div>
<h3 dir="rtl" align="right">3) مهاجرت دیتابیس (Flyway) یا اجرای دستی SQL</h3>
<p dir="rtl" align="right">اگر Flyway دارید، یک فایل مثل <span dir="ltr"><code>V2__notes.sql</code></span> داخل <span dir="ltr"><code>src/main/resources/db/migration</code></span> بسازید؛ وگرنه همین SQL را یک‌بار روی DB اجرا کنید.</p>
<div dir="ltr"><pre><code class="language-sql">
-- MySQL / MariaDB
CREATE TABLE IF NOT EXISTS notes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  createdAt DATETIME(6) NULL,
  updatedAt DATETIME(6) NULL,
  title VARCHAR(180) NOT NULL,
  body TEXT NULL
) ENGINE=InnoDB;
</code></pre></div>
<div dir="ltr"><pre><code class="language-sql">
-- PostgreSQL
CREATE TABLE IF NOT EXISTS notes (
  id BIGSERIAL PRIMARY KEY,
  &quot;createdAt&quot; TIMESTAMP(6) NULL,
  &quot;updatedAt&quot; TIMESTAMP(6) NULL,
  title VARCHAR(180) NOT NULL,
  body TEXT NULL
);
</code></pre></div>
<p dir="rtl" align="right">> جدول <span dir="ltr"><code>action_logs</code></span> از قبل در <span dir="ltr"><code>V1__init.sql</code></span> ساخته می‌شود و برای **لاگ خودکار درخواست‌ها** استفاده می‌شود (<span dir="ltr"><code>method</code></span>, <span dir="ltr"><code>path</code></span>, <span dir="ltr"><code>status</code></span>, <span dir="ltr"><code>username</code></span>, <span dir="ltr"><code>ip</code></span>, ...).</p>
<h3 dir="rtl" align="right">4) DTO (ورودی ساخت)</h3>
<div dir="ltr"><pre><code class="language-java">
// src/main/java/ir/momeni/slyther/note/dto/CreateNoteDto.java
package ir.momeni.slyther.note.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateNoteDto(@NotBlank String title, String body) { }
</code></pre></div>
<h3 dir="rtl" align="right">5) Controller — نیاز به لاگین + لاگ خودکار و دستی</h3>
<ul dir="rtl">
<li dir="rtl" align="right">با <span dir="ltr"><code>@PreAuthorize</code></span> اطمینان حاصل می‌کنیم که کاربر **حتماً لاگین کرده** باشد؛ وگرنه پاسخ **401** برمی‌گردد.</li>
<li dir="rtl" align="right">«لاگ خودکار» توسط <span dir="ltr"><code>AuditInterceptor</code></span> برای همهٔ مسیرهای <span dir="ltr"><code>/api/**</code></span> انجام می‌شود.</li>
<li dir="rtl" align="right">اگر خواستید **رویدادهای بیزینسی** خاص را جداگانه ثبت کنید، از <span dir="ltr"><code>ActionLogService</code></span> استفاده کنید (لاگ **دستی**).</li>
</ul>
<div dir="ltr"><pre><code class="language-java">
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
@RequestMapping(&quot;/api/notes&quot;)
@RequiredArgsConstructor
public class NoteController {
private final NoteRepository repo;
private final ActionLogService logService; // برای لاگ دستیِ رویدادهای بیزینسی

    /**
     * ساخت یادداشت — فقط برای کاربر لاگین‌شده (401 در صورت عدم لاگین)
     */
    @PreAuthorize(&quot;isAuthenticated()&quot;)
    @PostMapping
    public Note create(@RequestBody @Valid CreateNoteDto dto,
                       HttpServletRequest req,
                       Authentication authentication,
                       @AuthenticationPrincipal User currentUser) {
        // کار اصلی
        var note = repo.save(Note.builder().title(dto.title()).body(dto.body()).build());

        // مثالِ لاگ دستیِ بیزینسی (اختیاری)
        String username = authentication != null ? authentication.getName() : null;
        logService.save(ActionLog.builder()
                .username(username)
                .method(&quot;POST&quot;)
                .path(req.getRequestURI())
                .ip(req.getHeader(&quot;X-Forwarded-For&quot;) != null ? req.getHeader(&quot;X-Forwarded-For&quot;).split(&quot;,&quot;)[0].trim() : req.getRemoteAddr())
                .userAgent(req.getHeader(&quot;User-Agent&quot;))
                .status(201)
                .success(true)
                .build());

        return note;
    }

    /**
     * لیست یادداشت‌ها — فقط کاربران نقش USER (403 اگر لاگین است ولی نقش ندارد)
     */
    @PreAuthorize(&quot;hasRole(&#x27;USER&#x27;)&quot;)
    @GetMapping
    public List&lt;Note&gt; list() {
        return repo.findAll();
    }

    /** نمونهٔ دسترسی به آبجکت کاربر جاری */
    @PreAuthorize(&quot;isAuthenticated()&quot;)
    @GetMapping(&quot;/me&quot;)
    public String me(@AuthenticationPrincipal User me) {
        return me != null ? me.getUsername() : &quot;anonymous&quot;;
    }
}
</code></pre></div>
<h3 dir="rtl" align="right">6) رفتار امنیتی و خطاها</h3>
<ul dir="rtl">
<li dir="rtl" align="right">اگر توکن ارسال نشود یا نامعتبر باشد، <span dir="ltr"><code>JwtAuthFilter</code></span> احراز هویت را ست نمی‌کند و مسیرهای محافظت‌شده با **401 Unauthorized** پاسخ می‌دهند.</li>
<li dir="rtl" align="right">اگر کاربر لاگین باشد ولی نقش لازم را نداشته باشد، **403 Forbidden** برمی‌گردد (نگاه کنید به <span dir="ltr"><code>@EnableMethodSecurity</code></span> و هندلرهای خطا).</li>
<li dir="rtl" align="right">ساختار خطاها طبق <span dir="ltr"><code>GlobalExceptionHandler</code></span> به‌صورت JSON و با مدل <span dir="ltr"><code>ApiError</code></span> است.</li>
</ul>
<h3 dir="rtl" align="right">7) چک‌لیست سریع برای اضافه‌کردن ماژول جدید</h3>
<ul dir="rtl">
<li dir="rtl" align="right">[ ] Entity + Repository بسازید.</li>
<li dir="rtl" align="right">[ ] (اختیاری) فایل مهاجرت Flyway اضافه کنید یا SQL را دستی اجرا کنید.</li>
<li dir="rtl" align="right">[ ] Controller با <span dir="ltr"><code>@PreAuthorize</code></span> بنویسید (مثلاً <span dir="ltr"><code>isAuthenticated()</code></span> یا <span dir="ltr"><code>hasRole(&#x27;USER&#x27;)</code></span>).</li>
<li dir="rtl" align="right">[ ] از <span dir="ltr"><code>@AuthenticationPrincipal User</code></span> یا <span dir="ltr"><code>Authentication</code></span> برای دسترسی به کاربر استفاده کنید.</li>
<li dir="rtl" align="right">[ ] برای رویدادهای بیزینسی مهم، علاوه بر لاگ خودکار، با <span dir="ltr"><code>ActionLogService</code></span> لاگ دستی هم ذخیره کنید.</li>
<li dir="rtl" align="right">[ ] مسیر جدید را زیر <span dir="ltr"><code>/api/...</code></span> نگه دارید تا **لاگ‌برداری خودکار** توسط <span dir="ltr"><code>AuditInterceptor</code></span> فعال باشد.</li>
</ul>
<h2 dir="rtl" align="right">یادداشت‌های تولید (Production Notes)</h2>
<ul dir="rtl">
<li dir="rtl" align="right"><span dir="ltr"><code>server.error.include-message/stacktrace</code></span> در prod غیرفعال است (ایمن). دست‌کاری نکنید مگر بدانید چه می‌کنید.</li>
<li dir="rtl" align="right">Swagger و OpenAPI در prod خاموش است.</li>
<li dir="rtl" align="right">**HTTPS** و رهاسازی راز JWT بسیار مهم است؛ از یک secret طولانی و تصادفی استفاده کنید؛ ادوارِ توکن را معقول تنظیم کنید.</li>
<li dir="rtl" align="right">اگر پشت Reverse Proxy هستید، هدرهای <span dir="ltr"><code>X-Forwarded-*</code></span> و <span dir="ltr"><code>X-Real-IP</code></span> در فیلتر نرخ‌سنجی لحاظ می‌شوند.</li>
<li dir="rtl" align="right">Bcrypt strength روی 12 است (در <span dir="ltr"><code>CryptoConfig</code></span>). اگر به کارایی بالاتر نیاز دارید، مقدار را بر اساس سخت‌افزار تنظیم کنید.</li>
<li dir="rtl" align="right">جداول دارای ایندکس‌های لازم (روی hash توکن‌ها و روابط اصلی) هستند.</li>
<li dir="rtl" align="right">Actuator: <span dir="ltr"><code>/actuator/health</code></span>, <span dir="ltr"><code>/actuator/info</code></span> باز هستند (طبق تنظیمات).</li>
</ul>
<h2 dir="rtl" align="right">مشکلات رایج</h2>
<ul dir="rtl">
<li dir="rtl" align="right">**DB در دسترس نیست / درایور پیدا نمی‌شود:** پروفایل دیتابیس را درست ست کنید (<span dir="ltr"><code>dev,mysql</code></span> یا <span dir="ltr"><code>dev,postgres</code></span>) و مطمئن شوید کانتینر/سرویس DB بالا است. برای MySQL از درایور MariaDB استفاده می‌شود.</li>
<li dir="rtl" align="right">**JWT کار نمی‌کند:** <span dir="ltr"><code>APP_SECURITY_JWT_SECRET</code></span> و <span dir="ltr"><code>APP_SECURITY_JWT_ISSUER</code></span> را ست کنید.</li>
<li dir="rtl" align="right">**Swagger نمی‌آید:** پروفایل <span dir="ltr"><code>dev</code></span> فعال باشد و به آدرس <span dir="ltr"><code>/swagger-ui.html</code></span> بروید (و پکیج‌های scan را مطابق پروژه تنظیم کنید؛ مثلاً <span dir="ltr"><code>ir.momeni.slyther.testapi</code></span>).</li>
<li dir="rtl" align="right">**429 Too Many Requests در لاگین/فراموشی رمز:** Rate Limiting به سقف رسیده؛ صبر کنید یا مقادیر را در <span dir="ltr"><code>app.security.ratelimit</code></span> بالا ببرید.</li>
</ul>

</div>