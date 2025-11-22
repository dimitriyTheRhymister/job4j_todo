package ru.job4j.todo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@SpringBootTest
@ActiveProfiles("test")
class MainTest {

    @Test
    void contextLoads() {
        /* Пустой тест - если контекст загрузился, тест пройден */
    }

    @Test
    void whenMainMethodStartsThenNoExceptions() {
        /* Просто проверяем, что класс компилируется и может быть запущен */
        assertThatCode(() -> Main.main(new String[]{}))
                .doesNotThrowAnyException();
    }
}