package ru.job4j.todo.controller;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.job4j.todo.model.User;
import ru.job4j.todo.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/register")
    public String getRegistrationPage(Model model) {
        model.addAttribute("user", new User());
        return "users/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model, RedirectAttributes redirectAttributes) {
        try {
            var savedUser = userService.save(user);
            if (savedUser.isEmpty()) {
                model.addAttribute("error", "Registration failed");
                return "users/register";
            }

            redirectAttributes.addFlashAttribute("message", "Registration successful!");
            return "redirect:/users/login";

        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "Login already exists");
            return "users/register";
        }
    }

    @GetMapping("/login")
    public String getLoginPage(Model model) {
        model.addAttribute("user", new User());
        return "users/login";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute User user, Model model, HttpServletRequest request) {
        var userOptional = userService.findByLoginAndPassword(user.getLogin(), user.getPassword());
        if (userOptional.isEmpty()) {
            model.addAttribute("error", "Invalid login or password");
            return "users/login";
        }

        HttpSession session = request.getSession();
        session.setAttribute("user", userOptional.get());
        return "redirect:/";  /* Редирект на главную страницу */
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/users/login";
    }
}