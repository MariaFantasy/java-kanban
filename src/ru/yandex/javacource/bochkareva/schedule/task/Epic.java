package ru.yandex.javacource.bochkareva.schedule.task;

import com.sun.jdi.ArrayReference;
import org.jetbrains.annotations.NotNull;

import java.sql.Array;
import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtaskIds = new ArrayList<>();

    public Epic(@NotNull Task task) {
        super(task);
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtask(int id) {
        if (subtaskIds.contains(id)) {
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
}
