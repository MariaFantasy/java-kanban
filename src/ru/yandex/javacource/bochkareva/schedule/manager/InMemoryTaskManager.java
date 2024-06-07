package ru.yandex.javacource.bochkareva.schedule.manager;

import ru.yandex.javacource.bochkareva.schedule.task.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final TreeSet<Integer> proritizedTasks = new TreeSet<>(new Comparator<Integer>() {
        @Override
        public int compare(Integer id1, Integer id2) {
            final Task task1 = getTaskById(id1);
            final Task task2 = getTaskById(id2);
            return task1.getStartTime().compareTo(task2.getStartTime());
        }
    });
    protected final HistoryManager historyManager = new InMemoryHistoryManager();
    protected int taskCounter;

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
        proritizedTasks.add(id);
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
        proritizedTasks.add(id);
        return id;
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
        proritizedTasks.remove(id);
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
        proritizedTasks.remove(id);
    }

    @Override
    public void clearTasks() {
        for (Integer taskId : tasks.keySet()) {
            proritizedTasks.remove(taskId);
        }
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void clearSubtasks() {
        for (Integer taskId : subtasks.keySet()) {
            proritizedTasks.remove(taskId);
        }
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
        proritizedTasks.remove(id);
        proritizedTasks.add(id);
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
        proritizedTasks.remove(id);
        proritizedTasks.add(id);
    }

    private void updateEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return;
        }
        int doneSubtasks = 0;
        int newSubtasks = 0;
        ArrayList<Subtask> subtasks = getSubtasksOfEpic(id);
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        Duration duration = Duration.ZERO;
        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() == TaskStatus.DONE) {
                doneSubtasks += 1;
            } else if (subtask.getStatus() == TaskStatus.NEW) {
                newSubtasks += 1;
            }
            final LocalDateTime subtaskStartTime = subtask.getStartTime();
            if (subtaskStartTime != null) {
                if (startTime == null) {
                    startTime = subtaskStartTime;
                } else if (startTime.isAfter(subtaskStartTime)) {
                    startTime = subtaskStartTime;
                }
                final LocalDateTime subtaskEndTime = subtask.getEndTime();
                if (subtaskEndTime != null) {
                    if (endTime == null) {
                        endTime = subtaskEndTime;
                    } else if (endTime.isBefore(subtaskEndTime)) {
                        endTime = subtaskEndTime;
                    }
                    duration = duration.plus(subtask.getDuration());
                }
            }
        }
        if (newSubtasks == subtasks.size()) {
            epic.setStatus(TaskStatus.NEW);
        } else if (doneSubtasks == subtasks.size()) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
        epic.setStartTime(startTime);
        epic.setEndTime(endTime);
        epic.setDuration(duration);
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
