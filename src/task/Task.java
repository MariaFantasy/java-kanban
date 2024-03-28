package task;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Task {
    private final int id;
    private String name;
    private String description;
    private TaskStatus status;
    private TaskType type;

    public Task(@NotNull Task task){
        this.id=task.getId();
        this.name=task.getName();
        this.description=task.getDescription();
        this.status=task.getStatus();
        this.type=TaskType.COMMON;
    }

    public Task(int id, String name){
        this.id=id;
        this.name=name;
        this.status=TaskStatus.NEW;
        this.type=TaskType.COMMON;
    }

    public Task(int id, String name, String description){
        this(id, name);
        this.description=description;
    }

    public Task(int id, String name, TaskStatus status){
        this(id, name);
        this.status=status;
    }

    public Task(int id, String name, String description, TaskStatus status){
        this(id, name, status);
        this.description=description;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public TaskStatus getStatus(){
        return status;
    }

    public TaskType getType(){
        return type;
    }

    public void setName(String name){
        this.name=name;
    }

    public void setDescription(String description){
        this.description=description;
    }

    public void setStatus(TaskStatus status){
        if (this.type != TaskType.COMMON) return;
        this.status=status;
    }

    protected void setType(TaskType type){
        this.type=type;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        Task otherTask=(Task) o;
        return (this.id == otherTask.id);
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(id);
    }

    @Override
    public String toString(){
        String result="Task{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status.toString() + '\'';

        if (description != null) {
            result=result + ", description.length=" + description.length();
        } else {
            result=result + ", description=null";
        }

        return result + '}';
    }
}
