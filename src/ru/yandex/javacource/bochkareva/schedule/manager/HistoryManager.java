package ru.yandex.javacource.bochkareva.schedule.manager;

import ru.yandex.javacource.bochkareva.schedule.task.*;

import java.util.ArrayList;

public interface HistoryManager {
    public void add(Task task);

    public ArrayList<Task> getHistory();
}
