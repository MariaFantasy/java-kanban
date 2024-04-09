package ru.yandex.javacourse.bochkareva.schedule.task;

import org.junit.jupiter.api.Test;

import ru.yandex.javacource.bochkareva.schedule.task.Task;
import ru.yandex.javacource.bochkareva.schedule.task.Epic;

import static org.junit.jupiter.api.Assertions.assertEquals;


class EpicTest {

    @Test
    public void shouldBeEqualsWhenEqualsId() {
        Task task1 = new Task(1111, "New Task 1");
        Task task2 = new Task(1111, "New Task 2");
        Epic epic1 = new Epic(task1);
        Epic epic2 = new Epic(task2);

        assertEquals(epic1, epic2, "Эпики не равны.");
    }
}