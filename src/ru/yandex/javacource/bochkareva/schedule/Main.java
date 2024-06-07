package ru.yandex.javacource.bochkareva.schedule;

import ru.yandex.javacource.bochkareva.schedule.manager.Managers;
import ru.yandex.javacource.bochkareva.schedule.manager.TaskManager;
import ru.yandex.javacource.bochkareva.schedule.task.*;

public class Main {

    public static void main(String[] args) {
        Managers managers = new Managers();
        TaskManager taskManager = managers.getDefault();

        Task task1 = new Task(1, "Прочитать статью", "https://habr.com/ru/articles/801431/");
        Task task2 = new Task(1, "Прочитать статью", "https://habr.com/ru/companies/cdek_blog/articles/796451/");

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        Task taskToEpic1 = new Task(1, "Пройти теорию и практику 4 спринта", "https://practicum.yandex.ru/learn/");
        Epic epic1 = new Epic(taskToEpic1);
        int epicId = taskManager.addEpic(epic1);

        Task taskToSubtask1 = new Task(1, "ООП Инкапсуляция");
        Subtask subtask1 = new Subtask(taskToSubtask1, epicId);
        Task taskToSubtask2 = new Task(1, "ООП Наследование");
        Subtask subtask2 = new Subtask(taskToSubtask2, epicId);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Task taskToEpic2 = new Task(1, "Сделать проект 4 спринта", "https://practicum.yandex.ru/learn/");
        Epic epic2 = new Epic(taskToEpic2);
        epicId = taskManager.addEpic(epic2);

        Task taskToSubtask3 = new Task(1, "ООП Инкапсуляция");
        Subtask subtask3 = new Subtask(taskToSubtask3, epicId);
        taskManager.addSubtask(subtask3);

        taskManager.getTasks().stream().peek(System.out::println);
        taskManager.getEpics().stream().peek(System.out::println);
        taskManager.getSubtasks().stream().peek(System.out::println);

        System.out.println("---------------------------------------");
        System.out.println("Обновление статусов:");

        task1.setStatus(TaskStatus.IN_PROGRESS);
        task2.setStatus(TaskStatus.DONE);
        taskManager.updateTask(task1);
        taskManager.updateTask(task2);
        taskManager.getTasks().stream().peek(System.out::println);

        System.out.println("--------------");

        epic1.setStatus(TaskStatus.IN_PROGRESS);
        epic2.setStatus(TaskStatus.DONE);
        taskManager.updateEpic(epic1);
        taskManager.updateEpic(epic2);
        taskManager.getEpics().stream().peek(System.out::println);

        System.out.println("--------------");
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        taskManager.getEpics().stream().peek(System.out::println);

        System.out.println("--------------");
        subtask2.setStatus(TaskStatus.DONE);
        subtask3.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask2);
        taskManager.updateSubtask(subtask3);
        taskManager.getEpics().stream().peek(System.out::println);
        taskManager.getSubtasks().stream().peek(System.out::println);

        System.out.println("---------------------------------------");
        System.out.println("Удаление задач:");
        taskManager.deleteTaskById(1);
        taskManager.getTasks().stream().peek(System.out::println);

        System.out.println("---------------------------------------");
        System.out.println("Удаление эпиков:");
        taskManager.deleteEpicById(6);
        taskManager.getEpics().stream().peek(System.out::println);
        taskManager.getSubtasks().stream().peek(System.out::println);

        System.out.println("---------------------------------------");
        System.out.println("Удаление подзадач:");
        taskManager.deleteSubtaskById(4);
        taskManager.getEpics().stream().peek(System.out::println);
        taskManager.getSubtasks().stream().peek(System.out::println);

        System.out.println("---------------------------------------");
        System.out.println("Удаление несуществующих задач, эпиков и подзадач:");
        taskManager.deleteTaskById(10);
        taskManager.deleteEpicById(10);
        taskManager.deleteSubtaskById(10);
        taskManager.getTasks().stream().peek(System.out::println);
        taskManager.getEpics().stream().peek(System.out::println);
        taskManager.getSubtasks().stream().peek(System.out::println);

