package ru.yandex.javacource.bochkareva.schedule.server.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacource.bochkareva.schedule.exceptions.ManagerSaveException;
import ru.yandex.javacource.bochkareva.schedule.exceptions.TaskValidationException;
import ru.yandex.javacource.bochkareva.schedule.manager.TaskManager;
import ru.yandex.javacource.bochkareva.schedule.task.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SubtasksHandler extends BaseHttpHandler {
    final TaskManager taskManager;

    enum Endpoint { GET_SUBTASKS, GET_SUBTASK_BY_ID, POST_SUBTASK, DELETE_SUBTASK, UNKNOWN }

    public SubtasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        try {
            switch (endpoint) {
                case GET_SUBTASKS:
                    getSubtasks(exchange);
                    break;
                case GET_SUBTASK_BY_ID:
                    getSubtaskById(exchange);
                    break;
                case POST_SUBTASK:
                    postSubtask(exchange);
                    break;
                case DELETE_SUBTASK:
                    deleteSubtask(exchange);
                    break;
                default:
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void getSubtasks(HttpExchange exchange) throws IOException {
        final List<Subtask> subtasks = taskManager.getSubtasks();
        String jsonString = subtasks.stream().map(gson::toJson).collect(Collectors.joining(",", "[", "]"));

        sendText(exchange, jsonString, 200);
    }

    private void getSubtaskById(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        int id = Integer.parseInt(pathParts[2]);
        Subtask subtask = (Subtask) taskManager.getTaskById(id);
        if (subtask == null) {
            sendNotFound(exchange);
            return;
        }
        sendText(exchange, gson.toJson(subtask), 200);
    }

    private void postSubtask(HttpExchange exchange) throws IOException {
        String subtaskDescription = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);

        if (subtaskDescription.isBlank()) {
            sendBadRequest(exchange, "Пустое тело запроса.");
            return;
        }

        JsonElement jsonElement = JsonParser.parseString(subtaskDescription);
        if (!jsonElement.isJsonObject()) {
            sendBadRequest(exchange, "Передан не объект.");
            return;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        int id = jsonObject.has("id") ? jsonObject.get("id").getAsInt() : -1;
        int epicId = jsonObject.has("epicId") ? jsonObject.get("epicId").getAsInt() : -1;
        String name = jsonObject.has("name") ? jsonObject.get("name").getAsString() : "";
        String description = jsonObject.has("description") ?
                jsonObject.get("description").getAsString() : "";
        TaskStatus status = jsonObject.has("status") ?
                TaskStatus.valueOf(jsonObject.get("status").getAsString()) : TaskStatus.NEW;
        Duration duration = jsonObject.has("duration") ?
                Duration.parse(jsonObject.get("duration").getAsString()) : null;
        LocalDateTime startTime = jsonObject.has("startTime") ?
                LocalDateTime.parse(jsonObject.get("startTime").getAsString(), dtf) : null;

        Subtask subtask = new Subtask(id, name, description, status, epicId);
        subtask.setDuration(duration);
        subtask.setStartTime(startTime);

        try {
            if (jsonObject.has("id")) {
                if (taskManager.getTaskById(id) == null) {
                    sendBadRequest(exchange, "Передан id, которого ранее не было.");
                    return;
                }
                taskManager.updateSubtask(subtask);
                final Subtask updatedSubtask = (Subtask) taskManager.getTaskById(id);
                sendText(exchange, gson.toJson(updatedSubtask), 201);
            } else {
                Integer generatedId = taskManager.addSubtask(subtask);
                if (generatedId == null) {
                    sendBadRequest(exchange, "Не получается создать Subtask с указанными параметрами.");
                    return;
                }
                final Subtask generatedSubtask = (Subtask) taskManager.getTaskById(generatedId);
                sendText(exchange, gson.toJson(generatedSubtask), 201);
            }
        } catch (TaskValidationException e) {
            sendHasInteractions(exchange);
        } catch (ManagerSaveException e) {
            sendInternalServerError(exchange);
        }
    }

    private void deleteSubtask(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        int id = Integer.parseInt(pathParts[2]);
        final Subtask subtask = (Subtask) taskManager.getTaskById(id).clone();
        if (subtask == null) {
            sendNotFound(exchange);
        } else {
            taskManager.deleteSubtaskById(id);
            sendText(exchange, gson.toJson(subtask), 200);
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_SUBTASKS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_SUBTASK;
            }
        } else if (pathParts.length == 3 && pathParts[2].matches("(\\d)*")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_SUBTASK_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_SUBTASK;
            }
        }
        return Endpoint.UNKNOWN;
    }
}
