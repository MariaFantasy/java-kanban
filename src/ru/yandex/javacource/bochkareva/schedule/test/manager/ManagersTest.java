package ru.yandex.javacource.bochkareva.schedule.test.manager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.javacource.bochkareva.schedule.manager.HistoryManager;
import ru.yandex.javacource.bochkareva.schedule.manager.Managers;
import ru.yandex.javacource.bochkareva.schedule.manager.TaskManager;
import ru.yandex.javacource.bochkareva.schedule.task.Subtask;
import ru.yandex.javacource.bochkareva.schedule.task.Epic;
import ru.yandex.javacource.bochkareva.schedule.task.Task;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    public static Managers managers;
    @BeforeAll
    public static void beforeAll() {
        managers = new Managers();
    }

    @Test
    public void getDefauldReturnCorrectTaskManager() {
        TaskManager taskManager = managers.getDefault();
        ArrayList<Task> tasks = taskManager.getTasks();
        ArrayList<Epic> epics = taskManager.getEpics();
        ArrayList<Subtask> subtasks = taskManager.getSubtasks();

        HistoryManager historyManager = managers.getDefaultHistory();
        ArrayList<Task> history = historyManager.getHistory();

        assertNotNull(tasks, "TaskManager не был создан");
        assertNotNull(epics, "TaskManager не был создан");
        assertNotNull(subtasks, "TaskManager не был создан");
        assertNotNull(history, "HistoryManager не был создан");
    }
}