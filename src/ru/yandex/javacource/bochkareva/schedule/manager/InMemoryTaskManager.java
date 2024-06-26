package ru.yandex.javacource.bochkareva.schedule.manager;

import ru.yandex.javacource.bochkareva.schedule.exceptions.TaskValidationException;
import ru.yandex.javacource.bochkareva.schedule.task.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(new Comparator<Task>() {
        @Override
        public int compare(Task task1, Task task2) {
            return task1.getStartTime().compareTo(task2.getStartTime());
        }
    });
    protected final HistoryManager historyManager = new InMemoryHistoryManager();
    protected int taskCounter;

    public InMemoryTaskManager() {
        taskCounter = 0;
    }

    private boolean isIntersection(Task task1, Task task2) {
        final LocalDateTime start1 = task1.getStartTime();
        final LocalDateTime end1 = task1.getEndTime();
        final LocalDateTime start2 = task2.getStartTime();
        final LocalDateTime end2 = task2.getEndTime();


        if (start1 == null || start2 == null) {
            return false;
        }
        if (end1 == null && end2 == null) {
            return true;
        }
        if (end1 == null) {
            return start1.isBefore(end2);
        }
        if (end2 == null) {
            return start2.isBefore(end1);
        }
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private boolean validateTask(Task task) {
        List<Task> tasks = getPrioritizedTasks();
        Optional<Task> result = tasks.stream()
            .filter(prioritizedTask -> prioritizedTask.getId() != task.getId())
            .filter(prioritizedTask -> isIntersection(prioritizedTask, task))
            .findAny();
        return result.isPresent();
    }

    @Override
    public Integer addTask(Task task) {
        if (task == null) {
            return null;
        }
        if (validateTask(task)) {
            throw new TaskValidationException("Невозможно добавить задачу с интервалом начало: "
                    + task.getStartTime() + ", конец: " + task.getEndTime());
        }
        int id = ++taskCounter;
        task.setId(id);
        tasks.put(id, task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
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
        if (validateTask(subtask)) {
            throw new TaskValidationException("Невозможно добавить задачу с интервалом начало: "
                    + subtask.getStartTime() + ", конец: " + subtask.getEndTime());
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
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        return id;
    }

    @Override
    public void deleteTaskById(int id) {
        final Task task = getTaskById(id);
        if (task.getStartTime() != null) {
            prioritizedTasks.remove(task);
        }
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        final Epic epic = epics.remove(id);
        if (epic == null) {
            return;
        }
        epic.getSubtaskIds().stream()
            .peek(subtasks::remove)
            .forEach(historyManager::remove);
        epics.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return;
        }
        if (subtask.getStartTime() != null) {
            prioritizedTasks.remove(subtask);
        }
        int epicId = subtask.getEpicId();
        epics.get(epicId).deleteTaskById(id);
        subtasks.remove(id);
        updateEpic(epicId);
        historyManager.remove(id);
    }

    @Override
    public void clearTasks() {
        tasks.values().stream().forEach(prioritizedTasks::remove);
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void clearSubtasks() {
        subtasks.values().stream().forEach(prioritizedTasks::remove);
        epics.values().stream()
            .peek(Epic::clear)
            .map(Task::getId)
            .forEach(this::updateEpic);
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
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream().toList();
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return null;
        }
        return epic.getSubtaskIds().stream()
            .map(subtasks::get)
            .toList();
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) {
            return;
        }
        if (validateTask(task)) {
            throw new TaskValidationException("Невозможно добавить задачу с интервалом начало: "
                    + task.getStartTime() + ", конец: " + task.getEndTime());
        }
        int id = task.getId();
        Task savedTask = tasks.get(id);
        if (savedTask == null) {
            return;
        }
        tasks.put(id, task);
        prioritizedTasks.remove(task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
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
        if (validateTask(subtask)) {
            throw new TaskValidationException("Невозможно добавить задачу с интервалом начало: "
                    + subtask.getStartTime() + ", конец: " + subtask.getEndTime());
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
        prioritizedTasks.remove(subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
    }

    private void updateEpicStatus(Epic epic) {
        List<Subtask> subtasks = getSubtasksOfEpic(epic.getId());

        int doneSubtasks = (int) subtasks.stream().filter(subtask -> subtask.getStatus() == TaskStatus.DONE).count();
        int newSubtasks = (int) subtasks.stream().filter(subtask -> subtask.getStatus() == TaskStatus.NEW).count();

        if (newSubtasks == subtasks.size()) {
            epic.setStatus(TaskStatus.NEW);
        } else if (doneSubtasks == subtasks.size()) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private void updateEpicDuration(Epic epic) {
        List<Subtask> subtasks = getSubtasksOfEpic(epic.getId());

        subtasks.stream()
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .sorted()
                .findFirst()
                .ifPresent(epic::setStartTime);

        subtasks.stream()
                .map(Task::getEndTime)
                .filter(Objects::nonNull)
                .sorted(Comparator.reverseOrder())
                .findFirst()
                .ifPresent(epic::setEndTime);

        epic.setDuration(subtasks.stream()
                .map(Task::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus));
    }

    private void updateEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return;
        }
        updateEpicStatus(epic);
        updateEpicDuration(epic);
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            return null;
        }
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return null;
        }
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return null;
        }
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
