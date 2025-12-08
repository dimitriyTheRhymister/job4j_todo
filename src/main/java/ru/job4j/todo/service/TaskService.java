package ru.job4j.todo.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.job4j.todo.model.Category;
import ru.job4j.todo.model.Priority;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.model.User;
import ru.job4j.todo.repository.CategoryRepository;
import ru.job4j.todo.repository.PriorityRepository;
import ru.job4j.todo.repository.TaskRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
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
     * Создать новую задачу с выбранными категориями (новая версия с приоритетом)
     */
    public Task createTask(Task task, int priorityId, List<Integer> categoryIds) {
        log.debug("Creating task with priorityId={}, categoryIds={}", priorityId, categoryIds);

        if (task.getUser() == null) {
            throw new IllegalArgumentException("Задача должна быть назначена пользователю");
        }

        // Получаем приоритет по ID
        Priority priority = priorityRepository.findById(priorityId)
                .orElseThrow(() -> new IllegalArgumentException("Приоритет с ID " + priorityId + " не найден"));
        task.setPriority(priority);

        // Валидация и установка категорий
        if (categoryIds != null && !categoryIds.isEmpty()) {
            validateCategoryIds(categoryIds);
            List<Category> categories = categoryRepository.findByIds(categoryIds);
            task.setCategories(categories);
        } else {
            task.setCategories(new ArrayList<>());
        }

        return taskRepository.createTask(task);
    }

    /**
     * Создать новую задачу с выбранными категориями (старая версия для обратной совместимости)
     */
    public Task createTask(Task task, List<Integer> categoryIds) {
        log.warn("Using deprecated createTask method without priorityId. Task priority: {}",
                task.getPriority() != null ? task.getPriority().getId() : "null");

        // Проверяем, есть ли приоритет в задаче
        if (task.getPriority() == null || task.getPriority().getId() == null) {
            throw new IllegalArgumentException("Приоритет не установлен. Используйте метод с priorityId");
        }

        // Проверяем существование приоритета
        if (!priorityRepository.existsById(task.getPriority().getId())) {
            throw new IllegalArgumentException("Приоритет с ID " + task.getPriority().getId() + " не существует");
        }

        // Валидация и установка категорий
        if (categoryIds != null && !categoryIds.isEmpty()) {
            validateCategoryIds(categoryIds);
            List<Category> categories = categoryRepository.findByIds(categoryIds);
            task.setCategories(categories);
        } else {
            task.setCategories(new ArrayList<>());
        }

        return taskRepository.createTask(task);
    }

    /**
     * Обновить существующую задачу с новыми категориями и приоритетом
     */
    public boolean updateTask(Task task, int priorityId, List<Integer> categoryIds) {
        log.debug("Updating task id={} with priorityId={}, categoryIds={}",
                task.getId(), priorityId, categoryIds);

        if (task.getId() == null) {
            throw new IllegalArgumentException("ID задачи не может быть null при обновлении");
        }

        // Получаем и устанавливаем приоритет
        Priority priority = priorityRepository.findById(priorityId)
                .orElseThrow(() -> new IllegalArgumentException("Приоритет с ID " + priorityId + " не найден"));
        task.setPriority(priority);

        // Валидация и установка категорий
        if (categoryIds != null && !categoryIds.isEmpty()) {
            validateCategoryIds(categoryIds);
            List<Category> categories = categoryRepository.findByIds(categoryIds);
            task.setCategories(categories);
        } else {
            task.setCategories(new ArrayList<>());
        }

        return taskRepository.updateTask(task);
    }

    /**
     * Обновить существующую задачу с новыми категориями (старая версия для обратной совместимости)
     */
    public boolean updateTask(Task task, List<Integer> categoryIds) {
        log.warn("Using deprecated updateTask method without priorityId");

        if (task.getId() == null) {
            throw new IllegalArgumentException("ID задачи не может быть null при обновлении");
        }

        // Проверяем приоритет
        if (task.getPriority() != null) {
            Integer priorityId = task.getPriority().getId();
            if (priorityId == null || !priorityRepository.existsById(priorityId)) {
                throw new IllegalArgumentException("Приоритет с ID " + priorityId + " не существует");
            }
        } else {
            throw new IllegalArgumentException("Приоритет не установлен. Используйте метод с priorityId");
        }

        // Валидация и установка категорий
        if (categoryIds != null && !categoryIds.isEmpty()) {
            validateCategoryIds(categoryIds);
            List<Category> categories = categoryRepository.findByIds(categoryIds);
            task.setCategories(categories);
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

    /**
     * Найти приоритет по ID
     */
    public Optional<Priority> findPriorityById(int id) {
        return priorityRepository.findById(id);
    }

    /**
     * Найти категорию по ID
     */
    public Optional<Category> findCategoryById(int id) {
        return categoryRepository.findById(id);
    }

    /* ==== Вспомогательные методы ==== */

    /**
     * Валидация ID категорий
     */
    private void validateCategoryIds(List<Integer> categoryIds) {
        if (categoryIds != null) {
            for (Integer catId : categoryIds) {
                if (catId == null) {
                    throw new IllegalArgumentException("ID категории не может быть null");
                }
                if (!categoryRepository.existsById(catId)) {
                    throw new IllegalArgumentException("Категория с ID " + catId + " не существует");
                }
            }
        }
    }

    /**
     * Валидация задачи перед сохранением
     */
    private void validateTask(Task task) {
        if (task.getUser() == null) {
            throw new IllegalArgumentException("Задача должна быть назначена пользователю");
        }

        // Валидация приоритета
        if (task.getPriority() != null) {
            Integer priorityId = task.getPriority().getId();
            if (priorityId == null) {
                throw new IllegalArgumentException("ID приоритета не может быть null");
            }
            if (!priorityRepository.existsById(priorityId)) {
                throw new IllegalArgumentException("Приоритет с ID " + priorityId + " не существует");
            }
        } else {
            throw new IllegalArgumentException("Приоритет не установлен");
        }

        // Валидация категорий
        if (task.getCategories() != null && !task.getCategories().isEmpty()) {
            for (Category category : task.getCategories()) {
                if (category.getId() == null) {
                    throw new IllegalArgumentException("ID категории не может быть null");
                }
                if (!categoryRepository.existsById(category.getId())) {
                    throw new IllegalArgumentException("Категория с ID " + category.getId() + " не существует");
                }
            }
        }
    }
}