        System.out.println("---------------------------------------");
        printAllTasks(taskManager);

        // ***********************************
        // Дополнительное задание из спринта 6
        // ***********************************

        TaskManager taskManagerSprint6 = managers.getDefault();

        // Создаем две задачи
        Task task1Sprint6 = new Task(1, "Очень интересная задача 1");
        Task task2Sprint6 = new Task(1, "Очень интересная задача 2");

        int task1IdSprint6 = taskManagerSprint6.addTask(task1Sprint6);
        int task2IdSprint6 = taskManagerSprint6.addTask(task2Sprint6);

        // Создаем эпик с тремя подзадачами
        Task taskToEpic1Sprint6 = new Task(1, "Задача-эпик 1");
        Epic epic1Sprint6 = new Epic(taskToEpic1Sprint6);
        int epic1IdSprint6 = taskManagerSprint6.addEpic(epic1Sprint6);

        Task taskToSubtask1taskManagerSprint6 = new Task(1, "Подзадача 1");
        Task taskToSubtask2taskManagerSprint6 = new Task(1, "Подзадача 2");
        Task taskToSubtask3taskManagerSprint6 = new Task(1, "Подзадача 3");
        Subtask subtask1Sprint6 = new Subtask(taskToSubtask1taskManagerSprint6, epic1IdSprint6);
        Subtask subtask2Sprint6 = new Subtask(taskToSubtask2taskManagerSprint6, epic1IdSprint6);
        Subtask subtask3Sprint6 = new Subtask(taskToSubtask3taskManagerSprint6, epic1IdSprint6);

        int subtask1IdSprint6 = taskManagerSprint6.addSubtask(subtask1Sprint6);
        int subtask2IdSprint6 = taskManagerSprint6.addSubtask(subtask2Sprint6);
        int subtask3IdSprint6 = taskManagerSprint6.addSubtask(subtask3Sprint6);

        // Создаем эпик без подзадач
        Task taskToEpic2Sprint6 = new Task(1, "Задача-эпик 2");
        Epic epic2Sprint6 = new Epic(taskToEpic2Sprint6);
        int epic2IdSprint6 = taskManagerSprint6.addEpic(epic2Sprint6);

        // Запрашиваем задачи и смотрим на историю
        taskManagerSprint6.getTask(task1IdSprint6);
        taskManagerSprint6.getTask(task2IdSprint6);
        taskManagerSprint6.getEpic(epic1IdSprint6);
        taskManagerSprint6.getEpic(epic2IdSprint6);
        taskManagerSprint6.getSubtask(subtask1IdSprint6);
        taskManagerSprint6.getSubtask(subtask3IdSprint6);

        System.out.println("Печатаем историю:");
        taskManagerSprint6.getHistory().stream().peek(System.out::println);

        taskManagerSprint6.getSubtask(subtask2IdSprint6);
        taskManagerSprint6.getTask(task1IdSprint6);
        taskManagerSprint6.getTask(task2IdSprint6);
        taskManagerSprint6.getEpic(epic1IdSprint6);
        taskManagerSprint6.getEpic(epic2IdSprint6);

        System.out.println("Печатаем историю:");
        taskManagerSprint6.getHistory().stream().peek(System.out::println);

        taskManagerSprint6.deleteTaskById(task1IdSprint6);
        taskManagerSprint6.deleteEpicById(epic2IdSprint6);
        taskManagerSprint6.deleteSubtaskById(subtask2IdSprint6);

        System.out.println("Печатаем историю:");
        taskManagerSprint6.getHistory().stream().peek(System.out::println);

        taskManagerSprint6.deleteEpicById(epic1IdSprint6);

        printAllTasks(taskManagerSprint6);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        manager.getTasks().stream().peek(System.out::println);

        System.out.println("Эпики:");
        manager.getEpics().stream().peek(System.out::println);

        System.out.println("Подзадачи:");
        manager.getSubtasks().stream().peek(System.out::println);

        System.out.println("История:");
        manager.getHistory().stream().peek(System.out::println);
    }
}
