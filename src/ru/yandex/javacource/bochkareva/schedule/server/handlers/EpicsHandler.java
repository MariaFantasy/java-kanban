package ru.yandex.javacource.bochkareva.schedule.server.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacource.bochkareva.schedule.exceptions.ManagerSaveException;
import ru.yandex.javacource.bochkareva.schedule.exceptions.TaskValidationException;
import ru.yandex.javacource.bochkareva.schedule.manager.TaskManager;
import ru.yandex.javacource.bochkareva.schedule.task.Epic;
import ru.yandex.javacource.bochkareva.schedule.task.TaskStatus;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class EpicsHandler extends BaseHttpHandler {
    final TaskManager taskManager;
    enum Endpoint {GET_EPICS, GET_EPIC_BY_ID, POST_EPIC, DELETE_EPIC, UNKNOWN};

    public EpicsHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        try {
            switch (endpoint) {
                case GET_EPICS:
                    getEpics(exchange);
                    break;
                case GET_EPIC_BY_ID:
                    getEpicById(exchange);
                    break;
                case POST_EPIC:
                    postEpic(exchange);
                    break;
                case DELETE_EPIC:
                    deleteEpic(exchange);
                    break;
                default:
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void getEpics(HttpExchange exchange) throws IOException {
        final List<Epic> epics = taskManager.getEpics();
        String jsonString = epics.stream().map(gson::toJson).collect(Collectors.joining(",", "[", "]"));

        sendText(exchange, jsonString, 200);
    }

    private void getEpicById(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        int id = Integer.parseInt(pathParts[2]);
        Epic epic = (Epic) taskManager.getTaskById(id);
        if (epic == null) {
            sendNotFound(exchange);
            return;
        }
        sendText(exchange, gson.toJson(epic), 200);
    }

    private void postEpic(HttpExchange exchange) throws IOException {
        String epicDescription = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);

        if (epicDescription.isBlank()) {
            sendBadRequest(exchange, "Пустое тело запроса.");
            return;
        }

        JsonElement jsonElement = JsonParser.parseString(epicDescription);
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

        Epic epic = new Epic(id, name, description, status);

        try {
            if (jsonObject.has("id")) {
                if (taskManager.getTaskById(id) == null) {
                    sendBadRequest(exchange, "Передан id, которого ранее не было.");
                    return;
                }
                taskManager.updateEpic(epic);
                final Epic updatedEpic = (Epic) taskManager.getTaskById(id);
                sendText(exchange, gson.toJson(updatedEpic), 201);
            } else {
                Integer generatedId = taskManager.addEpic(epic);
                if (generatedId == null) {
                    sendBadRequest(exchange, "Не получается создать Epic с указанными параметрами.");
                    return;
                }
                final Epic generatedEpic = (Epic) taskManager.getTaskById(generatedId);
                sendText(exchange, gson.toJson(generatedEpic), 201);
            }
        } catch (TaskValidationException e) {
            sendHasInteractions(exchange);
        } catch (ManagerSaveException e) {
            sendInternalServerError(exchange);
        }
    }

    private void deleteEpic(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        int id = Integer.parseInt(pathParts[2]);
        final Epic epic = (Epic) taskManager.getTaskById(id).clone();
        if (epic == null) {
            sendNotFound(exchange);
        } else {
            taskManager.deleteEpicById(id);
            sendText(exchange, gson.toJson(epic), 200);
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_EPICS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_EPIC;
            }
        } else if (pathParts.length == 3 && pathParts[2].matches("(\\d)*")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_EPIC_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_EPIC;
            }
        }
        return Endpoint.UNKNOWN;
    }
}
