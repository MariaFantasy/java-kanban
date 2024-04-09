package ru.yandex.javacource.bochkareva.schedule.manager;

import ru.yandex.javacource.bochkareva.schedule.task.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    final private List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        if (history.size() == 10) {
            history.removeFirst();
        }
        history.add(task.clone());
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }
}
