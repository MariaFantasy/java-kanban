import task.*;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    HashMap<Integer, Task> tasks;
    private int taskCounter;

    TaskManager(){
        tasks=new HashMap<Integer, Task>();
        taskCounter=0;
    }

    public void createTask(Task task){
        if (task == null) return;
        Task newTask=new Task(++taskCounter, task.getName(), task.getDescription(), task.getStatus());
        tasks.put(taskCounter, newTask);
    }

    public void createTask(String name){
        Task task=new Task(++taskCounter, name);
        tasks.put(taskCounter, task);
    }

    public void createTask(String name, String description){
        Task task=new Task(++taskCounter, name, description);
        tasks.put(taskCounter, task);
    }

    public void createTask(String name, TaskStatus status){
        Task task=new Task(++taskCounter, name, status);
        tasks.put(taskCounter, task);
    }

    public void createTask(String name, String description, TaskStatus status){
        Task task=new Task(++taskCounter, name, description, status);
        tasks.put(taskCounter, task);
    }

    public void createEpic(Task task){
        if (task == null) return;
        Epic epicTask=new Epic(++taskCounter, task.getName(), task.getDescription());
        tasks.put(taskCounter, epicTask);
    }

    public void createEpic(Epic epicTask){
        if (epicTask == null) return;
        Epic task=new Epic(++taskCounter, epicTask.getName(), epicTask.getDescription());
        for (Subtask subtask : epicTask.getSubtasks()) {
            this.createSubtask(subtask, task);
        }
        tasks.put(task.getId(), task);
    }

    public void createEpic(String name){
        Epic task=new Epic(++taskCounter, name);
        tasks.put(taskCounter, task);
    }

    public void createEpic(String name, String description){
        Task task=new Epic(++taskCounter, name, description);
        tasks.put(taskCounter, task);
    }

    public void createSubtask(Subtask task, Epic parentTask){
        if (task == null) return;
        if (parentTask == null) return;
        Subtask newTask=new Subtask(++taskCounter, task.getName(), task.getDescription(), task.getStatus(), parentTask);
        tasks.put(taskCounter, newTask);
    }

    public void createSubtask(String name, Epic parentTask){
        if (parentTask == null) return;
        Subtask task=new Subtask(++taskCounter, name, parentTask);
        tasks.put(taskCounter, task);
    }

    public void createSubtask(String name, String description, Epic parentTask){
        if (parentTask == null) return;
        if (parentTask.getType() == TaskType.SUBTASK) return;
        Subtask task=new Subtask(++taskCounter, name, description, parentTask);
        tasks.put(taskCounter, task);
    }

    public void createSubtask(String name, TaskStatus status, Epic parentTask){
        if (parentTask == null) return;
        if (parentTask.getType() == TaskType.SUBTASK) return;
        Subtask task=new Subtask(++taskCounter, name, status, parentTask);
        tasks.put(taskCounter, task);
    }

    public void createSubtask(String name, String description, TaskStatus status, Epic parentTask){
        if (parentTask == null) return;
        if (parentTask.getType() == TaskType.SUBTASK) return;
        Subtask task=new Subtask(++taskCounter, name, description, status, parentTask);
        tasks.put(taskCounter, task);
    }

    public void deleteTaskById(int id){
        Task task=findTaskById(id);
        if (task.getType() == TaskType.SUBTASK) {
            Subtask subtask=(Subtask) task;
            subtask.getParentTask().deleteTaskById(id);
        } else if (task.getType() == TaskType.EPIC) {
            Epic epicTask=(Epic) task;
            for (int subtaskId : epicTask.getSubtasksId()) {
                deleteTaskById(subtaskId);
            }
        }
        tasks.remove(id);
    }

    public void clear(){
        tasks.clear();
        taskCounter=0;
    }

    public ArrayList<Task> getListOfTasks(){
        ArrayList<Task> result=new ArrayList<>();
        for (Task task : tasks.values()) {
            result.add(task);
        }
        return result;
    }

    public ArrayList<Epic> getListOfEpics(){
        ArrayList<Epic> result=new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getType() == TaskType.EPIC) {
                result.add((Epic) task);
            }
        }
        return result;
    }

    public ArrayList<Subtask> getSubtasksOfEpic(int id){
        if (findTaskById(id).getType() == TaskType.EPIC) return ((Epic) findTaskById(id)).getSubtasks();
        return null;
    }

    public Task findTaskById(int id){
        return tasks.get(id);
    }

    public ArrayList<Task> findTaskByKeyWord(String keyWord){
        ArrayList<Task> result=new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getName().contains(keyWord)) {
                result.add(task);
            }
        }
        return result;
    }


    public void changeTaskName(int id, String name){
        Task task=this.findTaskById(id);
        task.setName(name);
    }

    public void changeTaskDescription(int id, String description){
        Task task=this.findTaskById(id);
        task.setDescription(description);
    }

    public void changeTaskStatus(int id, TaskStatus status){
        Task task=this.findTaskById(id);
        task.setStatus(status);
    }

    public void changeTaskType(int id, TaskType type){
        Task task=this.findTaskById(id);
        if (type == task.getType()) return;
        if (task.getType() == TaskType.SUBTASK) {
            // Нельзя сменить статус на подзадачу, без указания линка родителя
            return;
        } else if (task.getType() == TaskType.EPIC) {
            Epic epicTask=(Epic) task;
            for (int subtaskId : epicTask.getSubtasksId()) {
                deleteTaskById(subtaskId);
            }
        }

        if (type == TaskType.COMMON) {
            createTask(task);
        } else if (type == TaskType.EPIC) {
            createEpic(task);
        }
        deleteTaskById(id);
    }

    public void updateTask(int id, Task taskUpdated){
        changeTaskName(id, taskUpdated.getName());
        changeTaskDescription(id, taskUpdated.getDescription());
        changeTaskType(id, taskUpdated.getType());
        changeTaskStatus(id, taskUpdated.getStatus());
    }

    @Override
    public String toString(){
        String result="TaskManager{" +
                "Number of tasks='" + tasks.size() + '\'' +
                ", number of epics='" + getListOfEpics().size() + '\'';
        if (tasks.isEmpty()) {
            result+=", tasks=null}";
        } else {
            result+=", tasks=\n";
            for (Task task : tasks.values()) {
                result+=task.toString();
                result+="\n";
            }
            result+="}";
        }

        return result;
    }
}
