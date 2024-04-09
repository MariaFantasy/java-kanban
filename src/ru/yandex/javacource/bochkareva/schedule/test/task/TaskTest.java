package ru.yandex.javacource.bochkareva.schedule.test.task;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ru.yandex.javacource.bochkareva.schedule.task.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {
    @Test
    public void shouldBeEqualsWhenEqualsId() {
        Task task1 = new Task(1111, "New Task 1");
        Task task2 = new Task(1111, "New Task 2");

        assertEquals(task1, task2, "Таски не равны.");
    }
}