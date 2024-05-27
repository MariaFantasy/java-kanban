package ru.yandex.javacource.bochkareva.schedule.manager;

import ru.yandex.javacource.bochkareva.schedule.exceptions.ManagerSaveException;
import ru.yandex.javacource.bochkareva.schedule.task.*;

import java.io.*;
import java.nio.file.Files;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private File file;
    private static final String HEADER = "id,type,name,status,description,epic";

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        final FileBackedTaskManager taskManager = new FileBackedTaskManager(file);

        try {
            final String csv = Files.readString(file.toPath());
            final String[] lines = csv.split(System.lineSeparator());
            int taskCounter = 0;

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (line.isEmpty()) {
                    break;
                }
                final Task task = taskFromString(line);
                final int id = task.getId();
                if (id > taskCounter) {
                    taskCounter = id;
                }
                taskManager.addAnyTask(task);
            }
            for (Subtask subtask : taskManager.subtasks.values()) {
                final Epic epic = taskManager.epics.get(subtask.getEpicId());
                epic.addSubtask(subtask.getId());
            }
            taskManager.taskCounter = taskCounter;
        } catch (FileNotFoundException e) {
            throw new ManagerSaveException("Файл для чтения не найден.");
        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка во время чтения файла.");
        }

        return taskManager;
    }

    public static Task taskFromString(String string) {
        final String[] taskInfo = string.split(",", -1);
        final int id = Integer.parseInt(taskInfo[0]);
        final TaskType type = TaskType.valueOf(taskInfo[1]);
        final String name = taskInfo[2];
        final TaskStatus status = TaskStatus.valueOf(taskInfo[3]);
        final String description = taskInfo[4];
        if (type == TaskType.TASK) {
            return new Task(id, name, description, status);
        }
        if (type == TaskType.EPIC) {
            return new Epic(id, name, description, status);
        }
        final int epicId = Integer.parseInt(taskInfo[5]);
        return new Subtask(id, name, description, status, epicId);
    }

    protected void addAnyTask(Task task) {
        final int id  = task.getId();
        switch (task.getType()) {
            case TASK:
                tasks.put(id, task);
                break;
            case EPIC:
                epics.put(id, (Epic) task);
                break;
            case SUBTASK:
                subtasks.put(id, (Subtask) task);
                break;
        }
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(HEADER);
            writer.newLine();

            for (Task task : tasks.values()) {
                writer.write(CSVTaskFormat.toString(task));
                writer.newLine();
            }
            for (Epic epic : epics.values()) {
                writer.write(CSVTaskFormat.toString(epic));
                writer.newLine();
            }
            for (Subtask subtask : subtasks.values()) {
                writer.write(CSVTaskFormat.toString(subtask));
                writer.newLine();
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

    public static void main(String[] args) throws IOException {
        File file = new File("resources/task.csv");
        FileBackedTaskManager taskManager = new FileBackedTaskManager(file);

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

        FileBackedTaskManager taskManager2 = FileBackedTaskManager.loadFromFile(file);
        printAllTasks(taskManager2);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getEpics()) {
            System.out.println(epic);

            for (Task task : manager.getSubtasksOfEpic(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}
