package ru.yandex.javacource.bochkareva.schedule.manager;

import ru.yandex.javacource.bochkareva.schedule.task.*;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int taskCounter;

    public TaskManager() {
        taskCounter = 0;
    }

    public Integer addTask(Task task) {
        if (task == null) {
            return null;
        }
        int id = ++taskCounter;
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    public Integer addEpic(Epic epic) {
        if (epic == null) {
            return null;
        }
        int id = ++taskCounter;
        epic.clear();
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    public Integer addSubtask(Subtask subtask) {
        if (subtask == null) {
            return null;
        }
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return null;
        }
        int id = ++taskCounter;
        subtask.setId(id);
        subtasks.put(id, subtask);
        epic.addSubtask(id);
        updateEpic(epic.getId());
        return id;
    }

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    public void deleteEpicById(int id) {
        final Epic epic = epics.remove(id);
        if (epic == null) {
            return;
        }
        for (int subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return;
        }
        int epicId = subtask.getEpicId();
        epics.get(epicId).deleteTaskById(id);
        subtasks.remove(id);
        updateEpic(epicId);
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void clearEpics() {
        subtasks.clear();
        epics.clear();
    }

    public void clearSubtasks() {
        for (Epic epic : epics.values()) {
            epic.clear();
            updateEpic(epic.getId());
        }
        subtasks.clear();
    }

    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public ArrayList<Subtask> getSubtasksOfEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return null;
        }
        ArrayList<Subtask> result = new ArrayList<>();
        for (int subtaskId : epic.getSubtaskIds()) {
            result.add(subtasks.get(subtaskId));
        }
        return result;
    }

    public void updateTask(Task task) {
        if (task == null) {
            return;
        }
        int id = task.getId();
        Task savedTask = tasks.get(id);
        if (savedTask == null) {
            return;
        }
        tasks.put(id, task);
    }

    public void updateEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        int id = epic.getId();
        Epic savedEpic = epics.get(id);
        if (savedEpic == null) {
            return;
        }
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
    }

    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        int id = subtask.getId();
        int epicId = subtask.getEpicId();
        Subtask savedSubtask = subtasks.get(id);
        if (savedSubtask == null) {
            return;
        }
        final Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }
        subtasks.put(id, subtask);
        updateEpic(epicId);
    }

    private void updateEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return;
        }
        int doneSubtasks = 0;
        int newSubtasks = 0;
        ArrayList<Subtask> subtasks = getSubtasksOfEpic(id);
        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() == TaskStatus.DONE) {
                doneSubtasks += 1;
            } else if (subtask.getStatus() == TaskStatus.NEW) {
                newSubtasks += 1;
            }
        }
        if (newSubtasks == subtasks.size()) {
            epic.setStatus(TaskStatus.NEW);
        } else if (doneSubtasks == subtasks.size()) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}
