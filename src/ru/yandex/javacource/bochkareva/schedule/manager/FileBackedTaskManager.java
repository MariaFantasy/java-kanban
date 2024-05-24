package ru.yandex.javacource.bochkareva.schedule.manager;

import ru.yandex.javacource.bochkareva.schedule.exceptions.ManagerSaveException;
import ru.yandex.javacource.bochkareva.schedule.task.Epic;
import ru.yandex.javacource.bochkareva.schedule.task.Subtask;
import ru.yandex.javacource.bochkareva.schedule.task.Task;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.FileWriter;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {

    private String filename;

    public FileBackedTaskManager(String filename) {
        super();
        this.filename = filename;
    }

    private void save() {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : super.getTasks()) {
                writer.write(task.toString() + ",\n");
            }
            for (Epic epic : super.getEpics()) {
                writer.write(epic.toString() + ",\n");
            }
            for (Subtask subtask : super.getSubtasks()) {
                writer.write(subtask.toString() + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка во время записи файла.");
        }
    }

    @Override
    public Integer addTask(Task task) {
        final int id = super.addTask(task);
        save();
        return id;
    }

    @Override
    public Integer addEpic(Epic epic) {
        final int id = super.addEpic(epic);
        save();
        return id;
    }

    @Override
    public Integer addSubtask(Subtask subtask) {
        final int id = super.addSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    public static void main(String[] args) {
        FileBackedTaskManager taskManager = new FileBackedTaskManager("C:\\Users\\PC#05\\Desktop\\Test Git\\mytest.txt");

        // Создаем две задачи
        Task task1Sprint7 = new Task(1, "Очень интересная задача 1");
        Task task2Sprint7 = new Task(1, "Очень интересная задача 2");

        int task1IdSprint7 = taskManager.addTask(task1Sprint7);
        int task2IdSprint7 = taskManager.addTask(task2Sprint7);

        // Создаем эпик с тремя подзадачами
        Task taskToEpic1Sprint7 = new Task(1, "Задача-эпик 1");
        Epic epic1Sprint7 = new Epic(taskToEpic1Sprint7);
        int epic1IdSprint7 = taskManager.addEpic(epic1Sprint7);

        Task taskToSubtask1taskManagerSprint7 = new Task(1, "Подзадача 1");
        Task taskToSubtask2taskManagerSprint7 = new Task(1, "Подзадача 2");
        Task taskToSubtask3taskManagerSprint7 = new Task(1, "Подзадача 3");
        Subtask subtask1Sprint7 = new Subtask(taskToSubtask1taskManagerSprint7, epic1IdSprint7);
        Subtask subtask2Sprint7 = new Subtask(taskToSubtask2taskManagerSprint7, epic1IdSprint7);
        Subtask subtask3Sprint7 = new Subtask(taskToSubtask3taskManagerSprint7, epic1IdSprint7);

        int subtask1IdSprint7 = taskManager.addSubtask(subtask1Sprint7);
        int subtask2IdSprint7 = taskManager.addSubtask(subtask2Sprint7);
        int subtask3IdSprint7 = taskManager.addSubtask(subtask3Sprint7);

        // Создаем эпик без подзадач
        Task taskToEpic2Sprint7 = new Task(1, "Задача-эпик 2");
        Epic epic2Sprint7 = new Epic(taskToEpic2Sprint7);
        int epic2IdSprint7 = taskManager.addEpic(epic2Sprint7);
    }
}
