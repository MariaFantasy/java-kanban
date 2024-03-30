package ru.yandex.javacource.bochkareva.schedule;

import ru.yandex.javacource.bochkareva.schedule.task.*;
import ru.yandex.javacource.bochkareva.schedule.manager.TaskManager;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

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

        for (Task task : taskManager.getTasks()) {
            System.out.println(task);
        }
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);
        }
        for (Subtask subtask : taskManager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("---------------------------------------");
        System.out.println("Обновление статусов:");

        task1.setStatus(TaskStatus.IN_PROGRESS);
        task2.setStatus(TaskStatus.DONE);
        taskManager.updateTask(task1);
        taskManager.updateTask(task2);
        for (Task task : taskManager.getTasks()) {
            System.out.println(task);
        }

        System.out.println("--------------");

        epic1.setStatus(TaskStatus.IN_PROGRESS);
        epic2.setStatus(TaskStatus.DONE);
        taskManager.updateEpic(epic1);
        taskManager.updateEpic(epic2);
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);
        }

        System.out.println("--------------");
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);
        }

        System.out.println("--------------");
        subtask2.setStatus(TaskStatus.DONE);
        subtask3.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask2);
        taskManager.updateSubtask(subtask3);
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);
        }
        for (Subtask subtask : taskManager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("---------------------------------------");
        System.out.println("Удаление задач:");
        taskManager.deleteTaskById(1);
        for (Task task : taskManager.getTasks()) {
            System.out.println(task);
        }

        System.out.println("---------------------------------------");
        System.out.println("Удаление эпиков:");
        taskManager.deleteEpicById(6);
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);
        }
        for (Subtask subtask : taskManager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("---------------------------------------");
        System.out.println("Удаление подзадач:");
        taskManager.deleteSubtaskById(4);
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);
        }
        for (Subtask subtask : taskManager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("---------------------------------------");
        System.out.println("Удаление несуществующих задач, эпиков и подзадач:");
        taskManager.deleteTaskById(10);
        taskManager.deleteEpicById(10);
        taskManager.deleteSubtaskById(10);
        for (Task task : taskManager.getTasks()) {
            System.out.println(task);
        }
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);
        }
        for (Subtask subtask : taskManager.getSubtasks()) {
            System.out.println(subtask);
        }
    }
}
