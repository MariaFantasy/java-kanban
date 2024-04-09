package ru.yandex.javacource.bochkareva.schedule.task;

import org.jetbrains.annotations.NotNull;

public class Subtask extends Task implements Cloneable {
    private int epicId;

    public Subtask(@NotNull Task task, int epicId) {
        super(task);
        this.epicId = epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        String result = "Subtask{" + "id='" + super.getId() + '\'' + ", parentTaskId='" + epicId + '\'' + ", name='" + super.getName() + '\'' + ", status='" + super.getStatus().toString() + '\'';

        if (super.getDescription() != null) {
            result = result + ", description.length=" + super.getDescription().length();
        } else {
            result = result + ", description=null";
        }

        return result + '}';
    }

    @Override
    public Subtask clone() {
        Subtask clone = (Subtask) super.clone();
        return clone;
    }
}
