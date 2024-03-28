package task;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class Epic extends Task {
    private HashMap<Integer, Subtask> childTasks;
    private int newSubtasks;
    private int doneSubtasks;

    public Epic(@NotNull Task task){
        super(task);
        super.setStatus(TaskStatus.NEW);
        super.setType(TaskType.EPIC);
        childTasks=new HashMap<>();
        doneSubtasks=0;
        newSubtasks=0;
    }

    public Epic(int id, String name){
        super(id, name, TaskStatus.NEW);
        super.setType(TaskType.EPIC);
        childTasks=new HashMap<>();
        doneSubtasks=0;
        newSubtasks=0;
    }

    public Epic(int id, String name, String description){
        super(id, name, description, TaskStatus.NEW);
        super.setType(TaskType.EPIC);
        childTasks=new HashMap<>();
        doneSubtasks=0;
        newSubtasks=0;
    }

    public Epic(@NotNull Task task, @NotNull ArrayList<Subtask> subtasks){
        this(task);
        childTasks=new HashMap<>();
        addTasks(subtasks);
    }

    public Epic(int id, String name, @NotNull ArrayList<Subtask> subtasks){
        this(id, name);
        childTasks=new HashMap<>();
        addTasks(subtasks);
    }

    public Epic(int id, String name, String description, @NotNull ArrayList<Subtask> subtasks){
        this(id, name, description);
        childTasks=new HashMap<>();
        addTasks(subtasks);
    }

    public ArrayList<Integer> getSubtasksId(){
        ArrayList<Integer> result=new ArrayList<>();
        for (int id : childTasks.keySet()) {
            result.add(id);
        }
        return result;
    }

    public ArrayList<Subtask> getSubtasks(){
        ArrayList<Subtask> result=new ArrayList<>();
        for (Subtask subtask : childTasks.values()) {
            result.add(subtask);
        }
        return result;
    }

    private void updateStatus(){
        if (doneSubtasks == childTasks.size()) {
            super.setStatus(TaskStatus.DONE);
        } else if (newSubtasks + doneSubtasks < childTasks.size()) {
            super.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    public void addTask(Subtask subtask){
        if (subtask == null) return;
        if (this.getId() == subtask.getId()) return;

        childTasks.put(subtask.getId(), subtask);
        if (subtask.getStatus() == TaskStatus.NEW) {
            newSubtasks+=1;
        } else if (subtask.getStatus() == TaskStatus.DONE) {
            doneSubtasks+=1;
        }
        updateStatus();
    }

    public void addTasks(ArrayList<Subtask> subtasks){
        if (subtasks == null) return;
        for (Subtask childTask : subtasks) {
            addTask(childTask);
        }
    }

    public void deleteTaskById(int id){
        if (!childTasks.containsKey(id)) return;

        if (childTasks.get(id).getStatus() == TaskStatus.NEW) {
            newSubtasks-=1;
        } else if (childTasks.get(id).getStatus() == TaskStatus.DONE) {
            doneSubtasks-=1;
        }
        childTasks.remove(id);
        updateStatus();
    }

    @Override
    public void setStatus(TaskStatus status){
        newSubtasks=0;
        doneSubtasks=0;
        for (Subtask childTask : childTasks.values()) {
            if (childTask.getStatus() == TaskStatus.NEW) {
                newSubtasks+=1;
            } else if (childTask.getStatus() == TaskStatus.DONE) {
                doneSubtasks+=1;
            }
        }
        updateStatus();
    }

    @Override
    public String toString(){
        String result="Epic{" +
                "id='" + super.getId() + '\'' +
                ", name='" + super.getName() + '\'' +
                ", status='" + super.getStatus().toString() + '\'';

        if (super.getDescription() != null) {
            result=result + ", description.length=" + super.getDescription().length();
        } else {
            result=result + ", description=null";
        }

        if (!childTasks.isEmpty()) {
            result=result + ", childTasks.count=" + childTasks.size();
        } else {
            result=result + ", childTasks=null";
        }

        return result + '}';
    }
}
