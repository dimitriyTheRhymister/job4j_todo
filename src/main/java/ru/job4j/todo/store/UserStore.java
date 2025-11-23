package ru.job4j.todo.store;

import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;
import ru.job4j.todo.model.User;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Repository
@AllArgsConstructor
public class UserStore {
    private final SessionFactory sf;

    public List<User> findAll() {
        return tx(session -> session.createQuery("from User order by created desc", User.class).list());
    }

    public Optional<User> findById(int id) {
        return tx(session -> {
            User user = session.get(User.class, id);
            return Optional.ofNullable(user);
        });
    }

    public Optional<User> findByLogin(String login) {
        return tx(session -> {
            User user = session.createQuery("from User where login = :login", User.class)
                    .setParameter("login", login)
                    .uniqueResult();
            return Optional.ofNullable(user);
        });
    }

    public Optional<User> findByLoginAndPassword(String login, String password) {
        return tx(session -> {
            User user = session.createQuery("from User where login = :login and password = :password", User.class)
                    .setParameter("login", login)
                    .setParameter("password", password)
                    .uniqueResult();
            return Optional.ofNullable(user);
        });
    }

    public User createUser(User user) {
        return tx(session -> {
            session.save(user);
            return user;
        });
    }

    public boolean deleteById(int id) {
        return tx(session -> {
            int deletedCount = session.createQuery("DELETE FROM User WHERE id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            return deletedCount > 0;
        });
    }

    public boolean updateUser(User user) {
        return tx(session -> {
            int updatedCount = session.createQuery(
                            "UPDATE User SET name = :name, login = :login, password = :password WHERE id = :id"
                    )
                    .setParameter("name", user.getName())
                    .setParameter("login", user.getLogin())
                    .setParameter("password", user.getPassword())
                    .setParameter("id", user.getId())
                    .executeUpdate();
            return updatedCount > 0;
        });
    }

    private <T> T tx(final Function<Session, T> command) {
        final Session session = sf.openSession();
        final Transaction tx = session.beginTransaction();
        try {
            T rsl = command.apply(session);
            tx.commit();
            return rsl;
        } catch (final Exception e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            session.close();
        }
    }
}