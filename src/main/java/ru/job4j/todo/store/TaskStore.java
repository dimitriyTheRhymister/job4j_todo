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

    public Task createTask(Task task) {
        return tx(session -> {
            session.save(task);
            return task;
        });
    }

    public boolean deleteById(int id) {
        return tx(session -> {
            int deletedCount = session.createQuery("DELETE FROM Task WHERE id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            return deletedCount > 0;
        });
    }

    public boolean completeTask(int id) {
        return tx(session -> {
            int updatedCount = session.createQuery("UPDATE Task SET done = true WHERE id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            return updatedCount > 0;
        });
    }

    public boolean updateTask(Task task) {
        return tx(session -> {
            int updatedCount = session.createQuery(
                            "UPDATE Task SET description = :description, done = :done WHERE id = :id"
                    )
                    .setParameter("description", task.getDescription())
                    .setParameter("done", task.isDone())
                    .setParameter("id", task.getId())
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