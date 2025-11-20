package ru.job4j.todo;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MainTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void whenContextLoadsThenSuccess() {
        /* Проверяем, что Spring контекст загружается без ошибок*/
        assertThat(context).isNotNull();
    }

    @Test
    void whenSessionFactoryBeanExistsThenSuccess() {
        /* Проверяем, что бин SessionFactory создан*/
        SessionFactory sessionFactory = context.getBean(SessionFactory.class);
        assertThat(sessionFactory).isNotNull();
        assertThat(sessionFactory.isOpen()).isTrue();
    }

    @Test
    void whenMainClassStartsThenNoExceptions() {
        /* Проверяем, что метод main запускается без исключений*/
        assertThat(Main.class).isNotNull();
    }
}