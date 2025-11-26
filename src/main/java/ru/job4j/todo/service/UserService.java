package ru.job4j.todo.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.job4j.todo.model.User;
import ru.job4j.todo.store.UserStore;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserStore userStore;

    public Optional<User> save(User user) {
        return Optional.ofNullable(userStore.createUser(user));
    }

    public Optional<User> findByLoginAndPassword(String login, String password) {
        return userStore.findByLoginAndPassword(login, password);
    }

    public boolean deleteById(int id) {
        return userStore.deleteById(id);
    }
}