package ru.job4j.todo.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.job4j.todo.model.Category;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class CategoryRepository {

    private final CrudRepository crudRepository;

    public List<Category> findAll() {
        return crudRepository.query(
                "FROM Category c ORDER BY c.name",
                Category.class
        );
    }

    public Optional<Category> findById(int id) {
        return crudRepository.optional(
                "FROM Category WHERE id = :fId",
                Category.class,
                Map.of("fId", id)
        );
    }

    public List<Category> findByIds(List<Integer> ids) {
        return crudRepository.query(
                "FROM Category WHERE id IN :ids",
                Category.class,
                Map.of("ids", ids)
        );
    }

    public boolean existsById(int id) {
        return findById(id).isPresent();
    }
}