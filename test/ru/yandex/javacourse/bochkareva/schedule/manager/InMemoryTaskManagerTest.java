package ru.yandex.javacourse.bochkareva.schedule.manager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacource.bochkareva.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacource.bochkareva.schedule.task.Epic;
import ru.yandex.javacource.bochkareva.schedule.task.Subtask;
import ru.yandex.javacource.bochkareva.schedule.task.Task;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    public static InMemoryTaskManager inMemoryTaskManager;
    public static Task task;

    @BeforeAll
    public static void beforeAll() {
        inMemoryTaskManager = new InMemoryTaskManager();
    }

    @BeforeEach
    public void beforeEach() {
        task = new Task(111, "New Task");
    }

    @Test
    public void shouldNotBeAbleToAddEpicAsSubtaskToYourself() {
        Epic epic = new Epic(task);

        int epicId = inMemoryTaskManager.addEpic(epic);
        Subtask subtask = new Subtask(task, epicId);
        int subtaskId = inMemoryTaskManager.addSubtask(subtask);

        assertNotEquals(subtaskId,
                epicId,
                "В эпик можно добавить самого же себя как подзадачу.");
    }

    @Test
    public void shouldNotBeAbleToAddSubtaskAsEpicToYourself() {
        Epic epic = new Epic(task);

        int epicId = inMemoryTaskManager.addEpic(epic);
        Subtask subtask = new Subtask(task, epicId);
        int subtaskId = inMemoryTaskManager.addSubtask(subtask);

        assertNotEquals(epicId,
                subtaskId,
                "В сабтаск можно добавить самого же себя как эпик.");
    }

    @Test
    public void shouldBeAbleToAddTaskAndFindIt() {
        int taskId = inMemoryTaskManager.addTask(task);
        Task calculatedTask = inMemoryTaskManager.getTask(taskId);

        assertEquals(taskId,
                calculatedTask.getId(),
                "Неправильно добавляется Task в TaskManager.");
    }

    @Test
    public void shouldBeAbleToAddEpicAndFindIt() {
        Epic epic = new Epic(task);
        int epicId = inMemoryTaskManager.addEpic(epic);
        Epic calculatedEpic = inMemoryTaskManager.getEpic(epicId);

        assertEquals(epicId,
                calculatedEpic.getId(),
                "Неправильно добавляется Epic в TaskManager.");
    }

    @Test
    public void shouldBeAbleToAddSubtaskAndFindIt() {
        Epic epic = new Epic(task);
        int epicId = inMemoryTaskManager.addEpic(epic);
        Subtask subtask = new Subtask(task, epicId);
        int subtaskId = inMemoryTaskManager.addSubtask(subtask);
        Subtask calculatedSubtask = inMemoryTaskManager.getSubtask(subtaskId);

        assertEquals(subtaskId,
                calculatedSubtask.getId(),
                "Неправильно добавляется Subtask в TaskManager.");
    }

    @Test
    public void shouldNotBeAbleToChangeNameForTaskInTaskManager() {
        int taskId = inMemoryTaskManager.addTask(task.clone());
        task.setName("New Name");

        assertNotEquals(inMemoryTaskManager.getTask(taskId).getName(),
                task.getName(),
                "Могу изменять параметры в таске, который был добавлен в менеджер.");
    }

    @Test
    public void shouldHistoryManagerSaveOldTaskVersion() {
        int taskId = inMemoryTaskManager.addTask(task);
        Task expectedTask = inMemoryTaskManager.getTaskById(taskId);
        inMemoryTaskManager.deleteTaskById(taskId);

        assertEquals(expectedTask,
                inMemoryTaskManager.getHistory().getLast(),
                "HistoryManager сохраняет не историю, а ссылки.");
    }

    @Test
    public void shouldNotAbleAddSameTaskToHistoryManager() {
        int expectedHistorySize = inMemoryTaskManager.getHistory().size() + 1;

        int taskId = inMemoryTaskManager.addTask(task);

        inMemoryTaskManager.getTask(taskId);
        inMemoryTaskManager.getTask(taskId);

        assertEquals(expectedHistorySize,
                inMemoryTaskManager.getHistory().size(),
                "HistoryManager сохраняет уже добавленный ранее Таск.");
    }
}