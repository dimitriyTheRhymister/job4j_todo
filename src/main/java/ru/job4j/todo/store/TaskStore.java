package ru.job4j.todo.store;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.job4j.todo.model.Task;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class TaskStore {
    private final CrudRepository crudRepository;

    public List<Task> findAll() {
        return crudRepository.query("from Task order by created desc", Task.class);
    }

    public List<Task> findCompleted() {
        return crudRepository.query("from Task where done = true order by created desc", Task.class);
    }

    public List<Task> findNew() {
        return crudRepository.query("from Task where done = false order by created desc", Task.class);
    }

    public Optional<Task> findById(int id) {
        return crudRepository.optional(
                "from Task where id = :fId", Task.class,
                Map.of("fId", id)
        );
    }

    public Task createTask(Task task) {
        crudRepository.run(session -> session.save(task));
        return task;
    }

    public boolean deleteById(int id) {
        crudRepository.run(
                "DELETE FROM Task WHERE id = :id",
                Map.of("id", id)
        );
        return true;
    }

    public boolean completeTask(int id) {
        crudRepository.run(
                "UPDATE Task SET done = true WHERE id = :id",
                Map.of("id", id)
        );
        return true;
    }

    public boolean updateTask(Task task) {
        crudRepository.run(
                "UPDATE Task SET description = :description, done = :done WHERE id = :id",
                Map.of(
                        "description", task.getDescription(),
                        "done", task.isDone(),
                        "id", task.getId()
                )
        );
        return true;
    }
}