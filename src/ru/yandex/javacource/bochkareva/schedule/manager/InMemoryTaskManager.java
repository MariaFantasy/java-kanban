package ru.yandex.javacource.bochkareva.schedule.manager;

import ru.yandex.javacource.bochkareva.schedule.task.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final TreeSet<Integer> prioritizedTasks = new TreeSet<>(new Comparator<Integer>() {
        @Override
        public int compare(Integer id1, Integer id2) {
            final Task task1 = getTaskById(id1).get();
            final Task task2 = getTaskById(id2).get();
            return task1.getStartTime().compareTo(task2.getStartTime());
        }
    });
    protected final HistoryManager historyManager = new InMemoryHistoryManager();
    protected int taskCounter;

    public InMemoryTaskManager() {
        taskCounter = 0;
    }

    private void addTaskToPrioritizedTasks(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            task = epics.get(id);
            if (task == null) {
                task = subtasks.get(id);
            }
        }
        if (task == null) {
            return;
        }

        if (task.getStartTime() != null) {
            prioritizedTasks.add(id);
        }
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
        if (end1 == null && start1.isBefore(end2)) {
            return true;
        }
        if (end2 == null && start2.isBefore(end1)) {
            return true;
        }
        if (start1.isBefore(end2) && start2.isBefore(end1)) {
            return true;
        }
        return false;
    }

    private boolean validateTask(Task task) {
        List<Task> tasks = getPrioritizedTasks();
        Optional<Task> result = tasks.stream()
            .filter(prioritizedTask -> isIntersection(prioritizedTask, task))
            .findAny();
        return result.isPresent();
    }

    @Override
    public Integer addTask(Task task) {
        if (task == null) {
            return null;
        }
        try {
            if (validateTask(task)) {
                throw new Exception("Задачи пересекаются");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;
        }
        int id = ++taskCounter;
        task.setId(id);
        tasks.put(id, task);
        addTaskToPrioritizedTasks(id);
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
        try {
            if (validateTask(subtask)) {
                throw new Exception("Задачи пересекаются");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;
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
        addTaskToPrioritizedTasks(id);
        return id;
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
        prioritizedTasks.remove(id);
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
        int epicId = subtask.getEpicId();
        epics.get(epicId).deleteTaskById(id);
        subtasks.remove(id);
        updateEpic(epicId);
        historyManager.remove(id);
        prioritizedTasks.remove(id);
    }

    @Override
    public void clearTasks() {
        tasks.keySet().stream().forEach(prioritizedTasks::remove);
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void clearSubtasks() {
        subtasks.keySet().stream().forEach(prioritizedTasks::remove);
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

    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream()
                .map(this::getTaskById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
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
        try {
            if (validateTask(task)) {
                throw new Exception("Задачи пересекаются");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        int id = task.getId();
        Task savedTask = tasks.get(id);
        if (savedTask == null) {
            return;
        }
        tasks.put(id, task);
        prioritizedTasks.remove(id);
        addTaskToPrioritizedTasks(id);
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
        try {
            if (validateTask(subtask)) {
                throw new Exception("Задачи пересекаются");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
        prioritizedTasks.remove(id);
        addTaskToPrioritizedTasks(id);
    }

    private void updateEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return;
        }
        List<Subtask> subtasks = getSubtasksOfEpic(id);

        int doneSubtasks = (int) subtasks.stream().filter(subtask -> subtask.getStatus() == TaskStatus.DONE).count();
        int newSubtasks = (int) subtasks.stream().filter(subtask -> subtask.getStatus() == TaskStatus.NEW).count();

        if (newSubtasks == subtasks.size()) {
            epic.setStatus(TaskStatus.NEW);
        } else if (doneSubtasks == subtasks.size()) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }

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

    @Override
    public Optional<Task> getTask(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            return Optional.empty();
        }
        historyManager.add(task);
        return Optional.of(task);
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return Optional.empty();
        }
        historyManager.add(epic);
        return Optional.of(epic);
    }

    @Override
    public Optional<Subtask> getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return Optional.empty();
        }
        historyManager.add(subtask);
        return Optional.of(subtask);
    }

    @Override
    public Optional<Task> getTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            task = epics.get(id);
            if (task == null) {
                task = subtasks.get(id);
                if (task == null) {
                    return Optional.empty();
                }
            }
        }
        historyManager.add(task);
        return Optional.of(task);
    }

    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
