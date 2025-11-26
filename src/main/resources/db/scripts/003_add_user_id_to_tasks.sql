-- Добавляем колонку user_id в таблицу tasks
ALTER TABLE tasks ADD COLUMN user_id INT;

-- Добавляем внешний ключ
ALTER TABLE tasks ADD CONSTRAINT fk_tasks_user
    FOREIGN KEY (user_id) REFERENCES users(id);

-- Обновляем существующие задачи (привязываем к admin пользователю)
UPDATE tasks SET user_id = (SELECT id FROM users WHERE login = 'admin')
WHERE user_id IS NULL;

-- Делаем колонку обязательной после заполнения
ALTER TABLE tasks ALTER COLUMN user_id SET NOT NULL;