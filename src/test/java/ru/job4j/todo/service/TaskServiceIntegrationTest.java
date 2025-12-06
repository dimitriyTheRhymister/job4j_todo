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
    private CrudRepository crudRepository; /* ✅ Внедряем CrudRepository напрямую */

    @PersistenceContext
    private EntityManager entityManager;

    private User testUser;
    private Priority testPriority;
    private Category catA, catB, catC;

    @BeforeEach
    void setUp() {
        /* Генерируем уникальный login ТОЛЬКО для пользователя */
        String uniqueLogin = "testlogin_" + UUID.randomUUID().toString().substring(0, 8);

        testUser = new User();
        testUser.setName("testuser");
        testUser.setLogin(uniqueLogin); /* ← уникально */
        testUser.setPassword("password");
        crudRepository.run(session -> {
            session.save(testUser);
            session.flush();
        });

        /* Приоритет и категории — БЕЗ уникальных имён! */
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

        /* Проверка, что ID присвоены и не null */
        assertThat(testUser.getId()).isNotNull();
        assertThat(testPriority.getId()).isNotNull();
        assertThat(catA.getId()).isNotNull();
        assertThat(catB.getId()).isNotNull();
        assertThat(catC.getId()).isNotNull();

        entityManager.clear();
    }

    /* 1. Создание задачи без категорий */
    @Test
    void whenCreateTaskWithoutCategories_thenTaskSavedWithEmptyCategories() {
        Task task = new Task();
        task.setDescription("Test task no categories");
        task.setPriority(testPriority);
        task.setUser(testUser);

        Task saved = taskService.createTask(task, null);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCategories()).isEmpty();

        /* Проверяем в БД напрямую */
        entityManager.clear();
        Optional<Task> fromDb = taskRepository.findById(saved.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getCategories()).isEmpty();
    }

    /* 2. Создание задачи с валидными категориями */
    @Test
    void whenCreateTaskWithValidCategories_thenTaskSavedWithCategories() {
        Task task = new Task();
        task.setDescription("Task with categories");
        task.setPriority(testPriority);
        task.setUser(testUser);

        List<Integer> categoryIds = Arrays.asList(catA.getId(), catB.getId());
        Task saved = taskService.createTask(task, categoryIds);

        assertThat(saved.getCategories()).hasSize(2);
        assertThat(saved.getCategories()).extracting(Category::getName)
                .containsExactlyInAnyOrder("Work", "Personal");
    }

    /* 3. Создание задачи с несуществующим ID категории */
    @Test
    void whenCreateTaskWithNonExistentCategoryId_thenThrowsIllegalArgumentException() {
        Task task = new Task();
        task.setDescription("Invalid category");
        task.setPriority(testPriority);
        task.setUser(testUser);

        List<Integer> invalidIds = Arrays.asList(catA.getId(), 99999); /* 99999 не существует */

        assertThatThrownBy(() -> taskService.createTask(task, invalidIds))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category with ID 99999 does not exist");
    }

    /* 4. Обновление задачи: замена категорий */
    @Test
    void whenUpdateTask_thenCategoriesReplacedInDatabase() {
        /* Создаём задачу с категориями A и B */
        Task task = new Task();
        task.setDescription("Original task");
        task.setPriority(testPriority);
        task.setUser(testUser);
        Task saved = taskService.createTask(task, Arrays.asList(catA.getId(), catB.getId()));

        /* Обновляем: оставляем только C */
        Task updatedTask = new Task();
        updatedTask.setId(saved.getId());
        updatedTask.setDescription("Updated task");
        updatedTask.setPriority(testPriority);
        updatedTask.setUser(testUser);

        boolean result = taskService.updateTask(updatedTask, Collections.singletonList(catC.getId()));

        assertThat(result).isTrue();

        /* Проверяем, что в БД только C */
        entityManager.clear();
        Optional<Task> fromDb = taskRepository.findById(saved.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getCategories()).hasSize(1);
        assertThat(fromDb.get().getCategories().get(0).getName()).isEqualTo("Urgent");
    }

    /* 5. Обновление с несуществующей категорией */
    @Test
    void whenUpdateTaskWithNonExistentCategory_thenThrowsIllegalArgumentException() {
        Task task = new Task();
        task.setDescription("ToUpdate");
        task.setPriority(testPriority);
        task.setUser(testUser);
        Task saved = taskService.createTask(task, Collections.singletonList(catA.getId()));

        Task update = new Task();
        update.setId(saved.getId());
        update.setDescription("Updated");
        update.setPriority(testPriority);
        update.setUser(testUser);

        assertThatThrownBy(() -> taskService.updateTask(update, List.of(88888)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category with ID 88888 does not exist");
    }

    /* 6. Обновление задачи без категорий → старые удаляются */
    @Test
    void whenUpdateTaskWithEmptyCategories_thenOldCategoriesRemoved() {
        Task task = new Task();
        task.setDescription("With cats");
        task.setPriority(testPriority);
        task.setUser(testUser);
        Task saved = taskService.createTask(task, Arrays.asList(catA.getId(), catB.getId()));

        /* Обновляем без категорий */
        Task update = new Task();
        update.setId(saved.getId());
        update.setDescription("No cats now");
        update.setPriority(testPriority);
        update.setUser(testUser);

        boolean result = taskService.updateTask(update, null);

        assertThat(result).isTrue();

        entityManager.clear();
        Optional<Task> fromDb = taskRepository.findById(saved.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getCategories()).isEmpty();
    }
}