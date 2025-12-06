package ru.job4j.todo.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.job4j.todo.model.Category;
import ru.job4j.todo.model.Priority;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.model.User;
import ru.job4j.todo.repository.CategoryRepository;
import ru.job4j.todo.repository.PriorityRepository;
import ru.job4j.todo.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final PriorityRepository priorityRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Найти все задачи пользователя
     */
    public List<Task> findAllByUser(User user) {
        return taskRepository.findAllByUser(user);
    }

    /**
     * Найти выполненные задачи пользователя
     */
    public List<Task> findCompletedByUser(User user) {
        return taskRepository.findCompletedByUser(user);
    }

    /**
     * Найти новые (невыполненные) задачи пользователя
     */
    public List<Task> findNewByUser(User user) {
        return taskRepository.findNewByUser(user);
    }

    /**
     * Найти задачу по ID
     */
    public Optional<Task> findById(int id) {
        return taskRepository.findById(id);
    }

    /**
     * Создать новую задачу с выбранными категориями
     */
    public Task createTask(Task task, List<Integer> categoryIds) {
        if (task.getUser() == null) {
            throw new IllegalArgumentException("Task must be assigned to a user");
        }

        if (task.getPriority() != null && !priorityRepository.existsById(task.getPriority().getId())) {
            throw new IllegalArgumentException("Priority with ID " + task.getPriority().getId() + " does not exist");
        }

        if (categoryIds != null) {
            for (Integer id : categoryIds) {
                if (id == null || !categoryRepository.existsById(id)) {
                    throw new IllegalArgumentException("Category with ID " + id + " does not exist");
                }
            }
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            task.setCategories(categoryRepository.findByIds(categoryIds));
        } else {
            task.setCategories(new ArrayList<>());
        }

        return taskRepository.createTask(task);
    }

    /**
     * Обновить существующую задачу с новыми категориями
     */
    public boolean updateTask(Task task, List<Integer> categoryIds) {
        if (task.getId() == null) {
            throw new IllegalArgumentException("Task ID must not be null for update");
        }

        /* Валидация приоритета (если есть) */
        if (task.getPriority() != null && !priorityRepository.existsById(task.getPriority().getId())) {
            throw new IllegalArgumentException("Priority with ID " + task.getPriority().getId() + " does not exist");
        }

        /* Валидация категорий по ID (новая логика) */
        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (Integer catId : categoryIds) {
                if (catId == null || !categoryRepository.existsById(catId)) {
                    throw new IllegalArgumentException("Category with ID " + catId + " does not exist");
                }
            }
        }

        /* Теперь безопасно загружать категории */
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Category> selectedCategories = categoryRepository.findByIds(categoryIds);
            task.setCategories(selectedCategories);
        } else {
            task.setCategories(new ArrayList<>());
        }

        return taskRepository.updateTask(task);
    }

    /**
     * Отметить задачу как выполненную
     */
    public boolean completeTask(int id) {
        return taskRepository.completeTask(id);
    }

    /**
     * Удалить задачу по ID
     */
    public boolean deleteById(int id) {
        return taskRepository.deleteById(id);
    }

    /**
     * Получить все приоритеты
     */
    public List<Priority> getAllPriorities() {
        return priorityRepository.findAll();
    }

    /**
     * Получить все категории
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Получить категории по их ID
     */
    public List<Category> findCategoriesByIds(List<Integer> ids) {
        return categoryRepository.findByIds(ids);
    }

    /**
     * Проверить существование категории по ID
     */
    public boolean categoryExists(int id) {
        return categoryRepository.existsById(id);
    }

    /**
     * Проверить существование приоритета по ID
     */
    public boolean priorityExists(int id) {
        return priorityRepository.existsById(id);
    }

    /* ==== Вспомогательные методы ====  */

    /**
     * Валидация задачи перед сохранением
     */
    private void validateTask(Task task) {
        if (task.getUser() == null) {
            throw new IllegalArgumentException("Task must be assigned to a user");
        }

        /* Валидация приоритета (если установлен)  */
        if (task.getPriority() != null) {
            int priorityId = task.getPriority().getId();
            if (!priorityRepository.existsById(priorityId)) {
                throw new IllegalArgumentException("Priority with ID " + priorityId + " does not exist");
            }
        }

        /* Валидация категорий (если установлены)  */
        if (task.getCategories() != null && !task.getCategories().isEmpty()) {
            for (Category category : task.getCategories()) {
                if (!categoryRepository.existsById(category.getId())) {
                    throw new IllegalArgumentException("Category with ID " + category.getId() + " does not exist");
                }
            }
        }
    }
}