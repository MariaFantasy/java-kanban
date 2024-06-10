package ru.yandex.javacourse.bochkareva.schedule.manager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacource.bochkareva.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacource.bochkareva.schedule.task.Epic;
import ru.yandex.javacource.bochkareva.schedule.task.Subtask;
import ru.yandex.javacource.bochkareva.schedule.task.Task;
import ru.yandex.javacource.bochkareva.schedule.task.TaskStatus;

import java.util.List;

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
        Task taskToChange = new Task(expectedTask);
        taskToChange.setName("Super new name");
        inMemoryTaskManager.updateTask(taskToChange);

        assertEquals(expectedTask.getName(),
                inMemoryTaskManager.getHistory().getLast().getName(),
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

    @Test
    public void shouldNotSaveNotActualIdsSubtaskInEpic() {
        Epic epic = new Epic(task);
        int epicId = inMemoryTaskManager.addEpic(epic);

        Subtask subtask = new Subtask(task, epicId);
        int subtaskId = inMemoryTaskManager.addSubtask(subtask);

        inMemoryTaskManager.deleteSubtaskById(subtaskId);
        Epic epicResult = inMemoryTaskManager.getEpic(epicId);

        List<Integer> result = epicResult.getSubtaskIds();

        assertEquals(0,
                epicResult.getSubtaskIds().size(),
                "Подзадачи из эпика не удаляются.");
    }

    @Test
    public void shouldBeNewEpicIfAllSubtasksNew() {
        Task taskToEpic = new Task(1, "Задача-эпик");
        Epic epic = new Epic(taskToEpic);
        int epicId = inMemoryTaskManager.addEpic(epic);

        Task taskToSubtask1 = new Task(1, "Подзадача 1");
        Task taskToSubtask2 = new Task(1, "Подзадача 2");
        Task taskToSubtask3 = new Task(1, "Подзадача 3");
        Subtask subtask1 = new Subtask(taskToSubtask1, epicId);
        Subtask subtask2 = new Subtask(taskToSubtask2, epicId);
        Subtask subtask3 = new Subtask(taskToSubtask3, epicId);

        subtask1.setStatus(TaskStatus.NEW);
        subtask2.setStatus(TaskStatus.NEW);
        subtask3.setStatus(TaskStatus.NEW);

        int subtask1Id = inMemoryTaskManager.addSubtask(subtask1);
        int subtask2Id = inMemoryTaskManager.addSubtask(subtask2);
        int subtask3Id = inMemoryTaskManager.addSubtask(subtask3);

        assertEquals(TaskStatus.NEW,
                inMemoryTaskManager.getTaskById(epicId).getStatus(),
                "Неверно определяется статус для эпика - NEW.");
    }

    @Test
    public void shouldBeDoneEpicIfAllSubtasksDone() {
        Task taskToEpic = new Task(1, "Задача-эпик");
        Epic epic = new Epic(taskToEpic);
        int epicId = inMemoryTaskManager.addEpic(epic);

        Task taskToSubtask1 = new Task(1, "Подзадача 1");
        Task taskToSubtask2 = new Task(1, "Подзадача 2");
        Task taskToSubtask3 = new Task(1, "Подзадача 3");
        Subtask subtask1 = new Subtask(taskToSubtask1, epicId);
        Subtask subtask2 = new Subtask(taskToSubtask2, epicId);
        Subtask subtask3 = new Subtask(taskToSubtask3, epicId);

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        subtask3.setStatus(TaskStatus.DONE);

        int subtask1Id = inMemoryTaskManager.addSubtask(subtask1);
        int subtask2Id = inMemoryTaskManager.addSubtask(subtask2);
        int subtask3Id = inMemoryTaskManager.addSubtask(subtask3);

        assertEquals(TaskStatus.DONE,
                inMemoryTaskManager.getTaskById(epicId).getStatus(),
                "Неверно определяется статус для эпика - DONE.");
    }

    @Test
    public void shouldBeInProgressEpicIfSubtasksInDoneAndNew() {
        Task taskToEpic = new Task(1, "Задача-эпик");
        Epic epic = new Epic(taskToEpic);
        int epicId = inMemoryTaskManager.addEpic(epic);

        Task taskToSubtask1 = new Task(1, "Подзадача 1");
        Task taskToSubtask2 = new Task(1, "Подзадача 2");
        Task taskToSubtask3 = new Task(1, "Подзадача 3");
        Subtask subtask1 = new Subtask(taskToSubtask1, epicId);
        Subtask subtask2 = new Subtask(taskToSubtask2, epicId);
        Subtask subtask3 = new Subtask(taskToSubtask3, epicId);

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.NEW);
        subtask3.setStatus(TaskStatus.NEW);

        int subtask1Id = inMemoryTaskManager.addSubtask(subtask1);
        int subtask2Id = inMemoryTaskManager.addSubtask(subtask2);
        int subtask3Id = inMemoryTaskManager.addSubtask(subtask3);

        assertEquals(TaskStatus.IN_PROGRESS,
                inMemoryTaskManager.getTaskById(epicId).getStatus(),
                "Неверно определяется статус для эпика - IN_PROGRESS.");
    }

    @Test
    public void shouldBeInProgressEpicIfAllSubtasksInProgress() {
        Task taskToEpic = new Task(1, "Задача-эпик");
        Epic epic = new Epic(taskToEpic);
        int epicId = inMemoryTaskManager.addEpic(epic);

        Task taskToSubtask1 = new Task(1, "Подзадача 1");
        Task taskToSubtask2 = new Task(1, "Подзадача 2");
        Task taskToSubtask3 = new Task(1, "Подзадача 3");
        Subtask subtask1 = new Subtask(taskToSubtask1, epicId);
        Subtask subtask2 = new Subtask(taskToSubtask2, epicId);
        Subtask subtask3 = new Subtask(taskToSubtask3, epicId);

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        subtask3.setStatus(TaskStatus.IN_PROGRESS);

        int subtask1Id = inMemoryTaskManager.addSubtask(subtask1);
        int subtask2Id = inMemoryTaskManager.addSubtask(subtask2);
        int subtask3Id = inMemoryTaskManager.addSubtask(subtask3);

        assertEquals(TaskStatus.IN_PROGRESS,
                inMemoryTaskManager.getTaskById(epicId).getStatus(),
                "Неверно определяется статус для эпика - IN_PROGRESS.");
    }

    @Test
    public void ShouldBeExistEpicForExistedSubtask() {
        Task taskToEpic = new Task(1, "Задача-эпик");
        Epic epic = new Epic(taskToEpic);
        int epicId = inMemoryTaskManager.addEpic(epic);

        Task taskToSubtask = new Task(1, "Подзадача");
        Subtask subtask = new Subtask(taskToSubtask, epicId);

        int subtaskId = inMemoryTaskManager.addSubtask(subtask);

        assertEquals(epicId,
                inMemoryTaskManager.getSubtask(subtaskId).getEpicId(),
                "Неверно сохраняется эпик для сабтаска.");
    }

    @Test
    public void ShouldBeExistSubtaskForExistedEpicWithSubtask() {
        Task taskToEpic = new Task(1, "Задача-эпик");
        Epic epic = new Epic(taskToEpic);
        int epicId = inMemoryTaskManager.addEpic(epic);

        Task taskToSubtask = new Task(1, "Подзадача");
        Subtask subtask = new Subtask(taskToSubtask, epicId);

        int subtaskId = inMemoryTaskManager.addSubtask(subtask);

        assertEquals(subtaskId,
                inMemoryTaskManager.getEpic(epicId).getSubtaskIds().getFirst(),
                "Неверно сохраняется сабтаск в эпике.");
    }
}
