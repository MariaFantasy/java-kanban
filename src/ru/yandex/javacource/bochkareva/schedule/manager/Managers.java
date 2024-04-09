package ru.yandex.javacource.bochkareva.schedule.manager;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Managers {
    public TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static  HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
