-- Создание таблицы категорий
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Создание таблицы связей задач и категорий
CREATE TABLE task_categories (
    task_id INT NOT NULL,
    category_id INT NOT NULL,
    PRIMARY KEY (task_id, category_id),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- Создание индексов для улучшения производительности
CREATE INDEX idx_task_categories_task_id ON task_categories(task_id);
CREATE INDEX idx_task_categories_category_id ON task_categories(category_id);