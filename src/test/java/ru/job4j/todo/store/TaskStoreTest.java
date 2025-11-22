package ru.job4j.todo.store;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.job4j.todo.model.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TaskStoreTest {

    @Autowired
    private TaskStore taskStore;

    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        task1 = new Task();
        task1.setDescription("Test task 1");
        task1.setCreated(LocalDateTime.now().minusHours(2));
        task1.setDone(false);

        task2 = new Task();
        task2.setDescription("Test task 2");
        task2.setCreated(LocalDateTime.now().minusHours(1));
        task2.setDone(true);

        task3 = new Task();
        task3.setDescription("Test task 3");
        task3.setCreated(LocalDateTime.now());
        task3.setDone(false);

        task1 = taskStore.createTask(task1);
        task2 = taskStore.createTask(task2);
        task3 = taskStore.createTask(task3);
    }

    @AfterEach
    void tearDown() {
        taskStore.findAll().forEach(task -> taskStore.deleteById(task.getId()));
    }

    @Test
    void whenFindAllThenReturnAllTasks() {
        List<Task> tasks = taskStore.findAll();

        assertThat(tasks).hasSize(3);
        assertThat(tasks).extracting(Task::getDescription)
                .containsExactlyInAnyOrder("Test task 1", "Test task 2", "Test task 3");
    }

    @Test
    void whenFindAllThenReturnTasksOrderedByCreatedDesc() {
        List<Task> tasks = taskStore.findAll();

        assertThat(tasks).hasSize(3);
        assertThat(tasks.get(0).getDescription()).isEqualTo("Test task 3");
        assertThat(tasks.get(1).getDescription()).isEqualTo("Test task 2");
        assertThat(tasks.get(2).getDescription()).isEqualTo("Test task 1");
    }

    @Test
    void whenFindCompletedThenReturnOnlyCompletedTasks() {
        List<Task> completedTasks = taskStore.findCompleted();

        assertThat(completedTasks).hasSize(1);
        assertThat(completedTasks.get(0).getDescription()).isEqualTo("Test task 2");
        assertThat(completedTasks.get(0).isDone()).isTrue();
    }

    @Test
    void whenFindNewThenReturnOnlyNewTasks() {
        List<Task> newTasks = taskStore.findNew();

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
    }

    @Test
    void whenFindByIdNotExistingThenReturnEmpty() {
        Optional<Task> foundTask = taskStore.findById(999);

        assertThat(foundTask).isEmpty();
    }

    @Test
    void whenSaveNewTaskThenTaskHasId() {
        Task newTask = new Task();
        newTask.setDescription("New test task");
        newTask.setCreated(LocalDateTime.now());
        newTask.setDone(false);

        Task savedTask = taskStore.createTask(newTask);

        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getDescription()).isEqualTo("New test task");

        Optional<Task> foundTask = taskStore.findById(savedTask.getId());
        assertThat(foundTask).isPresent();
        assertThat(foundTask.get().getDescription()).isEqualTo("New test task");
    }

    @Test
    void whenUpdateExistingTaskThenTaskUpdated() {
        task1.setDescription("Updated task 1");
        task1.setDone(true);

        Task updatedTask = taskStore.createTask(task1);

        assertThat(updatedTask.getDescription()).isEqualTo("Updated task 1");
        assertThat(updatedTask.isDone()).isTrue();

        Optional<Task> foundTask = taskStore.findById(task1.getId());
        assertThat(foundTask).isPresent();
        assertThat(foundTask.get().getDescription()).isEqualTo("Updated task 1");
        assertThat(foundTask.get().isDone()).isTrue();
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