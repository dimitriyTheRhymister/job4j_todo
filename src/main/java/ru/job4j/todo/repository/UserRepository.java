package ru.job4j.todo.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.job4j.todo.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class UserRepository {
    private final CrudRepository crudRepository;

    public List<User> findAll() {
        return crudRepository.query("from User order by created desc", User.class);
    }

    public Optional<User> findById(int id) {
        return crudRepository.optional(
                "from User where id = :fId", User.class,
                Map.of("fId", id)
        );
    }

    public Optional<User> findByLogin(String login) {
        return crudRepository.optional(
                "from User where login = :login", User.class,
                Map.of("login", login)
        );
    }

    public Optional<User> findByLoginAndPassword(String login, String password) {
        return crudRepository.optional(
                "from User where login = :login and password = :password", User.class,
                Map.of(
                        "login", login,
                        "password", password
                )
        );
    }

    public User createUser(User user) {
        crudRepository.run(session -> session.save(user));
        return user;
    }

    public boolean deleteById(int id) {
        int deletedCount = crudRepository.executeUpdate(
                "DELETE FROM User WHERE id = :id",
                Map.of("id", id)
        );
        return deletedCount > 0;
    }

    public boolean updateUser(User user) {
        crudRepository.run(
                "UPDATE User SET name = :name, login = :login, password = :password WHERE id = :id",
                Map.of(
                        "name", user.getName(),
                        "login", user.getLogin(),
                        "password", user.getPassword(),
                        "id", user.getId()
                )
        );
        return true;
    }
}