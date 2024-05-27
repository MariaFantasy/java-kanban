package ru.yandex.javacource.bochkareva.schedule.task;

import java.util.ArrayList;

public class Epic extends Task implements Cloneable {
    private ArrayList<Integer> subtaskIds = new ArrayList<>();

    public Epic(Task task) {
        super(task);
    }

    public void setSubtaskIds(ArrayList<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
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
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        String result = super.getId() + ",EPIC," + super.getName() + "," + super.getStatus().toString();

        if (super.getDescription() != null) {
            result = result + "," + super.getDescription();
        } else {
            result = result + ",";
        }

        return result;
    }

    @Override
    public Epic clone() {
        return (Epic) super.clone();
    }
}
