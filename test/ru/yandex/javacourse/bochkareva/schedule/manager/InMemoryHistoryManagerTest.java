package ru.yandex.javacourse.bochkareva.schedule.manager;

import org.junit.jupiter.api.Test;
import ru.yandex.javacource.bochkareva.schedule.manager.HistoryManager;
import ru.yandex.javacource.bochkareva.schedule.manager.InMemoryHistoryManager;
import ru.yandex.javacource.bochkareva.schedule.task.Task;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    @Test
    public void ShouldBeEmptyHistoryIfNotSeenTasks() {
        HistoryManager historyManager = new InMemoryHistoryManager();


        assertEquals(0,
                historyManager.getHistory().size(),
                "История непуста, хотя задачи не добавляли.");
    }

    @Test
    public void ShouldContainOneTaskIfTaskSeenMoreThanOneTime() {
        HistoryManager historyManager = new InMemoryHistoryManager();

        Task task = new Task(111, "New Task");

        historyManager.add(task);
        historyManager.add(task);

        assertEquals(1,
                historyManager.getHistory().size(),
                "Задача сохраняется в историю несколько раз.");
    }

    @Test
    public void ShouldNotContainTaskThatDeletedFromBeginOfHistory() {
        HistoryManager historyManager = new InMemoryHistoryManager();

        Task task1 = new Task(111, "New Task");
        Task task2 = new Task(222, "New Task");
        Task task3 = new Task(333, "New Task");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId());

        assertFalse(historyManager.getHistory().stream()
                    .map(Task::getId)
                    .anyMatch(id -> id == task1.getId()),
                "Не удаляется задача из начала истории.");
    }

    @Test
    public void ShouldNotContainTaskThatDeletedFromInnerPartOfHistory() {
        HistoryManager historyManager = new InMemoryHistoryManager();

        Task task1 = new Task(111, "New Task");
        Task task2 = new Task(222, "New Task");
        Task task3 = new Task(333, "New Task");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());

        assertFalse(historyManager.getHistory().stream()
                        .map(Task::getId)
                        .anyMatch(id -> id == task2.getId()),
                "Не удаляется задача из середины истории.");
    }

    @Test
    public void ShouldNotContainTaskThatDeletedFromEndOfHistory() {
        HistoryManager historyManager = new InMemoryHistoryManager();

        Task task1 = new Task(111, "New Task");
        Task task2 = new Task(222, "New Task");
        Task task3 = new Task(333, "New Task");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task3.getId());

        assertFalse(historyManager.getHistory().stream()
                        .map(Task::getId)
                        .anyMatch(id -> id == task3.getId()),
                "Не удаляется задача из конца истории.");
    }
}
