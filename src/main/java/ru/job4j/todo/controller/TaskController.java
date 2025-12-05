package ru.job4j.todo.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.job4j.todo.model.Priority;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.model.User;
import ru.job4j.todo.service.TaskService;

import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/tasks")
@Slf4j
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public String getAllTasks(
            Model model,
            @SessionAttribute("user") User user) {
        List<Task> tasks = taskService.findAllByUser(user);
        model.addAttribute("tasks", tasks);
        return "index";
    }

    @GetMapping("/create")
    public String showCreateForm(
            Model model,
            @SessionAttribute("user") User user) {
        List<Priority> priorities = taskService.getAllPriorities();
        model.addAttribute("priorities", priorities);
        model.addAttribute("task", new Task());
        return "create";
    }

    @PostMapping("/create")
    public String createTask(
            @ModelAttribute Task task,
            @SessionAttribute("user") User user,
            RedirectAttributes redirectAttributes) {

        task.setUser(user);

        try {
            taskService.createTask(task); // ← теперь принимает только task
            redirectAttributes.addFlashAttribute("success", "Задача успешно создана!");
            return "redirect:/tasks";
        } catch (Exception e) {
            log.error("Ошибка при создании задачи", e);
            redirectAttributes.addFlashAttribute("error", "Не удалось создать задачу");
            return "redirect:/tasks/create";
        }
    }

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

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable int id,
            Model model,
            @SessionAttribute("user") User user) {

        Task task = validateTaskOwnership(id, user, model);
        if (task == null) {
            return "error";
        }

        List<Priority> priorities = taskService.getAllPriorities();
        model.addAttribute("priorities", priorities);
        model.addAttribute("task", task);
        return "edit";
    }

    @PostMapping("/update")
    public String updateTask(
            @ModelAttribute Task task,
            @SessionAttribute("user") User user,
            Model model) {

        // Проверка принадлежности (без userId из формы!)
        Task existing = validateTaskOwnership(task.getId(), user, model);
        if (existing == null) {
            return "error";
        }

        task.setUser(user); // на всякий случай (хотя Hibernate может и не требовать)

        try {
            taskService.updateTask(task);
            return "redirect:/tasks/" + task.getId();
        } catch (Exception e) {
            log.error("Ошибка при обновлении задачи", e);
            model.addAttribute("errorMessage", "Не удалось обновить задачу");
            return "error";
        }
    }

    @PostMapping("/complete/{id}")
    public String completeTask(
            @PathVariable int id,
            @SessionAttribute("user") User user,
            Model model) {

        Task task = validateTaskOwnership(id, user, model);
        if (task == null) {
            return "error";
        }

        boolean completed = taskService.completeTask(id);
        if (!completed) {
            model.addAttribute("errorMessage", "Не удалось отметить задачу как выполненную");
            return "error";
        }
        return "redirect:/tasks/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteTask(
            @PathVariable int id,
            @SessionAttribute("user") User user,
            Model model) {

        Task task = validateTaskOwnership(id, user, model);
        if (task == null) {
            return "error";
        }

        boolean deleted = taskService.deleteById(id);
        if (!deleted) {
            model.addAttribute("errorMessage", "Задача с id " + id + " не найдена");
            return "error";
        }
        return "redirect:/tasks";
    }

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