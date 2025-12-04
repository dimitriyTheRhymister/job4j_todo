-- liquibase formatted sql

-- changeset author:todo id:005_add_priority_id_to_tasks
ALTER TABLE tasks
ADD COLUMN priority_id INTEGER,
ADD CONSTRAINT fk_task_priority
FOREIGN KEY (priority_id)
REFERENCES priorities(id)
ON DELETE SET NULL;

-- Устанавливаем приоритет "urgently" для существующих задач
UPDATE tasks
SET priority_id = (SELECT id FROM priorities WHERE name = 'urgently')
WHERE priority_id IS NULL;
-- rollback ALTER TABLE tasks DROP COLUMN priority_id;