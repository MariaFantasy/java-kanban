package ru.yandex.javacource.bochkareva.schedule.task;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Epic extends Task implements Cloneable {
    private ArrayList<Integer> subtaskIds = new ArrayList<>();

    public Epic(@NotNull Task task) {
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

        return result + '}';
    }

    @Override
    public Epic clone() {
        Epic clone = (Epic) super.clone();
        return clone;
    }
}
