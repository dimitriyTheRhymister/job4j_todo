package ru.job4j.todo.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.job4j.todo.model.Priority;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.model.User;
import ru.job4j.todo.repository.PriorityRepository;
import ru.job4j.todo.repository.TaskRepository;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final PriorityRepository priorityRepository; // теперь это класс

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public List<Task> findCompleted() {
        return taskRepository.findCompleted();
    }

    public List<Task> findNew() {
        return taskRepository.findNew();
    }

    public Optional<Task> findById(int id) {
        return taskRepository.findById(id);
    }

    public boolean deleteById(int id) {
        return taskRepository.deleteById(id);
    }

    public boolean completeTask(int id) {
        return taskRepository.completeTask(id);
    }

    // Оригинальный метод (для обратной совместимости)
    public Task createTask(Task task, User user) {
        task.setUser(user);
        return taskRepository.createTask(task, user);
    }

    // Новый метод с поддержкой приоритета
    public Task createTask(Task task, User user, Integer priorityId) {
        task.setUser(user);

        if (priorityId != null) {
            Optional<Priority> priority = priorityRepository.findById(priorityId);
            priority.ifPresent(task::setPriority);
        }

        return taskRepository.createTask(task, user);
    }

    public boolean updateTask(Task task) {
        if (task.getId() == null) {
            throw new IllegalArgumentException("Task ID must not be null for update");
        }
        return taskRepository.updateTask(task);
    }

    public List<Task> findAllByUser(User user) {
        return taskRepository.findAllByUser(user);
    }

    public List<Task> findCompletedByUser(User user) {
        return taskRepository.findCompletedByUser(user);
    }

    public List<Task> findNewByUser(User user) {
        return taskRepository.findNewByUser(user);
    }

    /**
     * Получить все приоритеты
     */
    public List<Priority> getAllPriorities() {
        return priorityRepository.findAll();
    }

    /**
     * Найти приоритет по ID
     */
    public Optional<Priority> findPriorityById(int id) {
        return priorityRepository.findById(id);
    }

    /**
     * Найти приоритет по имени
     */
    public Optional<Priority> findPriorityByName(String name) {
        return priorityRepository.findByName(name);
    }
}