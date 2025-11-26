package ru.job4j.todo.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.model.User;
import ru.job4j.todo.store.TaskStore;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TaskService {
    private final TaskStore taskStore;

    public List<Task> findAll() {
        return taskStore.findAll();
    }

    public List<Task> findCompleted() {
        return taskStore.findCompleted();
    }

    public List<Task> findNew() {
        return taskStore.findNew();
    }

    public Optional<Task> findById(int id) {
        return taskStore.findById(id);
    }

    public boolean deleteById(int id) {
        return taskStore.deleteById(id);
    }

    public boolean completeTask(int id) {
        return taskStore.completeTask(id);
    }

    public Task createTask(Task task, User user) {
        task.setUser(user);
        return taskStore.createTask(task, user);
    }

    public boolean updateTask(Task task) {
        if (task.getId() == null) {
            throw new IllegalArgumentException("Task ID must not be null for update");
        }
        return taskStore.updateTask(task);
    }

    public List<Task> findAllByUser(User user) {
        return taskStore.findAllByUser(user);
    }

    public List<Task> findCompletedByUser(User user) {
        return taskStore.findCompletedByUser(user);
    }

    public List<Task> findNewByUser(User user) {
        return taskStore.findNewByUser(user);
    }
}