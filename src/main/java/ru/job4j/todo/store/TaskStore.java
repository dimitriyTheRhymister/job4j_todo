package ru.job4j.todo.store;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.model.User;

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

    public List<Task> findAllByUser(User user) {
        return crudRepository.query(
                "from Task where user = :user order by created desc",
                Task.class,
                Map.of("user", user)
        );
    }

    public List<Task> findCompletedByUser(User user) {
        return crudRepository.query(
                "from Task where user = :user and done = true order by created desc",
                Task.class,
                Map.of("user", user)
        );
    }

    public List<Task> findNewByUser(User user) {
        return crudRepository.query(
                "from Task where user = :user and done = false order by created desc",
                Task.class,
                Map.of("user", user)
        );
    }

    public Optional<Task> findById(int id) {
        return crudRepository.optional(
                "from Task where id = :fId", Task.class,
                Map.of("fId", id)
        );
    }

    public Task createTask(Task task, User user) {
        task.setUser(user);
        crudRepository.run(session -> session.save(task));
        return task;
    }

    public boolean deleteById(int id) {
        int deletedCount = crudRepository.executeUpdate(
                "DELETE FROM Task WHERE id = :id",
                Map.of("id", id)
        );
        return deletedCount > 0;
    }

    public boolean completeTask(int id) {
        int updatedCount = crudRepository.executeUpdate(
                "UPDATE Task SET done = true WHERE id = :id",
                Map.of("id", id)
        );
        return updatedCount > 0;
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