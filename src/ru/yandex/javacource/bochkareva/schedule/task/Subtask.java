package ru.yandex.javacource.bochkareva.schedule.task;

public class Subtask extends Task implements Cloneable {
    private int epicId;

    public Subtask(Task task, int epicId) {
        super(task);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, TaskStatus status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        String result = "Subtask{" + "id='" + super.getId() + '\'' + ", parentTaskId='" + epicId + '\'' + ", name='" + super.getName() + '\'' + ", status='" + super.getStatus().toString() + '\'';

        if (super.getDescription() != null) {
            result = result + ", description.length=" + super.getDescription().length();
        } else {
            result = result + ", description=null";
        }

        result = result + ", startTime=" + getStartTime();
        result = result + ", endTime=" + getEndTime();
        result = result + ", duration=" + getDuration();

        return result + '}';
    }

    @Override
    public Subtask clone() {
        return (Subtask) super.clone();
    }
}
