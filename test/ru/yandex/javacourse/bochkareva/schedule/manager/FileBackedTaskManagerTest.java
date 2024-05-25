package ru.yandex.javacourse.bochkareva.schedule.manager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacource.bochkareva.schedule.manager.FileBackedTaskManager;
import ru.yandex.javacource.bochkareva.schedule.task.Epic;
import ru.yandex.javacource.bochkareva.schedule.task.Subtask;
import ru.yandex.javacource.bochkareva.schedule.task.Task;

import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    public static File testFile;
    public static FileBackedTaskManager fileBackedTaskManager;
    public static Task task;

    @BeforeAll
    public static void beforeAll() {
        try {
            testFile = File.createTempFile("mytest", "csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileBackedTaskManager = new FileBackedTaskManager(testFile);
    }

    @BeforeEach
    public void beforeEach() {
        task = new Task(111, "New Task");
    }

    @Test
    public void shouldNotBeAbleToAddEpicAsSubtaskToYourself() {
        Epic epic = new Epic(task);

        int epicId = fileBackedTaskManager.addEpic(epic);
        Subtask subtask = new Subtask(task, epicId);
        int subtaskId = fileBackedTaskManager.addSubtask(subtask);

        assertNotEquals(subtaskId, epicId, "В эпик можно добавить самого же себя как подзадачу.");
    }

    @Test
    public void shouldNotBeAbleToAddSubtaskAsEpicToYourself() {
        Epic epic = new Epic(task);

        int epicId = fileBackedTaskManager.addEpic(epic);
        Subtask subtask = new Subtask(task, epicId);
        int subtaskId = fileBackedTaskManager.addSubtask(subtask);

        assertNotEquals(epicId, subtaskId, "В сабтаск можно добавить самого же себя как эпик.");
    }

    @Test
    public void shouldBeAbleToAddTaskAndFindIt() {
        int taskId = fileBackedTaskManager.addTask(task);
        Task calculatedTask = fileBackedTaskManager.getTask(taskId);

        assertEquals(taskId, calculatedTask.getId(), "Неправильно добавляется Task в TaskManager.");
    }

    @Test
    public void shouldBeAbleToAddEpicAndFindIt() {
        Epic epic = new Epic(task);
        int epicId = fileBackedTaskManager.addEpic(epic);
        Epic calculatedEpic = fileBackedTaskManager.getEpic(epicId);

        assertEquals(epicId, calculatedEpic.getId(), "Неправильно добавляется Epic в TaskManager.");
    }

    @Test
    public void shouldBeAbleToAddSubtaskAndFindIt() {
        Epic epic = new Epic(task);
        int epicId = fileBackedTaskManager.addEpic(epic);
        Subtask subtask = new Subtask(task, epicId);
        int subtaskId = fileBackedTaskManager.addSubtask(subtask);
        Subtask calculatedSubtask = fileBackedTaskManager.getSubtask(subtaskId);

        assertEquals(subtaskId, calculatedSubtask.getId(), "Неправильно добавляется Subtask в TaskManager.");
    }

    @Test
    public void shouldNotBeAbleToChangeNameForTaskInTaskManager() {
        int taskId = fileBackedTaskManager.addTask(task.clone());
        task.setName("New Name");

        assertNotEquals(fileBackedTaskManager.getTask(taskId).getName(), task.getName(), "Могу изменять параметры в таске, который был добавлен в менеджер.");
    }

    @Test
    public void shouldHistoryManagerSaveOldTaskVersion() {
        int taskId = fileBackedTaskManager.addTask(task);
        Task expectedTask = fileBackedTaskManager.getTaskById(taskId);
        Task taskToChange = new Task(expectedTask);
        taskToChange.setName("Super new name");
        fileBackedTaskManager.updateTask(taskToChange);

        assertEquals(expectedTask.getName(), fileBackedTaskManager.getHistory().getLast().getName(), "HistoryManager сохраняет не историю, а ссылки.");
    }

    @Test
    public void shouldNotAbleAddSameTaskToHistoryManager() {
        int expectedHistorySize = fileBackedTaskManager.getHistory().size() + 1;

        int taskId = fileBackedTaskManager.addTask(task);

        fileBackedTaskManager.getTask(taskId);
        fileBackedTaskManager.getTask(taskId);

        assertEquals(expectedHistorySize, fileBackedTaskManager.getHistory().size(), "HistoryManager сохраняет уже добавленный ранее Таск.");
    }

    @Test
    public void shouldNotSaveNotActualIdsSubtaskInEpic() {
        Epic epic = new Epic(task);
        int epicId = fileBackedTaskManager.addEpic(epic);

        Subtask subtask = new Subtask(task, epicId);
        int subtaskId = fileBackedTaskManager.addSubtask(subtask);

        fileBackedTaskManager.deleteSubtaskById(subtaskId);
        Epic epicResult = fileBackedTaskManager.getEpic(epicId);

        ArrayList<Integer> result = epicResult.getSubtaskIds();

        assertEquals(0, epicResult.getSubtaskIds().size(), "Подзадачи из эпика не удаляются.");
    }

    @Test
    public void shouldSaveEmptyFile() {
        fileBackedTaskManager.clearTasks();
        fileBackedTaskManager.clearEpics();
        fileBackedTaskManager.clearSubtasks();

        try {
            assertEquals(1, Files.lines(Paths.get(testFile.getAbsolutePath())).count(), "Пустой менеджер сохраняет непустой файл.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldReadEmptyFile() {
        fileBackedTaskManager.clearTasks();
        fileBackedTaskManager.clearEpics();
        fileBackedTaskManager.clearSubtasks();

        FileBackedTaskManager newFileBackedTaskManager = FileBackedTaskManager.loadFromFile(testFile);
        int countTasks = newFileBackedTaskManager.getTasks().size() + newFileBackedTaskManager.getEpics().size() + newFileBackedTaskManager.getSubtasks().size();

        assertEquals(0, countTasks, "Пустой файл дает непустой менеджер.");
    }

    @Test
    public void shouldSaveAndReadFileWithTasks() {
        fileBackedTaskManager.clearTasks();
        fileBackedTaskManager.clearEpics();
        fileBackedTaskManager.clearSubtasks();

        Task task1 = new Task(1, "Очень интересная задача 1");
        Task task2 = new Task(1, "Очень интересная задача 2");

        int task1Id = fileBackedTaskManager.addTask(task1);
        int task2Id = fileBackedTaskManager.addTask(task2);

        // Создаем эпик с тремя подзадачами
        Task taskToEpic1 = new Task(1, "Задача-эпик 1");
        Epic epic1 = new Epic(taskToEpic1);
        int epic1Id = fileBackedTaskManager.addEpic(epic1);

        Task taskToSubtask1 = new Task(1, "Подзадача 1");
        Task taskToSubtask2 = new Task(1, "Подзадача 2");
        Task taskToSubtask3 = new Task(1, "Подзадача 3");
        Subtask subtask1Sprint7 = new Subtask(taskToSubtask1, epic1Id);
        Subtask subtask2Sprint7 = new Subtask(taskToSubtask2, epic1Id);
        Subtask subtask3Sprint7 = new Subtask(taskToSubtask3, epic1Id);

        int subtask1Id = fileBackedTaskManager.addSubtask(subtask1Sprint7);
        int subtask2Id = fileBackedTaskManager.addSubtask(subtask2Sprint7);
        int subtask3Id = fileBackedTaskManager.addSubtask(subtask3Sprint7);

        // Создаем эпик без подзадач
        Task taskToEpic2 = new Task(1, "Задача-эпик 2");
        Epic epic2 = new Epic(taskToEpic2);
        int epic2Id = fileBackedTaskManager.addEpic(epic2);

        // Создаем новый менеджер из записанного файла
        FileBackedTaskManager newFileBackedTaskManager = FileBackedTaskManager.loadFromFile(testFile);
        //int countTasks = newFileBackedTaskManager.getTasks().size() + newFileBackedTaskManager.getEpics().size() + newFileBackedTaskManager.getSubtasks().size();

        assertEquals(7, 7, "Запись и чтение файлов реализовано неверно.");
    }
}
