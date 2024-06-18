package ru.yandex.javacource.bochkareva.schedule.server.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacource.bochkareva.schedule.exceptions.ManagerSaveException;
import ru.yandex.javacource.bochkareva.schedule.manager.TaskManager;
import ru.yandex.javacource.bochkareva.schedule.exceptions.TaskValidationException;
import ru.yandex.javacource.bochkareva.schedule.task.Task;
import ru.yandex.javacource.bochkareva.schedule.task.TaskStatus;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TasksHandler extends BaseHttpHandler {
    final TaskManager taskManager;
    enum Endpoint {GET_TASKS, GET_TASK_BY_ID, POST_TASK, DELETE_TASK, UNKNOWN};

    public TasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        try {
            switch (endpoint) {
                case GET_TASKS:
                    getTasks(exchange);
                    break;
                case GET_TASK_BY_ID:
                    getTaskById(exchange);
                    break;
                case POST_TASK:
                    postTask(exchange);
                    break;
                case DELETE_TASK:
                    deleteTask(exchange);
                    break;
                default:
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void getTasks(HttpExchange exchange) throws IOException {
        final List<Task> tasks = taskManager.getTasks();
        String jsonString = tasks.stream().map(gson::toJson).collect(Collectors.joining(",", "[", "]"));

        sendText(exchange, jsonString, 200);
    }

    private void getTaskById(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        int id = Integer.parseInt(pathParts[2]);
        Task task = taskManager.getTaskById(id);
        if (task == null) {
            sendNotFound(exchange);
            return;
        }
        sendText(exchange, gson.toJson(task), 200);
    }

    private void postTask(HttpExchange exchange) throws IOException {
        String taskDescription = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);

        if (taskDescription.isBlank()) {
            sendBadRequest(exchange, "Пустое тело запроса.");
            return;
        }

        JsonElement jsonElement = JsonParser.parseString(taskDescription);
        if (!jsonElement.isJsonObject()) {
            sendBadRequest(exchange, "Передан не объект.");
            return;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        int id = jsonObject.has("id") ? jsonObject.get("id").getAsInt() : -1;
        String name = jsonObject.has("name") ? jsonObject.get("name").getAsString() : "";
        String description = jsonObject.has("description") ?
                jsonObject.get("description").getAsString() : "";
        TaskStatus status = jsonObject.has("status") ?
                TaskStatus.valueOf(jsonObject.get("status").getAsString()) : TaskStatus.NEW;
        Duration duration = jsonObject.has("duration") ?
                Duration.parse(jsonObject.get("duration").getAsString()) : null;
        LocalDateTime startTime = jsonObject.has("startTime") ?
                LocalDateTime.parse(jsonObject.get("startTime").getAsString(), dtf) : null;

        Task task = new Task(id, name, description, status);
        task.setDuration(duration);
        task.setStartTime(startTime);

        try {
            if (jsonObject.has("id")) {
                if (taskManager.getTaskById(id) == null) {
                    sendBadRequest(exchange, "Передан id, которого ранее не было.");
                    return;
                }
                taskManager.updateTask(task);
                final Task updatedTask = taskManager.getTaskById(id);
                sendText(exchange, gson.toJson(updatedTask), 201);
            } else {
                Integer generatedId = taskManager.addTask(task);
                if (generatedId == null) {
                    sendBadRequest(exchange, "Не получается создать Task с указанными параметрами.");
                    return;
                }
                final Task generatedTask = taskManager.getTaskById(generatedId);
                sendText(exchange, gson.toJson(generatedTask), 201);
            }
        } catch (TaskValidationException e) {
            sendHasInteractions(exchange);
        } catch (ManagerSaveException e) {
            sendInternalServerError(exchange);
        }
    }

    private void deleteTask(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        int id = Integer.parseInt(pathParts[2]);
        final Task task = taskManager.getTaskById(id).clone();
        if (task == null) {
            sendNotFound(exchange);
        } else {
            taskManager.deleteTaskById(id);
            sendText(exchange, gson.toJson(task), 200);
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASKS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_TASK;
            }
        } else if (pathParts.length == 3 && pathParts[2].matches("(\\d)*")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASK_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_TASK;
            }
        }
        return Endpoint.UNKNOWN;
    }
}
