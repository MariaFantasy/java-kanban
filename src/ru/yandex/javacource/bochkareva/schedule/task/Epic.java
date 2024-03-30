package ru.yandex.javacource.bochkareva.schedule.task;

import com.sun.jdi.ArrayReference;
import org.jetbrains.annotations.NotNull;

import java.sql.Array;
import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> childTasks = new ArrayList<>();

    public Epic(@NotNull Task task) {
        super(task);
    }

    public Epic(@NotNull Epic epic) {
        super(epic);
        addSubtasks(childTasks);
    }

    public ArrayList<Integer> getSubtasksId() {
        return childTasks;
    }

    public void addSubtask(int id) {
        if (childTasks.contains(id)) {
            return;
        }
        childTasks.add(id);
    }

    public void addSubtasks(ArrayList<Integer> subtasks) {
        if (subtasks == null) {
            return;
        }
        for (int childTaskId : subtasks) {
            addSubtask(childTaskId);
        }
    }

    public void deleteTaskById(Integer id) {
        childTasks.remove(id);
    }

    public void clear() {
        childTasks.clear();
    }

    @Override
    public String toString() {
        String result = "Epic{" + "id='" + super.getId() + '\'' + ", name='" + super.getName() + '\'' + ", status='" + super.getStatus().toString() + '\'';

        if (super.getDescription() != null) {
            result = result + ", description.length=" + super.getDescription().length();
        } else {
            result = result + ", description=null";
        }

        if (!childTasks.isEmpty()) {
            result = result + ", childTasks.count=" + childTasks.size();
        } else {
            result = result + ", childTasks=null";
        }

        return result + '}';
    }
}
