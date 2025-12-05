package ru.job4j.todo.dto;

import ru.job4j.todo.model.User;

public class CurrentUser {
    private final int id;
    private final String name;
    private final boolean guest;

    public static CurrentUser guest() {
        return new CurrentUser(0, "Гость", true);
    }

    public static CurrentUser of(User user) {
        return new CurrentUser(user.getId(), user.getName(), false);
    }

    private CurrentUser(int id, String name, boolean guest) {
        this.id = id;
        this.name = name;
        this.guest = guest;
    }

    // getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isGuest() {
        return guest;
    }
}