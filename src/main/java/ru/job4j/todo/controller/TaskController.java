package ru.job4j.todo.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.service.TaskService;

import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("task", new Task());
        return "create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Task task) {
        taskService.save(task);
        return "redirect:/";
    }

    @GetMapping("/{id}")
    public String taskDetails(@PathVariable int id, Model model) {
        Optional<Task> taskOptional = taskService.findById(id);
        if (taskOptional.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("task", taskOptional.get());
        return "details";
    }

    @PostMapping("/complete/{id}")
    public String completeTask(@PathVariable int id) {
        taskService.completeTask(id);
        return "redirect:/tasks/" + id;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        Optional<Task> taskOptional = taskService.findById(id);
        if (taskOptional.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("task", taskOptional.get());
        return "edit";
    }

    @PostMapping("/update")
    public String updateTask(@ModelAttribute Task task) {
        taskService.save(task);
        return "redirect:/tasks/" + task.getId();
    }

    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable int id) {
        taskService.deleteById(id);
        return "redirect:/";
    }
}