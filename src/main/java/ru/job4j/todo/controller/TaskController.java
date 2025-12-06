package ru.job4j.todo.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.job4j.todo.model.Category;
import ru.job4j.todo.model.Priority;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.model.User;
import ru.job4j.todo.service.TaskService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@RequestMapping("/tasks")
@Slf4j
public class TaskController {

    private final TaskService taskService;

    /**
     * Главная страница - список всех задач пользователя
     */
    @GetMapping
    public String getAllTasks(
            Model model,
            @SessionAttribute("user") User user) {
        List<Task> tasks = taskService.findAllByUser(user);
        model.addAttribute("tasks", tasks);
        return "index";
    }

    /**
     * Показать форму создания новой задачи
     */
    @GetMapping("/create")
    public String showCreateForm(
            Model model,
            @SessionAttribute("user") User user) {
        /* Загружаем все приоритеты и категории для выпадающих списков */
        List<Priority> priorities = taskService.getAllPriorities();
        List<Category> categories = taskService.getAllCategories();

        model.addAttribute("priorities", priorities);
        model.addAttribute("categories", categories);
        model.addAttribute("task", new Task());
        return "create";
    }

    /**
     * Создать новую задачу
     */
    @PostMapping("/create")
    public String createTask(
            @ModelAttribute Task task,
            @RequestParam(value = "categoryIds", required = false) List<Integer> categoryIds,
            @SessionAttribute("user") User user,
            RedirectAttributes redirectAttributes) {

        task.setUser(user);

        try {
            taskService.createTask(task, categoryIds);
            redirectAttributes.addFlashAttribute("success", "Задача успешно создана!");
            return "redirect:/tasks";
        } catch (IllegalArgumentException e) {
            log.error("Ошибка валидации при создании задачи", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/tasks/create";
        } catch (Exception e) {
            log.error("Ошибка при создании задачи", e);
            redirectAttributes.addFlashAttribute("error", "Не удалось создать задачу");
            return "redirect:/tasks/create";
        }
    }

    /**
     * Показать детали задачи
     */
    @GetMapping("/{id}")
    public String taskDetails(
            @PathVariable int id,
            Model model,
            @SessionAttribute("user") User user) {

        Task task = validateTaskOwnership(id, user, model);
        if (task == null) {
            return "error";
        }
        model.addAttribute("task", task);
        return "details";
    }

    /**
     * Показать форму редактирования задачи
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable int id,
            Model model,
            @SessionAttribute("user") User user) {

        Task task = validateTaskOwnership(id, user, model);
        if (task == null) {
            return "error";
        }

        /* Загружаем все приоритеты и категории для выпадающих списков */
        List<Priority> priorities = taskService.getAllPriorities();
        List<Category> categories = taskService.getAllCategories();

        /* Получаем ID выбранных категорий для предварительного выбора в форме */
        List<Integer> selectedCategoryIds = task.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toList());

        model.addAttribute("priorities", priorities);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryIds", selectedCategoryIds);
        model.addAttribute("task", task);
        return "edit";
    }

    /**
     * Обновить задачу
     */
    @PostMapping("/update")
    public String updateTask(
            @ModelAttribute Task task,
            @RequestParam(value = "categoryIds", required = false) List<Integer> categoryIds,
            @SessionAttribute("user") User user,
            Model model,
            RedirectAttributes redirectAttributes) {

        /* Проверка принадлежности задачи пользователю */
        Task existing = validateTaskOwnership(task.getId(), user, model);
        if (existing == null) {
            return "error";
        }

        /* Устанавливаем пользователя (Hibernate может потерять связь при merge) */
        task.setUser(user);

        try {
            boolean updated = taskService.updateTask(task, categoryIds);
            if (updated) {
                redirectAttributes.addFlashAttribute("success", "Задача успешно обновлена!");
                return "redirect:/tasks/" + task.getId();
            } else {
                model.addAttribute("errorMessage", "Не удалось обновить задачу");
                return "error";
            }
        } catch (IllegalArgumentException e) {
            log.error("Ошибка валидации при обновлении задачи", e);
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        } catch (Exception e) {
            log.error("Ошибка при обновлении задачи", e);
            model.addAttribute("errorMessage", "Не удалось обновить задачу");
            return "error";
        }
    }

    /**
     * Отметить задачу как выполненную
     */
    @PostMapping("/complete/{id}")
    public String completeTask(
            @PathVariable int id,
            @SessionAttribute("user") User user,
            Model model,
            RedirectAttributes redirectAttributes) {

        Task task = validateTaskOwnership(id, user, model);
        if (task == null) {
            return "error";
        }

        boolean completed = taskService.completeTask(id);
        if (completed) {
            redirectAttributes.addFlashAttribute("success", "Задача отмечена как выполненная!");
            return "redirect:/tasks/" + id;
        } else {
            model.addAttribute("errorMessage", "Не удалось отметить задачу как выполненную");
            return "error";
        }
    }

    /**
     * Удалить задачу
     */
    @PostMapping("/delete/{id}")
    public String deleteTask(
            @PathVariable int id,
            @SessionAttribute("user") User user,
            Model model,
            RedirectAttributes redirectAttributes) {

        Task task = validateTaskOwnership(id, user, model);
        if (task == null) {
            return "error";
        }

        boolean deleted = taskService.deleteById(id);
        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "Задача успешно удалена!");
            return "redirect:/tasks";
        } else {
            model.addAttribute("errorMessage", "Не удалось удалить задачу");
            return "error";
        }
    }

    /**
     * Фильтр: показать только выполненные задачи
     */
    @GetMapping("/completed")
    public String showCompletedTasks(
            Model model,
            @SessionAttribute("user") User user) {
        List<Task> tasks = taskService.findCompletedByUser(user);
        model.addAttribute("tasks", tasks);
        model.addAttribute("filter", "completed");
        return "index";
    }

    /**
     * Фильтр: показать только новые задачи
     */
    @GetMapping("/new")
    public String showNewTasks(
            Model model,
            @SessionAttribute("user") User user) {
        List<Task> tasks = taskService.findNewByUser(user);
        model.addAttribute("tasks", tasks);
        model.addAttribute("filter", "new");
        return "index";
    }

    /* ==== Вспомогательные методы ==== */

    /**
     * Проверяет, существует ли задача и принадлежит ли она пользователю.
     * Если проверка не пройдена — добавляет сообщение об ошибке в модель и возвращает null.
     * Иначе — возвращает задачу.
     */
    private Task validateTaskOwnership(int taskId, User user, Model model) {
        Optional<Task> taskOpt = taskService.findById(taskId);
        if (taskOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Задача с id " + taskId + " не найдена");
            return null;
        }

        Task task = taskOpt.get();
        if (task.getUser() == null || !task.getUser().getId().equals(user.getId())) {
            model.addAttribute("errorMessage", "У вас нет доступа к этой задаче");
            return null;
        }

        return task;
    }
}