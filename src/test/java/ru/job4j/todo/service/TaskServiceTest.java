package ru.job4j.todo.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    private User testUser;

    @AfterEach
    void tearDown() {
        if (testUser != null && testUser.getId() != null) {
            taskService.findAllByUser(testUser).forEach(task ->
                    taskService.deleteById(task.getId())
            );
            userService.deleteById(testUser.getId());
        }
    }

    @Test
    void whenFindAllByUserThenReturnUserTasks() {
        testUser = new User();
        testUser.setName("Test User");
        testUser.setLogin("testuser");
        testUser.setPassword("password");
        testUser = userService.save(testUser).orElseThrow();

        Task task1 = new Task();
        task1.setDescription("Service test 1");
        task1.setCreated(LocalDateTime.now());
        task1.setDone(false);
        task1.setUser(testUser);

        Task task2 = new Task();
        task2.setDescription("Service test 2");
        task2.setCreated(LocalDateTime.now());
        task2.setDone(true);
        task2.setUser(testUser);

        taskService.createTask(task1, testUser);
        taskService.createTask(task2, testUser);

        List<Task> tasks = taskService.findAllByUser(testUser);

        assertThat(tasks).hasSize(2);
        assertThat(tasks).extracting(Task::getDescription)
                .containsExactlyInAnyOrder("Service test 1", "Service test 2");
    }

    @Test
    void whenFindCompletedByUserThenReturnOnlyCompletedTasks() {
        testUser = new User();
        testUser.setName("Test User");
        testUser.setLogin("testuser2");
        testUser.setPassword("password");
        testUser = userService.save(testUser).orElseThrow();

        Task task1 = new Task();
        task1.setDescription("Completed task");
        task1.setCreated(LocalDateTime.now());
        task1.setDone(true);
        task1.setUser(testUser);

        Task task2 = new Task();
        task2.setDescription("New task");
        task2.setCreated(LocalDateTime.now());
        task2.setDone(false);
        task2.setUser(testUser);

        taskService.createTask(task1, testUser);
        taskService.createTask(task2, testUser);

        List<Task> completedTasks = taskService.findCompletedByUser(testUser);

        assertThat(completedTasks).hasSize(1);
        assertThat(completedTasks.get(0).getDescription()).isEqualTo("Completed task");
        assertThat(completedTasks.get(0).isDone()).isTrue();
    }

    @Test
    void whenFindNewByUserThenReturnOnlyNewTasks() {
        testUser = new User();
        testUser.setName("Test User");
        testUser.setLogin("testuser3");
        testUser.setPassword("password");
        testUser = userService.save(testUser).orElseThrow();

        Task task1 = new Task();
        task1.setDescription("New task 1");
        task1.setCreated(LocalDateTime.now());
        task1.setDone(false);
        task1.setUser(testUser);

        Task task2 = new Task();
        task2.setDescription("Completed task");
        task2.setCreated(LocalDateTime.now());
        task2.setDone(true);
        task2.setUser(testUser);

        taskService.createTask(task1, testUser);
        taskService.createTask(task2, testUser);

        List<Task> newTasks = taskService.findNewByUser(testUser);

        assertThat(newTasks).hasSize(1);
        assertThat(newTasks.get(0).getDescription()).isEqualTo("New task 1");
        assertThat(newTasks.get(0).isDone()).isFalse();
    }
}