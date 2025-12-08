package ru.job4j.todo.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.job4j.todo.model.Category;
import ru.job4j.todo.model.Priority;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.model.User;
import ru.job4j.todo.service.TaskService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    private User testUser;
    private Task sampleTask;
    private Priority samplePriority;
    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setName("testuser");
        testUser.setLogin("testlogin");

        samplePriority = new Priority();
        samplePriority.setId(1);
        samplePriority.setName("High");

        sampleCategory = new Category();
        sampleCategory.setId(1);
        sampleCategory.setName("Work");

        sampleTask = new Task();
        sampleTask.setId(100);
        sampleTask.setDescription("Test task");
        sampleTask.setDone(false);
        sampleTask.setCreated(LocalDateTime.now());
        sampleTask.setUser(testUser);
        sampleTask.setPriority(samplePriority);
        sampleTask.setCategories(Collections.singletonList(sampleCategory));
    }

    @Test
    void getAllTasks_shouldReturnIndexWithTasks() throws Exception {
        when(taskService.findAllByUser(eq(testUser))).thenReturn(Collections.singletonList(sampleTask));

        mockMvc.perform(get("/tasks")
                        .sessionAttr("user", testUser))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("tasks", hasSize(1)));
    }

    @Test
    void showCreateForm_shouldReturnCreateViewWithPrioritiesAndCategories() throws Exception {
        when(taskService.getAllPriorities()).thenReturn(Collections.singletonList(samplePriority));
        when(taskService.getAllCategories()).thenReturn(Collections.singletonList(sampleCategory));

        mockMvc.perform(get("/tasks/create")
                        .sessionAttr("user", testUser))
                .andExpect(status().isOk())
                .andExpect(view().name("create"))
                .andExpect(model().attributeExists("priorities", "categories", "task"));
    }

    @Test
    void createTask_validData_shouldRedirectToTasksWithSuccessFlash() throws Exception {
        Task taskFromForm = new Task();
        taskFromForm.setDescription("New task");

        when(taskService.createTask(
                argThat(t -> "New task".equals(t.getDescription())),
                eq(1),          // priorityId = 1
                eq(List.of(1))  // categoryIds = [1]
        )).thenReturn(sampleTask);

        mockMvc.perform(post("/tasks/create")
                        .flashAttr("task", taskFromForm)
                        .param("priority.id", "1")      // ← ДОБАВЛЕНО
                        .param("categoryIds", "1")      // ← ДОБАВЛЕНО
                        .sessionAttr("user", testUser))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"))
                .andExpect(flash().attribute("success", "Задача успешно создана!"));
    }

    @Test
    void createTask_invalidCategory_shouldRedirectToCreateWithErrorFlash() throws Exception {
        Task taskFromForm = new Task();
        taskFromForm.setDescription("Bad task");

        when(taskService.createTask(any(Task.class), eq(1), eq(List.of(999))))
                .thenThrow(new IllegalArgumentException("Категория с ID 999 не существует"));

        mockMvc.perform(post("/tasks/create")
                        .flashAttr("task", taskFromForm)
                        .param("priority.id", "1")
                        .param("categoryIds", "999")    // ← несуществующая категория
                        .sessionAttr("user", testUser))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/create"))
                .andExpect(flash().attribute("error", "Категория с ID 999 не существует"));
    }

    @Test
    void taskDetails_ownTask_shouldReturnDetailsView() throws Exception {
        when(taskService.findById(100)).thenReturn(java.util.Optional.of(sampleTask));

        mockMvc.perform(get("/tasks/100")
                        .sessionAttr("user", testUser))
                .andExpect(status().isOk())
                .andExpect(view().name("details"))
                .andExpect(model().attribute("task", sampleTask));
    }

    @Test
    void taskDetails_taskNotFound_shouldReturnErrorView() throws Exception {
        when(taskService.findById(999)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/tasks/999")
                        .sessionAttr("user", testUser))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", "Задача с id 999 не найдена"));
    }

    @Test
    void taskDetails_foreignTask_shouldReturnErrorView() throws Exception {
        User otherUser = new User();
        otherUser.setId(999);
        Task foreignTask = new Task();
        foreignTask.setId(100);
        foreignTask.setUser(otherUser);
        foreignTask.setPriority(samplePriority);
        foreignTask.setCategories(Collections.emptyList());
        foreignTask.setCreated(LocalDateTime.now());

        when(taskService.findById(100)).thenReturn(java.util.Optional.of(foreignTask));

        mockMvc.perform(get("/tasks/100")
                        .sessionAttr("user", testUser))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", "У вас нет доступа к этой задаче"));
    }

    @Test
    void completeTask_validTask_shouldRedirectWithSuccess() throws Exception {
        when(taskService.findById(100)).thenReturn(java.util.Optional.of(sampleTask));
        when(taskService.completeTask(100)).thenReturn(true);

        mockMvc.perform(post("/tasks/complete/100")
                        .sessionAttr("user", testUser))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/100"))
                .andExpect(flash().attribute("success", "Задача отмечена как выполненная!"));
    }

    @Test
    void deleteTask_validTask_shouldRedirectToTasksWithSuccess() throws Exception {
        when(taskService.findById(100)).thenReturn(java.util.Optional.of(sampleTask));
        when(taskService.deleteById(100)).thenReturn(true);

        mockMvc.perform(post("/tasks/delete/100")
                        .sessionAttr("user", testUser))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"))
                .andExpect(flash().attribute("success", "Задача успешно удалена!"));
    }

    @Test
    void showCompletedTasks_shouldReturnIndexWithFilter() throws Exception {
        when(taskService.findCompletedByUser(testUser)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/tasks/completed")
                        .sessionAttr("user", testUser))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("filter", "completed"));
    }

    @Test
    void showNewTasks_shouldReturnIndexWithFilter() throws Exception {
        when(taskService.findNewByUser(testUser)).thenReturn(Collections.singletonList(sampleTask));

        mockMvc.perform(get("/tasks/new")
                        .sessionAttr("user", testUser))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("filter", "new"))
                .andExpect(model().attribute("tasks", hasSize(1)));
    }
}