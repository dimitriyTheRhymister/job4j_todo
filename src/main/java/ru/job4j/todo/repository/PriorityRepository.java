package ru.job4j.todo.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.job4j.todo.model.Priority;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class PriorityRepository {
    private final CrudRepository crudRepository;

    /**
     * Получить все приоритеты отсортированные по position
     */
    public List<Priority> findAll() {
        return crudRepository.query(
                "FROM Priority ORDER BY position",
                Priority.class
        );
    }

    /**
     * Найти приоритет по ID
     */
    public Optional<Priority> findById(int id) {
        return crudRepository.optional(
                "FROM Priority WHERE id = :id",
                Priority.class,
                Map.of("id", id)
        );
    }

    /**
     * Найти приоритет по имени
     */
    public Optional<Priority> findByName(String name) {
        return crudRepository.optional(
                "FROM Priority WHERE name = :name",
                Priority.class,
                Map.of("name", name)
        );
    }

    /**
     * Сохранить приоритет (для админки, если нужно будет добавлять новые)
     */
    public Priority save(Priority priority) {
        crudRepository.run(session -> session.save(priority));
        return priority;
    }

    /**
     * Удалить приоритет по ID
     */
    public boolean deleteById(int id) {
        int deletedCount = crudRepository.executeUpdate(
                "DELETE FROM Priority WHERE id = :id",
                Map.of("id", id)
        );
        return deletedCount > 0;
    }
}