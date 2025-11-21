package ru.job4j.todo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, Model model, HttpServletRequest request) {
        LOGGER.error("Произошла ошибка при запросе: {} {}",
                request.getMethod(), request.getRequestURI(), ex);

        model.addAttribute("errorMessage",
                "Извините, произошла непредвиденная ошибка. Мы уже работаем над исправлением.");

        return "error";
    }
}