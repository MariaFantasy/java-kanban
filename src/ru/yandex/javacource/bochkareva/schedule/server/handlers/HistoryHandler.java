package ru.yandex.javacource.bochkareva.schedule.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.javacource.bochkareva.schedule.manager.TaskManager;
import ru.yandex.javacource.bochkareva.schedule.task.Task;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryHandler extends BaseHttpHandler {
    final TaskManager taskManager;

    public HistoryHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final List<Task> tasks = taskManager.getHistory();
        String jsonString = tasks.stream().map(gson::toJson).collect(Collectors.joining(",", "[", "]"));

        sendText(exchange, jsonString, 200);
    }
}
