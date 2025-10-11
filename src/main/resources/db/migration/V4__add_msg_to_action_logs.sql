/* اگر ستون msg در action_logs وجود ندارد، اضافه کن */
SET @sql := (
  SELECT IF(
    NOT EXISTS(
      SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'action_logs'
        AND COLUMN_NAME = 'msg'
    ),
    'ALTER TABLE action_logs ADD COLUMN msg VARCHAR(512) NULL',
    'SELECT 1'
  )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
