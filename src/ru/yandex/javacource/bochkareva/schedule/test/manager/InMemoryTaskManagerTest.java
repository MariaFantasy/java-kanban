package ru.yandex.javacource.bochkareva.schedule.test.manager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.javacource.bochkareva.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacource.bochkareva.schedule.task.Epic;
import ru.yandex.javacource.bochkareva.schedule.task.Subtask;
import ru.yandex.javacource.bochkareva.schedule.task.Task;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    public static InMemoryTaskManager inMemoryTaskManager;

    @BeforeAll
    public static void beforeAll() {
        inMemoryTaskManager = new InMemoryTaskManager();
    }

    @Test
    public void shouldNotBeAbleToAddEpicAsSubtaskToYourself() {
        Task task = new Task(111, "New Task");
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
        Task task = new Task(111, "New Task");
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
        Task task = new Task(111, "New Task");
        int taskId = inMemoryTaskManager.addTask(task);
        Task calculatedTask = inMemoryTaskManager.getTask(taskId);

        assertEquals(taskId,
                calculatedTask.getId(),
                "Неправильно добавляется Task в TaskManager.");
    }

    @Test
    public void shouldBeAbleToAddEpicAndFindIt() {
        Task task = new Task(111, "New Task");
        Epic epic = new Epic(task);
        int epicId = inMemoryTaskManager.addEpic(epic);
        Epic calculatedEpic = inMemoryTaskManager.getEpic(epicId);

        assertEquals(epicId,
                calculatedEpic.getId(),
                "Неправильно добавляется Epic в TaskManager.");
    }

    @Test
    public void shouldBeAbleToAddSubtaskAndFindIt() {
        Task task = new Task(111, "New Task");
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
        Task task = new Task(111, "New Task");
        int taskId = inMemoryTaskManager.addTask(task);
        task.setName("New Name");

        assertNotEquals(inMemoryTaskManager.getTask(taskId).getName(),
                task.getName(),
                "Могу изменять параметры в таске, который был добавлен в менеджер.");
    }

    @Test
    public void shouldHistoryManagerSaveOldTaskVersion() {
        Task task = new Task(111, "New Task");
        int taskId = inMemoryTaskManager.addTask(task);
        Task expectedTask = inMemoryTaskManager.getTaskById(taskId);
        inMemoryTaskManager.deleteTaskById(taskId);

        assertEquals(expectedTask,
                inMemoryTaskManager.getHistory().getLast(),
                "HistoryManager сохраняет не историю, а ссылки.");
    }
}