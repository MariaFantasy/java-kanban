package ru.yandex.javacource.bochkareva.schedule.manager;

import ru.yandex.javacource.bochkareva.schedule.task.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {

    private ArrayList<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (history.size() == 10) {
            history.removeFirst();
        }
        history.add(new Task(task));
    }

    @Override
    public ArrayList<Task> getHistory() {
        return history;
    }
}
