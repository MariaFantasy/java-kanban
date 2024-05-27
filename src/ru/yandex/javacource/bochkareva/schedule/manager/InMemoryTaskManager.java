package ru.yandex.javacource.bochkareva.schedule.manager;

import ru.yandex.javacource.bochkareva.schedule.task.*;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = new InMemoryHistoryManager();
    private int taskCounter;

    public InMemoryTaskManager() {
        taskCounter = 0;
    }

    @Override
    public Integer addTask(Task task) {
        if (task == null) {
            return null;
        }
        int id = ++taskCounter;
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    @Override
    public Integer addEpic(Epic epic) {
        if (epic == null) {
            return null;
        }
        int id = ++taskCounter;
        epic.clear();
        epic.setId(id);
        Epic newEpic = new Epic(epic);
        epics.put(id, newEpic);
        return id;
    }

    @Override
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
        Subtask newSubtask = new Subtask(subtask, epic.getId());
        subtasks.put(id, newSubtask);
        epic.addSubtask(id);
        updateEpic(epic.getId());
        return id;
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        final Epic epic = epics.remove(id);
        if (epic == null) {
            return;
        }
        for (int subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        }
        epics.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return;
        }
        int epicId = subtask.getEpicId();
        epics.get(epicId).deleteTaskById(id);
        subtasks.remove(id);
        updateEpic(epicId);
        historyManager.remove(id);
    }

    @Override
    public void clearTasks() {
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void clearSubtasks() {
        for (Epic epic : epics.values()) {
            epic.clear();
            updateEpic(epic.getId());
        }
        subtasks.clear();
    }

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
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

    @Override
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

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        int id = epic.getId();
        final Epic savedEpic = epics.get(id);
        if (savedEpic == null) {
            return;
        }
        epic.setSubtaskIds(savedEpic.getSubtaskIds());
        epic.setStatus(savedEpic.getStatus());
        epics.put(id, epic);
    }

    @Override
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

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            task = epics.get(id);
            if (task == null) {
                task = subtasks.get(id);
                if (task == null) {
                    return null;
                }
            }
        }
        historyManager.add(task);
        return task;
    }

    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
