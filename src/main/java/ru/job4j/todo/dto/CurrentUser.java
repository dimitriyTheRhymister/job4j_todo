package ru.job4j.todo.dto;

import ru.job4j.todo.model.User;

public class CurrentUser {
    private final int id;
    private final String name;
    private final String timezone; // ДОБАВЛЕНО
    private final boolean guest;

    public static CurrentUser guest() {
        return new CurrentUser(0, "Гость", "Europe/Moscow", true);
    }

    public static CurrentUser of(User user) {
        return new CurrentUser(
                user.getId(),
                user.getName(),
                user.getTimezone() != null ? user.getTimezone() : "Europe/Moscow",
                false
        );
    }

    private CurrentUser(int id, String name, String timezone, boolean guest) {
        this.id = id;
        this.name = name;
        this.timezone = timezone;
        this.guest = guest;
    }

    // getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTimezone() { // ДОБАВЛЕН
        return timezone;
    }

    public boolean isGuest() {
        return guest;
    }
}