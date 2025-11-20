package ru.job4j.todo.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.service.TaskService;

import java.util.List;

@Controller
@AllArgsConstructor
public class MainController {
    private final TaskService taskService;

    @GetMapping("/")
    public String index(Model model) {
        List<Task> tasks = taskService.findAll();
        model.addAttribute("tasks", tasks);
        return "index";
    }

    @GetMapping("/all")
    public String allTasks(Model model) {
        List<Task> tasks = taskService.findAll();
        model.addAttribute("tasks", tasks);
        model.addAttribute("filter", "all");
        return "index";
    }

    @GetMapping("/completed")
    public String completedTasks(Model model) {
        List<Task> tasks = taskService.findCompleted();
        model.addAttribute("tasks", tasks);
        model.addAttribute("filter", "completed");
        return "index";
    }

    @GetMapping("/new")
    public String newTasks(Model model) {
        List<Task> tasks = taskService.findNew();
        model.addAttribute("tasks", tasks);
        model.addAttribute("filter", "new");
        return "index";
    }
}