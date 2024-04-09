package ru.yandex.javacource.bochkareva.schedule.manager;

import ru.yandex.javacource.bochkareva.schedule.task.*;

import java.util.List;

public interface HistoryManager {
    void add(Task task);

    List<Task> getHistory();
}
