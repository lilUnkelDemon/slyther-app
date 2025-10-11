/* ---------- SESSIONS ---------- */
/* expire_at -> expires_at (اگر لازم باشد) */
SET @sql := (
  SELECT IF(
    EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sessions' AND COLUMN_NAME = 'expire_at')
    AND NOT EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sessions' AND COLUMN_NAME = 'expires_at'),
    'ALTER TABLE sessions CHANGE COLUMN expire_at expires_at DATETIME(6) NOT NULL',
    'SELECT 1'
  )
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

/* fallback: expiresAt -> expires_at (اگر هنوز camelCase باشد) */
SET @sql := (
  SELECT IF(
    EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sessions' AND COLUMN_NAME = 'expiresAt')
    AND NOT EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sessions' AND COLUMN_NAME = 'expires_at'),
    'ALTER TABLE sessions CHANGE COLUMN expiresAt expires_at DATETIME(6) NOT NULL',
    'SELECT 1'
  )
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


/* ---------- PASSWORD_RESET_TOKENS ---------- */
/* expire_at -> expires_at */
SET @sql := (
  SELECT IF(
    EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'password_reset_tokens' AND COLUMN_NAME = 'expire_at')
    AND NOT EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'password_reset_tokens' AND COLUMN_NAME = 'expires_at'),
    'ALTER TABLE password_reset_tokens CHANGE COLUMN expire_at expires_at DATETIME(6) NOT NULL',
    'SELECT 1'
  )
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

/* fallback: expiresAt -> expires_at */
SET @sql := (
  SELECT IF(
    EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'password_reset_tokens' AND COLUMN_NAME = 'expiresAt')
    AND NOT EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'password_reset_tokens' AND COLUMN_NAME = 'expires_at'),
    'ALTER TABLE password_reset_tokens CHANGE COLUMN expiresAt expires_at DATETIME(6) NOT NULL',
    'SELECT 1'
  )
); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
