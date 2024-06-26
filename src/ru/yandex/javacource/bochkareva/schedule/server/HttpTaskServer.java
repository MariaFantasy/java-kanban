package ru.yandex.javacource.bochkareva.schedule.server;

import com.sun.net.httpserver.HttpServer;
import ru.yandex.javacource.bochkareva.schedule.manager.Managers;
import ru.yandex.javacource.bochkareva.schedule.manager.TaskManager;
import ru.yandex.javacource.bochkareva.schedule.server.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static HttpServer httpServer;

    public static void start() throws IOException {
        Managers managers = new Managers();
        TaskManager taskManager = managers.getDefault();

        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

        httpServer.createContext("/tasks", new TasksHandler(taskManager));
        httpServer.createContext("/subtasks", new SubtasksHandler(taskManager));
        httpServer.createContext("/epics", new EpicsHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));

        httpServer.start(); // запускаем сервер
    }

    public static void stop() {
        httpServer.stop(0);
    }

    public static void main(String[] args) throws IOException {
        start();
    }
}
