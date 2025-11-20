package ru.job4j.todo.store;

import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;
import ru.job4j.todo.model.Task;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Repository
@AllArgsConstructor
public class TaskStore {
    private final SessionFactory sf;

    public List<Task> findAll() {
        return tx(session -> session.createQuery("from Task order by created desc", Task.class).list());
    }

    public List<Task> findCompleted() {
        return tx(session -> session.createQuery("from Task where done = true order by created desc", Task.class).list());
    }

    public List<Task> findNew() {
        return tx(session -> session.createQuery("from Task where done = false order by created desc", Task.class).list());
    }

    public Optional<Task> findById(int id) {
        return tx(session -> {
            Task task = session.get(Task.class, id);
            return Optional.ofNullable(task);
        });
    }

    public Task save(Task task) {
        return tx(session -> {
            if (task.getId() == 0) {
                session.save(task);
            } else {
                session.update(task);
            }
            return task;
        });
    }

    public void deleteById(int id) {
        tx(session -> {
            Task task = new Task();
            task.setId(id);
            session.delete(task);
            return null;
        });
    }

    public void completeTask(int id) {
        tx(session -> {
            Task task = session.get(Task.class, id);
            if (task != null) {
                task.setDone(true);
                session.update(task);
            }
            return null;
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