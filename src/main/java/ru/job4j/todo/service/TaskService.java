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
    private final PriorityRepository priorityRepository;

    public List<Task> findAllByUser(User user) {
        return taskRepository.findAllByUser(user);
    }

    public List<Task> findCompletedByUser(User user) {
        return taskRepository.findCompletedByUser(user);
    }

    public List<Task> findNewByUser(User user) {
        return taskRepository.findNewByUser(user);
    }

    public Optional<Task> findById(int id) {
        return taskRepository.findById(id);
    }

    public Task createTask(Task task) {
        validateTask(task);
        return taskRepository.createTask(task);
    }

    public boolean updateTask(Task task) {
        if (task.getId() == null) {
            throw new IllegalArgumentException("Task ID must not be null for update");
        }
        validateTask(task);
        return taskRepository.updateTask(task);
    }

    public boolean completeTask(int id) {
        return taskRepository.completeTask(id);
    }

    public boolean deleteById(int id) {
        return taskRepository.deleteById(id);
    }

    public List<Priority> getAllPriorities() {
        return priorityRepository.findAll();
    }

    public Optional<Priority> findPriorityById(int id) {
        return priorityRepository.findById(id);
    }

    // ==== Вспомогательные методы ====

    private void validateTask(Task task) {
        if (task.getUser() == null) {
            throw new IllegalArgumentException("Task must be assigned to a user");
        }
        if (task.getPriority() != null) {
            int priorityId = task.getPriority().getId();
            if (!priorityRepository.existsById(priorityId)) {
                throw new IllegalArgumentException("Priority with ID " + priorityId + " does not exist");
            }
        }
    }
}