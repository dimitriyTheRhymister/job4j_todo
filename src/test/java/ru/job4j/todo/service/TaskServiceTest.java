package ru.job4j.todo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.job4j.todo.model.Task;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Test
    void whenFindAllThenReturnAllTasks() {
        Task task1 = new Task();
        task1.setDescription("Service test 1");
        task1.setCreated(LocalDateTime.now());
        task1.setDone(false);

        Task task2 = new Task();
        task2.setDescription("Service test 2");
        task2.setCreated(LocalDateTime.now());
        task2.setDone(true);

        taskService.createTask(task1);
        taskService.createTask(task2);

        List<Task> tasks = taskService.findAll();

        assertThat(tasks).isNotEmpty();

        taskService.findAll().forEach(task -> taskService.deleteById(task.getId()));
    }
}