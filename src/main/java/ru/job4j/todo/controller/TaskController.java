package ru.job4j.todo.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.job4j.todo.model.Priority;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.model.User;
import ru.job4j.todo.service.TaskService;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    // Главная страница со всеми задачами текущего пользователя
    @GetMapping
    public String getAllTasks(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/users/login";
        }

        List<Task> tasks = taskService.findAllByUser(user);
        model.addAttribute("tasks", tasks);
        return "index"; // Изменил на index (главная страница)
    }

    // Страница создания задачи
    @GetMapping("/create")
    public String showCreateForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/users/login";
        }

        // Получаем список приоритетов для формы
        List<Priority> priorities = taskService.getAllPriorities();
        model.addAttribute("priorities", priorities);
        model.addAttribute("task", new Task());
        return "create";
    }

    @PostMapping("/create")
    public String createTask(@ModelAttribute Task task,
                             @RequestParam(value = "priorityId", required = false) Integer priorityId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Пользователь не авторизован");
            return "redirect:/users/login";
        }

        task.setUser(user);

        try {
            // Используем метод с приоритетом
            taskService.createTask(task, user, priorityId);
            redirectAttributes.addFlashAttribute("success", "Задача успешно создана!");
            return "redirect:/tasks";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при создании задачи: " + e.getMessage());
            return "redirect:/tasks/create";
        }
    }

    @GetMapping("/{id}")
    public String taskDetails(@PathVariable int id,
                              Model model,
                              HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/users/login";
        }

        Optional<Task> taskOptional = taskService.findById(id);
        if (taskOptional.isEmpty()) {
            model.addAttribute("errorMessage", "Задача с id " + id + " не найдена");
            return "error";
        }

        Task task = taskOptional.get();
        // Проверяем принадлежность задачи
        if (task.getUser() == null || !task.getUser().getId().equals(user.getId())) {
            model.addAttribute("errorMessage", "У вас нет доступа к этой задаче");
            return "error";
        }

        model.addAttribute("task", task);
        return "details";
    }

    // Страница редактирования задачи
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id,
                               Model model,
                               HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/users/login";
        }

        Optional<Task> taskOptional = taskService.findById(id);
        if (taskOptional.isEmpty()) {
            model.addAttribute("errorMessage", "Задача с id " + id + " не найдена");
            return "error";
        }

        Task task = taskOptional.get();
        // Проверяем принадлежность задачи
        if (task.getUser() == null || !task.getUser().getId().equals(user.getId())) {
            model.addAttribute("errorMessage", "У вас нет прав для редактирования этой задачи");
            return "error";
        }

        // Получаем список приоритетов для формы
        List<Priority> priorities = taskService.getAllPriorities();
        model.addAttribute("priorities", priorities);
        model.addAttribute("task", task);
        return "edit";
    }

    @PostMapping("/update")
    public String updateTask(@ModelAttribute Task task,
                             @RequestParam(value = "priorityId", required = false) Integer priorityId,
                             @RequestParam(value = "userId") Integer userId, // Добавляем отдельный параметр
                             HttpSession session,
                             Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/users/login";
        }

        // Проверяем, что редактируемая задача принадлежит текущему пользователю
        if (!userId.equals(user.getId())) {
            model.addAttribute("errorMessage", "У вас нет прав для редактирования этой задачи");
            return "error";
        }

        // Устанавливаем пользователя из сессии (а не из формы)
        task.setUser(user);

        // Устанавливаем приоритет
        if (priorityId != null) {
            Optional<Priority> priority = taskService.findPriorityById(priorityId);
            priority.ifPresent(task::setPriority);
        } else {
            task.setPriority(null);
        }

        try {
            boolean updated = taskService.updateTask(task);
            if (!updated) {
                model.addAttribute("errorMessage", "Задача не найдена");
                return "error";
            }
            return "redirect:/tasks/" + task.getId();
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Не удалось обновить задачу: " + e.getMessage());
            return "error";
        }
    }

    // Метод для отметки задачи как выполненной
    @PostMapping("/complete/{id}")
    public String completeTask(@PathVariable int id,
                               HttpSession session,
                               Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/users/login";
        }

        // Проверяем, принадлежит ли задача пользователю
        Optional<Task> taskOptional = taskService.findById(id);
        if (taskOptional.isPresent() && !taskOptional.get().getUser().getId().equals(user.getId())) {
            model.addAttribute("errorMessage", "У вас нет прав для выполнения этой операции");
            return "error";
        }

        boolean completed = taskService.completeTask(id);
        if (!completed) {
            model.addAttribute("errorMessage", "Не удалось отметить задачу как выполненную");
            return "error";
        }
        return "redirect:/tasks/" + id;
    }

    // Метод для удаления задачи
    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable int id,
                             HttpSession session,
                             Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/users/login";
        }

        // Проверяем, принадлежит ли задача пользователю
        Optional<Task> taskOptional = taskService.findById(id);
        if (taskOptional.isPresent() && !taskOptional.get().getUser().getId().equals(user.getId())) {
            model.addAttribute("errorMessage", "У вас нет прав для удаления этой задачи");
            return "error";
        }

        boolean deleted = taskService.deleteById(id);
        if (!deleted) {
            model.addAttribute("errorMessage", "Задача с id " + id + " не найдена");
            return "error";
        }
        return "redirect:/tasks";
    }
}