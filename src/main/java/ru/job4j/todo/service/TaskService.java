package ru.job4j.todo.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.job4j.todo.model.Task;
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

    public Task createTask(Task task) {
        return taskStore.createTask(task);
    }

    public boolean updateTask(Task task) {
        if (task.getId() == null) {
            throw new IllegalArgumentException("Task ID must not be null for update");
        }
        return taskStore.updateTask(task);
    }
}