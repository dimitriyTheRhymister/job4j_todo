package ru.job4j.todo.controller;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.job4j.todo.model.User;
import ru.job4j.todo.service.UserService;
import ru.job4j.todo.util.TimezoneUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("timezones", userService.getPopularTimezones());
        model.addAttribute("user", new User());
        return "users/register";
    }

    @PostMapping("/register")
    public String register(
            @ModelAttribute User user,
            @RequestParam String timezone,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Валидация часового пояса
        if (timezone == null || timezone.isEmpty() || !TimezoneUtils.isValidTimezone(timezone)) {
            timezone = "Europe/Moscow";
        }

        user.setTimezone(timezone);
        // Устанавливаем created в выбранном часовом поясе
        user.setCreated(TimezoneUtils.getCurrentDateTimeInTimezone(timezone));

        try {
            var savedUser = userService.save(user);
            if (savedUser.isEmpty()) {
                model.addAttribute("error", "Registration failed");
                model.addAttribute("timezones", userService.getPopularTimezones());
                return "users/register";
            }
            redirectAttributes.addFlashAttribute("message", "Registration successful!");
            return "redirect:/users/login";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "Login already exists");
            model.addAttribute("timezones", userService.getPopularTimezones());
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
            model.addAttribute("error", "Неверный логин или пароль");
            return "users/login";
        }

        User foundUser = userOptional.get();

        // Если у пользователя нет часового пояса, устанавливаем по умолчанию
        if (foundUser.getTimezone() == null || foundUser.getTimezone().isEmpty()) {
            foundUser.setTimezone("Europe/Moscow"); // Устанавливаем часовой пояс по умолчанию
            userService.update(foundUser); // Сохраняем изменения в базе данных
        }

        HttpSession session = request.getSession();
        session.setAttribute("user", foundUser);
        return "redirect:/tasks";
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