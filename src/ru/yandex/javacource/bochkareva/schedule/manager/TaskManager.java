package ru.yandex.javacource.bochkareva.schedule.manager;

import ru.yandex.javacource.bochkareva.schedule.task.*;

import java.util.ArrayList;

public interface TaskManager {

    public Integer addTask(Task task);

    public Integer addEpic(Epic epic);

    public Integer addSubtask(Subtask subtask);

    public void deleteTaskById(int id);

    public void deleteEpicById(int id);

    public void deleteSubtaskById(int id);

    public void clearTasks();

    public void clearEpics();

    public void clearSubtasks();

    public ArrayList<Task> getTasks();

    public ArrayList<Epic> getEpics();

    public ArrayList<Subtask> getSubtasks();

    public ArrayList<Subtask> getSubtasksOfEpic(int id);

    public void updateTask(Task task);

    public void updateEpic(Epic epic);

    public void updateSubtask(Subtask subtask);

    public Task getTask(int id);
    public Epic getEpic(int id);
    public Subtask getSubtask(int id);
    public Task getTaskById(int id);
    public ArrayList<Task> getHistory();
}
