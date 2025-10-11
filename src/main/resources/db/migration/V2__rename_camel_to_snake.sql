/* =========================
   V2: rename camelCase -> snake_case  (MariaDB 10.4 compatible)
   نکته: هیچ جا RENAME INDEX نداریم؛ همه‌چیز شرطی با PREPARE/EXECUTE
   ========================= */

/* ---------- ROLES ---------- */
-- createdAt -> created_at
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='roles' AND COLUMN_NAME='createdAt'),
            'ALTER TABLE roles CHANGE COLUMN createdAt created_at DATETIME(6) NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- updatedAt -> updated_at
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='roles' AND COLUMN_NAME='updatedAt'),
            'ALTER TABLE roles CHANGE COLUMN updatedAt updated_at DATETIME(6) NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


/* ---------- USERS ---------- */
-- createdAt -> created_at
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='users' AND COLUMN_NAME='createdAt'),
            'ALTER TABLE users CHANGE COLUMN createdAt created_at DATETIME(6) NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- updatedAt -> updated_at
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='users' AND COLUMN_NAME='updatedAt'),
            'ALTER TABLE users CHANGE COLUMN updatedAt updated_at DATETIME(6) NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


/* ---------- SESSIONS ---------- */
-- createdAt -> created_at
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='sessions' AND COLUMN_NAME='createdAt'),
            'ALTER TABLE sessions CHANGE COLUMN createdAt created_at DATETIME(6) NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- updatedAt -> updated_at
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='sessions' AND COLUMN_NAME='updatedAt'),
            'ALTER TABLE sessions CHANGE COLUMN updatedAt updated_at DATETIME(6) NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- expiresAt -> expire_at
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='sessions' AND COLUMN_NAME='expiresAt'),
            'ALTER TABLE sessions CHANGE COLUMN expiresAt expire_at DATETIME(6) NOT NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- userAgent -> user_agent
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='sessions' AND COLUMN_NAME='userAgent'),
            'ALTER TABLE sessions CHANGE COLUMN userAgent user_agent VARCHAR(255) NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ipAddress -> ip_address
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='sessions' AND COLUMN_NAME='ipAddress'),
            'ALTER TABLE sessions CHANGE COLUMN ipAddress ip_address VARCHAR(46) NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- refreshTokenHash -> refresh_token_hash
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='sessions' AND COLUMN_NAME='refreshTokenHash'),
            'ALTER TABLE sessions CHANGE COLUMN refreshTokenHash refresh_token_hash VARCHAR(64) NOT NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

/* ایندکس‌ها در MariaDB 10.4: بدون RENAME INDEX */
-- اگر ایندکس قدیمی با نام refreshTokenHash هست، DROP کن
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='sessions' AND INDEX_NAME='refreshTokenHash'),
            'ALTER TABLE sessions DROP INDEX refreshTokenHash',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- اگر ایندکس جدید وجود ندارد، بساز (Unique روی refresh_token_hash)
SET @sql := (
  SELECT IF(NOT EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
                       WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='sessions' AND INDEX_NAME='idx_session_refresh_hash'),
            'ALTER TABLE sessions ADD UNIQUE KEY idx_session_refresh_hash (refresh_token_hash)',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


/* ---------- ACTION_LOGS ---------- */
-- createdAt -> created_at
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='action_logs' AND COLUMN_NAME='createdAt'),
            'ALTER TABLE action_logs CHANGE COLUMN createdAt created_at DATETIME(6) NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- updatedAt -> updated_at
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='action_logs' AND COLUMN_NAME='updatedAt'),
            'ALTER TABLE action_logs CHANGE COLUMN updatedAt updated_at DATETIME(6) NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- userAgent -> user_agent
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='action_logs' AND COLUMN_NAME='userAgent'),
            'ALTER TABLE action_logs CHANGE COLUMN userAgent user_agent VARCHAR(255) NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- errorMessage -> error_message
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='action_logs' AND COLUMN_NAME='errorMessage'),
            'ALTER TABLE action_logs CHANGE COLUMN errorMessage error_message VARCHAR(512) NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


/* ---------- PASSWORD_RESET_TOKENS ---------- */
-- createdAt -> created_at
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='password_reset_tokens' AND COLUMN_NAME='createdAt'),
            'ALTER TABLE password_reset_tokens CHANGE COLUMN createdAt created_at DATETIME(6) NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- updatedAt -> updated_at
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='password_reset_tokens' AND COLUMN_NAME='updatedAt'),
            'ALTER TABLE password_reset_tokens CHANGE COLUMN updatedAt updated_at DATETIME(6) NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- tokenHash -> token_hash
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='password_reset_tokens' AND COLUMN_NAME='tokenHash'),
            'ALTER TABLE password_reset_tokens CHANGE COLUMN tokenHash token_hash VARCHAR(64) NOT NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- expiresAt -> expire_at
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='password_reset_tokens' AND COLUMN_NAME='expiresAt'),
            'ALTER TABLE password_reset_tokens CHANGE COLUMN expiresAt expire_at DATETIME(6) NOT NULL',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

/* ایندکسِ یونیک روی token_hash؛ اول قدیمی رو اگر بود DROP، بعد جدید رو اگر نبود ADD */
SET @sql := (
  SELECT IF(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='password_reset_tokens' AND INDEX_NAME='tokenHash'),
            'ALTER TABLE password_reset_tokens DROP INDEX tokenHash',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(NOT EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
                       WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='password_reset_tokens' AND INDEX_NAME='idx_prt_token_hash'),
            'ALTER TABLE password_reset_tokens ADD UNIQUE KEY idx_prt_token_hash (token_hash)',
            'SELECT 1')
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

/* =========================
   END
   ========================= */
