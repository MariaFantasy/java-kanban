package task;

import org.jetbrains.annotations.NotNull;

public class Subtask extends Task {
    private final Epic parentTask;

    public Subtask(@NotNull Task task, @NotNull Epic parentTask){
        super(task);
        this.setType(TaskType.SUBTASK);
        this.parentTask=parentTask;
        parentTask.addTask(this);
    }

    public Subtask(int id, String name, @NotNull Epic parentTask){
        super(id, name);
        super.setType(TaskType.SUBTASK);
        this.parentTask=parentTask;
        parentTask.addTask(this);
    }

    public Subtask(int id, String name, String description, @NotNull Epic parentTask){
        super(id, name, description);
        super.setType(TaskType.SUBTASK);
        this.parentTask=parentTask;
        parentTask.addTask(this);
    }

    public Subtask(int id, String name, TaskStatus status, @NotNull Epic parentTask){
        super(id, name, status);
        super.setType(TaskType.SUBTASK);
        this.parentTask=parentTask;
        parentTask.addTask(this);
    }

    public Subtask(int id, String name, String description, TaskStatus status, @NotNull Epic parentTask){
        super(id, name, description, status);
        super.setType(TaskType.SUBTASK);
        this.parentTask=parentTask;
        parentTask.addTask(this);
    }

    public Epic getParentTask(){
        return parentTask;
    }

    @Override
    public void setStatus(TaskStatus status){
        super.setStatus(status);
        parentTask.setStatus(status);
    }

    @Override
    public String toString(){
        String result="Subtask{" +
                "id='" + super.getId() + '\'' +
                ", parentTaskId='" + parentTask.getId() + '\'' +
                ", name='" + super.getName() + '\'' +
                ", status='" + super.getStatus().toString() + '\'';

        if (super.getDescription() != null) {
            result=result + ", description.length=" + super.getDescription().length();
        } else {
            result=result + ", description=null";
        }

        return result + '}';
    }
}
