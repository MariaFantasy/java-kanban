package ru.yandex.javacource.bochkareva.schedule.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task implements Cloneable {
    private ArrayList<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(Task task) {
        super(task);
    }

    public Epic(int id, String name, String description, TaskStatus status) {
        super(id, name, description, status);
    }

    public void setSubtaskIds(ArrayList<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtask(int id) {
        if (super.getId() == id) {
            return;
        }
        subtaskIds.add(id);
    }

    public void addSubtasks(ArrayList<Integer> subtasks) {
        if (subtasks == null) {
            return;
        }
        for (int subtaskId : subtasks) {
            addSubtask(subtaskId);
        }
    }

    public void deleteTaskById(Integer id) {
        subtaskIds.remove(id);
    }

    public void clear() {
        subtaskIds.clear();
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        String result = "Epic{" + "id='" + super.getId() + '\'' + ", name='" + super.getName() + '\'' + ", status='" + super.getStatus().toString() + '\'';

        if (super.getDescription() != null) {
            result = result + ", description.length=" + super.getDescription().length();
        } else {
            result = result + ", description=null";
        }

        if (!subtaskIds.isEmpty()) {
            result = result + ", subtasks.count=" + subtaskIds.size();
        } else {
            result = result + ", subtasks=null";
        }

        result = result + ", startTime=" + getStartTime();
        result = result + ", endTime=" + getEndTime();
        result = result + ", duration=" + getDuration();

        return result + '}';
    }

    @Override
    public Epic clone() {
        return (Epic) super.clone();
    }
}
