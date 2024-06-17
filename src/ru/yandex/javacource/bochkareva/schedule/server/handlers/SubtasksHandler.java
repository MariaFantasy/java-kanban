package ru.yandex.javacource.bochkareva.schedule.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.javacource.bochkareva.schedule.manager.TaskManager;

import java.io.IOException;

public class SubtasksHandler implements HttpHandler {
    final TaskManager taskManager;

    public SubtasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        //
    }
}
