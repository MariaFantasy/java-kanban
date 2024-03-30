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
        task.setId(++taskCounter);
        tasks.put(taskCounter, task);
        return taskCounter;
    }

    public Integer addEpic(Epic epic) {
        if (epic == null) {
            return null;
        }
        epic.clear();
        epic.setId(++taskCounter);
        epics.put(taskCounter, epic);
        updateEpic(taskCounter);
        return taskCounter;
    }

    public Integer addSubtask(Subtask subtask) {
        if (subtask == null) {
            return null;
        }
        Epic epic = epics.get(subtask.getParentTaskId());
        if (epic == null) {
            return null;
        }
        subtask.setId(++taskCounter);
        subtasks.put(taskCounter, subtask);
        epic.addSubtask(taskCounter);
        updateEpic(epic.getId());
        return taskCounter;
    }

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    public void deleteEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return;
        }
        ArrayList<Integer> subtasksId = (ArrayList<Integer>) epic.getSubtasksId().clone();
        for (int subtaskId : subtasksId) {
            epic.deleteTaskById(subtaskId);
            deleteSubtaskById(subtaskId);
        }
        epics.remove(id);
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return;
        }
        int epicId = subtask.getParentTaskId();
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
        for (int subtaskId : subtasks.keySet()) {
            deleteSubtaskById(subtaskId);
        }
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

    public ArrayList<Integer> getSubtasksOfEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return null;
        }
        return epic.getSubtasksId();
    }

    public void updateEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return;
        }
        int doneSubtasks = 0;
        int newSubtasks = 0;
        ArrayList<Integer> subtasksId = getSubtasksOfEpic(id);
        for (int subtaskId : subtasksId) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask.getStatus() == TaskStatus.DONE) {
                doneSubtasks += 1;
            } else if (subtask.getStatus() == TaskStatus.NEW) {
                newSubtasks += 1;
            }
        }
        if (newSubtasks == subtasksId.size()) {
            epic.setStatus(TaskStatus.NEW);
        } else if (doneSubtasks == subtasksId.size()) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    public void updateTask(Task task) {
        if (task == null) {
            return;
        }
        if (tasks.get(task.getId()) == null) {
            return;
        }
        tasks.put(task.getId(), task);
    }

    public void updateEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        Epic oldEpic = epics.get(epic.getId());
        if (oldEpic == null) {
            return;
        }
        ArrayList<Integer> newSubtasks = epic.getSubtasksId();
        for (int subtaskId : newSubtasks) {
            if (!subtasks.containsKey(subtaskId)) {
                epic.deleteTaskById(subtaskId);
            }
        }
        for (int subtaskId : oldEpic.getSubtasksId()) {
            if (!newSubtasks.contains(subtaskId)) {
                deleteSubtaskById(subtaskId);
            }
        }
        epics.put(epic.getId(), epic);
        updateEpic(epic.getId());
    }

    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        if (subtasks.get(subtask.getId()) == null) {
            return;
        }
        subtasks.put(subtask.getId(), subtask);
        updateEpic(subtask.getParentTaskId());
    }
}
