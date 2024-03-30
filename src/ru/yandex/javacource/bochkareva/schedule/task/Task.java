package ru.yandex.javacource.bochkareva.schedule.task;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Task {
    private int id;
    private String name;
    private String description;
    private TaskStatus status;

    public Task(@NotNull Task task) {
        this.id = task.getId();
        this.name = task.getName();
        this.description = task.getDescription();
        this.status = task.getStatus();
    }

    public Task(int id, String name) {
        this.id = id;
        this.name = name;
        this.status = TaskStatus.NEW;
    }

    public Task(int id, String name, String description) {
        this(id, name);
        this.description = description;
    }

    public Task(int id, String name, TaskStatus status) {
        this(id, name);
        this.status = status;
    }

    public Task(int id, String name, String description, TaskStatus status) {
        this(id, name, status);
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        Task otherTask = (Task) o;
        return (this.id == otherTask.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        String result = "Task{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", status='" + status.toString() + '\'';

        if (description != null) {
            result = result + ", description.length=" + description.length();
        } else {
            result = result + ", description=null";
        }

        return result + '}';
    }
}
