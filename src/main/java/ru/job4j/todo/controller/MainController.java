package ru.job4j.todo.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.model.User;
import ru.job4j.todo.service.TaskService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@AllArgsConstructor
public class MainController {
    private final TaskService taskService;

    @GetMapping("/")
    public String index(HttpServletRequest request, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        List<Task> tasks = user != null ? taskService.findAllByUser(user) : List.of();
        model.addAttribute("tasks", tasks);
        model.addAttribute("user", user);
        return "index";
    }

    @GetMapping("/all")
    public String allTasks(HttpServletRequest request, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        List<Task> tasks = user != null ? taskService.findAllByUser(user) : List.of();
        model.addAttribute("tasks", tasks);
        model.addAttribute("filter", "all");
        model.addAttribute("user", user);
        return "index";
    }

    @GetMapping("/completed")
    public String completedTasks(HttpServletRequest request, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        List<Task> tasks = user != null ? taskService.findCompletedByUser(user) : List.of();
        model.addAttribute("tasks", tasks);
        model.addAttribute("filter", "completed");
        model.addAttribute("user", user);
        return "index";
    }

    @GetMapping("/new")
    public String newTasks(HttpServletRequest request, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        List<Task> tasks = user != null ? taskService.findNewByUser(user) : List.of();
        model.addAttribute("tasks", tasks);
        model.addAttribute("filter", "new");
        model.addAttribute("user", user);
        return "index";
    }
}