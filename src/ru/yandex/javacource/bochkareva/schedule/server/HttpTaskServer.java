package ru.yandex.javacource.bochkareva.schedule.server;

import com.sun.net.httpserver.HttpServer;
import ru.yandex.javacource.bochkareva.schedule.manager.Managers;
import ru.yandex.javacource.bochkareva.schedule.manager.TaskManager;
import ru.yandex.javacource.bochkareva.schedule.server.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.google.gson.Gson;

public class HttpTaskServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        Managers managers = new Managers();
        TaskManager taskManager = managers.getDefault();

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

        httpServer.createContext("/tasks", new TasksHandler(taskManager));
        httpServer.createContext("/subtasks", new SubtasksHandler(taskManager));
        httpServer.createContext("/epics", new EpicsHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));

        httpServer.start(); // запускаем сервер

        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
        // завершаем работу сервера для корректной работы тренажёра
        //httpServer.stop(1);
    }
}
