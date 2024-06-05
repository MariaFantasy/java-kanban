package ru.yandex.javacource.bochkareva.schedule.task;

public class CSVTaskFormat {

    public static String toString(Task task) {
        return task.getId() + ","
                + task.getType() + ","
                + task.getName() + ","
                + task.getStatus() + ","
                + task.getDescription() + ","
                + (task.getType().equals(TaskType.SUBTASK) ? ((Subtask) task).getEpicId() : "") + ","
                + (!task.getStatus().equals(TaskStatus.NEW) ? task.getStartTime() : "null") + ","
                + (task.getStatus().equals(TaskStatus.DONE) ? task.getEndTime() : "null") + ","
                + (task.getStatus().equals(TaskStatus.DONE) ? task.getDuration() : "null");
    }
}
