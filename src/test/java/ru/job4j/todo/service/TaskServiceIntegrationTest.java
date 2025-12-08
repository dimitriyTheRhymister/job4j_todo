package ru.job4j.todo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.todo.model.Category;
import ru.job4j.todo.model.Priority;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.model.User;
import ru.job4j.todo.repository.CategoryRepository;
import ru.job4j.todo.repository.CrudRepository;
import ru.job4j.todo.repository.PriorityRepository;
import ru.job4j.todo.repository.TaskRepository;
import ru.job4j.todo.repository.UserRepository;
import ru.job4j.todo.util.TimezoneUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class TaskServiceIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PriorityRepository priorityRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CrudRepository crudRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User testUser;
    private Priority testPriority;
    private Category catA, catB, catC;

    @BeforeEach
    void setUp() {
        String uniqueLogin = "testlogin_" + UUID.randomUUID().toString().substring(0, 8);

        testUser = new User();
        testUser.setName("testuser");
        testUser.setLogin(uniqueLogin);
        testUser.setPassword("password");
        testUser.setTimezone("Europe/Moscow");
        testUser.setCreated(TimezoneUtils.getCurrentDateTimeInTimezone("Europe/Moscow"));

        crudRepository.run(session -> {
            session.save(testUser);
            session.flush();
        });

        testPriority = new Priority();
        testPriority.setName("High");
        testPriority.setPosition(1);
        crudRepository.run(session -> {
            session.save(testPriority);
            session.flush();
        });

        catA = new Category(); catA.setName("Work");
        catB = new Category(); catB.setName("Personal");
        catC = new Category(); catC.setName("Urgent");

        crudRepository.run(session -> {
            session.save(catA);
            session.save(catB);
            session.save(catC);
            session.flush();
        });

        assertThat(testUser.getId()).isNotNull();
        assertThat(testPriority.getId()).isNotNull();
        assertThat(catA.getId()).isNotNull();
        assertThat(catB.getId()).isNotNull();
        assertThat(catC.getId()).isNotNull();

        entityManager.clear();
    }

    @Test
    void whenCreateTaskWithoutCategories_thenTaskSavedWithEmptyCategories() {
        Task task = new Task();
        task.setDescription("Test task no categories");
        task.setUser(testUser);

        Task saved = taskService.createTask(task, testPriority.getId(), null);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCategories()).isEmpty();

        entityManager.clear();
        Optional<Task> fromDb = taskRepository.findById(saved.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getCategories()).isEmpty();
    }

    @Test
    void whenCreateTaskWithValidCategories_thenTaskSavedWithCategories() {
        Task task = new Task();
        task.setDescription("Task with categories");
        task.setUser(testUser);

        List<Integer> categoryIds = Arrays.asList(catA.getId(), catB.getId());
        Task saved = taskService.createTask(task, testPriority.getId(), categoryIds);

        assertThat(saved.getCategories()).hasSize(2);
        assertThat(saved.getCategories()).extracting(Category::getName)
                .containsExactlyInAnyOrder("Work", "Personal");
    }

    @Test
    void whenCreateTaskWithNonExistentCategoryId_thenThrowsIllegalArgumentException() {
        Task task = new Task();
        task.setDescription("Invalid category");
        task.setUser(testUser);

        List<Integer> invalidIds = Arrays.asList(catA.getId(), 99999);

        assertThatThrownBy(() -> taskService.createTask(task, testPriority.getId(), invalidIds))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Категория с ID 99999 не существует");
    }

    @Test
    void whenUpdateTask_thenCategoriesReplacedInDatabase() {
        Task task = new Task();
        task.setDescription("Original task");
        task.setUser(testUser);
        Task saved = taskService.createTask(task, testPriority.getId(), Arrays.asList(catA.getId(), catB.getId()));

        Task updatedTask = new Task();
        updatedTask.setId(saved.getId());
        updatedTask.setDescription("Updated task");
        updatedTask.setUser(testUser);

        boolean result = taskService.updateTask(updatedTask, testPriority.getId(), Collections.singletonList(catC.getId()));

        assertThat(result).isTrue();

        entityManager.clear();
        Optional<Task> fromDb = taskRepository.findById(saved.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getCategories()).hasSize(1);
        assertThat(fromDb.get().getCategories().get(0).getName()).isEqualTo("Urgent");
    }

    @Test
    void whenUpdateTaskWithNonExistentCategory_thenThrowsIllegalArgumentException() {
        Task task = new Task();
        task.setDescription("ToUpdate");
        task.setUser(testUser);
        Task saved = taskService.createTask(task, testPriority.getId(), Collections.singletonList(catA.getId()));

        Task update = new Task();
        update.setId(saved.getId());
        update.setDescription("Updated");
        update.setUser(testUser);

        assertThatThrownBy(() -> taskService.updateTask(update, testPriority.getId(), List.of(88888)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Категория с ID 88888 не существует");
    }

    @Test
    void whenUpdateTaskWithEmptyCategories_thenOldCategoriesRemoved() {
        Task task = new Task();
        task.setDescription("With cats");
        task.setUser(testUser);
        Task saved = taskService.createTask(task, testPriority.getId(), Arrays.asList(catA.getId(), catB.getId()));

        Task update = new Task();
        update.setId(saved.getId());
        update.setDescription("No cats now");
        update.setUser(testUser);

        boolean result = taskService.updateTask(update, testPriority.getId(), null);

        assertThat(result).isTrue();

        entityManager.clear();
        Optional<Task> fromDb = taskRepository.findById(saved.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getCategories()).isEmpty();
    }
}