package ru.yandex.javacource.bochkareva.schedule.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacource.bochkareva.schedule.manager.TaskManager;
import ru.yandex.javacource.bochkareva.schedule.task.Task;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class PrioritizedHandler extends BaseHttpHandler {
    final TaskManager taskManager;

    public PrioritizedHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final List<Task> tasks = taskManager.getPrioritizedTasks();
        String jsonString = tasks.stream().map(gson::toJson).collect(Collectors.joining(",", "[", "]"));

        sendText(exchange, jsonString, 200);
    }
}
