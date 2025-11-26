package ru.job4j.todo.store;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TaskStoreTest {

    @Autowired
    private TaskStore taskStore;

    @Autowired
    private UserStore userStore;

    private User testUser;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("Test User");
        testUser.setLogin("testuser");
        testUser.setPassword("password");
        testUser = userStore.createUser(testUser);

        task1 = new Task();
        task1.setDescription("Test task 1");
        task1.setCreated(LocalDateTime.now().minusHours(2));
        task1.setDone(false);
        task1.setUser(testUser);

        task2 = new Task();
        task2.setDescription("Test task 2");
        task2.setCreated(LocalDateTime.now().minusHours(1));
        task2.setDone(true);
        task2.setUser(testUser);

        task3 = new Task();
        task3.setDescription("Test task 3");
        task3.setCreated(LocalDateTime.now());
        task3.setDone(false);
        task3.setUser(testUser);

        task1 = taskStore.createTask(task1, testUser);
        task2 = taskStore.createTask(task2, testUser);
        task3 = taskStore.createTask(task3, testUser);
    }

    @AfterEach
    void tearDown() {
        taskStore.findAllByUser(testUser).forEach(task -> taskStore.deleteById(task.getId()));
        userStore.deleteById(testUser.getId());
    }

    @Test
    void whenFindAllByUserThenReturnAllUserTasks() {
        List<Task> tasks = taskStore.findAllByUser(testUser);

        assertThat(tasks).hasSize(3);
        assertThat(tasks).extracting(Task::getDescription)
                .containsExactlyInAnyOrder("Test task 1", "Test task 2", "Test task 3");
    }

    @Test
    void whenFindAllByUserThenReturnTasksOrderedByCreatedDesc() {
        List<Task> tasks = taskStore.findAllByUser(testUser);

        assertThat(tasks).hasSize(3);
        assertThat(tasks.get(0).getDescription()).isEqualTo("Test task 3");
        assertThat(tasks.get(1).getDescription()).isEqualTo("Test task 2");
        assertThat(tasks.get(2).getDescription()).isEqualTo("Test task 1");
    }

    @Test
    void whenFindCompletedByUserThenReturnOnlyCompletedTasks() {
        List<Task> completedTasks = taskStore.findCompletedByUser(testUser);

        assertThat(completedTasks).hasSize(1);
        assertThat(completedTasks.get(0).getDescription()).isEqualTo("Test task 2");
        assertThat(completedTasks.get(0).isDone()).isTrue();
    }

    @Test
    void whenFindNewByUserThenReturnOnlyNewTasks() {
        List<Task> newTasks = taskStore.findNewByUser(testUser);

        assertThat(newTasks).hasSize(2);
        assertThat(newTasks).extracting(Task::getDescription)
                .containsExactlyInAnyOrder("Test task 1", "Test task 3");
        assertThat(newTasks).allMatch(task -> !task.isDone());
    }

    @Test
    void whenFindByIdExistingThenReturnTask() {
        Optional<Task> foundTask = taskStore.findById(task1.getId());

        assertThat(foundTask).isPresent();
        assertThat(foundTask.get().getDescription()).isEqualTo("Test task 1");
        assertThat(foundTask.get().isDone()).isFalse();
        assertThat(foundTask.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void whenFindByIdNotExistingThenReturnEmpty() {
        Optional<Task> foundTask = taskStore.findById(999);

        assertThat(foundTask).isEmpty();
    }

    @Test
    void whenSaveNewTaskThenTaskHasIdAndUser() {
        Task newTask = new Task();
        newTask.setDescription("New test task");
        newTask.setCreated(LocalDateTime.now());
        newTask.setDone(false);
        newTask.setUser(testUser);

        Task savedTask = taskStore.createTask(newTask, testUser);

        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getDescription()).isEqualTo("New test task");
        assertThat(savedTask.getUser().getId()).isEqualTo(testUser.getId());

        Optional<Task> foundTask = taskStore.findById(savedTask.getId());
        assertThat(foundTask).isPresent();
        assertThat(foundTask.get().getDescription()).isEqualTo("New test task");
        assertThat(foundTask.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void whenDeleteByIdExistingThenReturnTrue() {
        boolean deleted = taskStore.deleteById(task1.getId());

        assertThat(deleted).isTrue();

        Optional<Task> foundTask = taskStore.findById(task1.getId());
        assertThat(foundTask).isEmpty();
    }

    @Test
    void whenDeleteByIdNotExistingThenReturnFalse() {
        boolean deleted = taskStore.deleteById(999);

        assertThat(deleted).isFalse();
    }

    @Test
    void whenCompleteTaskExistingThenReturnTrue() {
        boolean completed = taskStore.completeTask(task1.getId());

        assertThat(completed).isTrue();

        Optional<Task> foundTask = taskStore.findById(task1.getId());
        assertThat(foundTask).isPresent();
        assertThat(foundTask.get().isDone()).isTrue();
    }

    @Test
    void whenCompleteTaskNotExistingThenReturnFalse() {
        boolean completed = taskStore.completeTask(999);

        assertThat(completed).isFalse();
    }
}