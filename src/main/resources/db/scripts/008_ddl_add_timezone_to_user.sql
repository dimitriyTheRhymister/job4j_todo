-- Добавляем поле для хранения часового пояса пользователя
ALTER TABLE users ADD COLUMN IF NOT EXISTS user_zone VARCHAR(50);

-- Комментарий к полю
COMMENT ON COLUMN users.user_zone IS 'Часовой пояс пользователя (например, Europe/Moscow)';