package ru.yandex.javacource.bochkareva.schedule.manager;

import java.io.File;

public class Managers {
    public TaskManager getDefault() {
        return new FileBackedTaskManager(new File("./resources/task.csv"));
    }

    public static  HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
