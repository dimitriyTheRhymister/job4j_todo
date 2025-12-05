package ru.job4j.todo.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class TaskRepository {

    private final CrudRepository crudRepository;

    public Optional<Task> findById(int id) {
        return crudRepository.optional(
                "SELECT DISTINCT t FROM Task t "
                        +
                        "JOIN FETCH t.priority "
                        +
                        "JOIN FETCH t.user "
                        +
                        "WHERE t.id = :fId",
                Task.class,
                Map.of("fId", id)
        );
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

    public Task createTask(Task task) {
        crudRepository.run(session -> session.save(task));
        return task;
    }

    public boolean updateTask(Task task) {
        return crudRepository.tx(session -> {
            session.merge(task);
            return true;
        });
    }

    // ==== Запросы по пользователю ====

    public List<Task> findAllByUser(User user) {
        return crudRepository.query(
                "SELECT DISTINCT t FROM Task t "
                        +
                        "JOIN FETCH t.priority "
                        +
                        "WHERE t.user = :user "
                        +
                        "ORDER BY t.created DESC",
                Task.class,
                Map.of("user", user)
        );
    }

    public List<Task> findCompletedByUser(User user) {
        return crudRepository.query(
                "SELECT DISTINCT t FROM Task t "
                        +
                        "JOIN FETCH t.priority "
                        +
                        "WHERE t.user = :user AND t.done = true "
                        +
                        "ORDER BY t.created DESC",
                Task.class,
                Map.of("user", user)
        );
    }

    public List<Task> findNewByUser(User user) {
        return crudRepository.query(
                "SELECT DISTINCT t FROM Task t "
                        +
                        "JOIN FETCH t.priority "
                        +
                        "WHERE t.user = :user AND t.done = false "
                        +
                        "ORDER BY t.created DESC",
                Task.class,
                Map.of("user", user)
        );
    }
}