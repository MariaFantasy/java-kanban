package ru.yandex.javacourse.bochkareva.schedule.server;

import com.google.gson.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.javacource.bochkareva.schedule.server.HttpTaskServer;
import ru.yandex.javacource.bochkareva.schedule.server.handlers.DurationAdapter;
import ru.yandex.javacource.bochkareva.schedule.server.handlers.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    public static HttpTaskServer server = new HttpTaskServer();
    protected static final Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    @BeforeAll
    public static void beforeAll() throws IOException, InterruptedException {
        server.start();

        HttpClient client = HttpClient.newHttpClient();

        URI urlTasks = URI.create("http://localhost:8080/tasks");
        URI urlEpics = URI.create("http://localhost:8080/epics");
        URI urlSubtasks = URI.create("http://localhost:8080/subtasks");

        String taskJson = String.format("{\"name\":\"%s\",\"status\":\"%s\",\"duration\":\"PT20H15M\",\"startTime\":\"2024-01-01 00:00:00\"} ", "New Task", "DONE");
        HttpRequest request = HttpRequest.newBuilder().uri(urlTasks).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        taskJson = String.format("{\"name\":\"%s\",\"status\":\"%s\",\"duration\":\"PT20H15M\",\"startTime\":\"2023-01-01 00:00:00\"} ", "New Task 2", "DONE");
        request = HttpRequest.newBuilder().uri(urlTasks).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        taskJson = String.format("{\"name\":\"%s\",\"status\":\"%s\"} ", "New Epic", "NEW");
        request = HttpRequest.newBuilder().uri(urlEpics).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        taskJson = String.format("{\"name\":\"%s\",\"status\":\"%s\"} ", "New Epic 2", "NEW");
        request = HttpRequest.newBuilder().uri(urlEpics).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        taskJson = String.format("{\"name\":\"%s\",\"status\":\"%s\",\"epicId\":4,\"duration\":\"PT20H15M\",\"startTime\":\"2025-01-01 00:00:00\"} ", "New Subtask", "DONE");
        request = HttpRequest.newBuilder().uri(urlSubtasks).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @AfterAll
    public static void afterAll() throws IOException {
        server.stop();
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Не получается получить список задач.");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray(), "При запросе списка задач возвращается не список.");

        JsonArray jsonArray = jsonElement.getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement element = jsonArray.get(i);
            assertTrue(element.isJsonObject(), "При запросе списка задач возвращается список не объектов.");
        }
    }

    @Test
    public void testGetTaskByIdFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Возвращает неверный код при поиске существующей задачи.");

        assertFalse(response.body().isBlank(), "При запросе существующей задачи возвращается пустое тело.");
    }

    @Test
    public void testGetTaskByIdNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1000");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Возвращает неверный код при поиске несуществующей задачи.");

        assertTrue(response.body().isBlank(), "При запросе несуществующей задачи возвращается непустое тело: " + response.body());
    }

    @Test
    public void testCreateTaskSuccess() throws IOException, InterruptedException {
        String taskJson = String.format("{\"name\":\"%s\",\"status\":\"%s\"} ", "New Task", "NEW");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Задача не была создана (возвращает неправильный статус).");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject(), "Тело ответа содержит не объект.");

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertTrue(jsonObject.has("id"), "Тело ответа имеет неверную структуру (нет id задачи).");

        int id = jsonObject.get("id").getAsInt();

        URI urlCheck = URI.create(String.format("http://localhost:8080/tasks/%d", id));
        HttpRequest requestCheck = HttpRequest.newBuilder().uri(urlCheck).GET().build();
        HttpResponse<String> responseCheck = client.send(requestCheck, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseCheck.statusCode(), "Задача не была создана (сервер не находит задачу).");
    }

    @Test
    public void testCreateTaskFailedBecauseOfTimelines() throws IOException, InterruptedException {
        String taskJson = String.format("{\"name\":\"%s\",\"status\":\"%s\",\"startTime\":\"2024-01-01 00:00:00\"} ", "New Task", "NEW");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Возвращается неверный статус.");
    }

    @Test
    public void testUpdateTaskSuccess() throws IOException, InterruptedException {
        String taskJson = String.format("{\"id\":1,\"name\":\"%s\",\"status\":\"%s\",\"description\":\"Нужно доделать задание\",\"duration\":\"PT20H15M\",\"startTime\":\"2024-01-01 00:00:00\"} ", "New Task", "DONE");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Задача не была обновлена (возвращает неправильный статус).");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject(), "Тело ответа содержит не объект.");

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertTrue(jsonObject.has("id"), "Тело ответа имеет неверную структуру (нет id задачи).");

        int id = jsonObject.get("id").getAsInt();

        URI urlCheck = URI.create(String.format("http://localhost:8080/tasks/%d", id));
        HttpRequest requestCheck = HttpRequest.newBuilder().uri(urlCheck).GET().build();
        HttpResponse<String> responseCheck = client.send(requestCheck, HttpResponse.BodyHandlers.ofString());
        assertEquals("Нужно доделать задание",
                JsonParser.parseString(responseCheck.body()).getAsJsonObject().get("description").getAsString(),
                "Таска не была обновлена (осталась в прежнем состоянии).");
    }

    @Test
    public void testUpdateTaskFailedBecauseOfTimelines() throws IOException, InterruptedException {
        String taskJson = String.format("{\"id\":1,\"name\":\"%s\",\"status\":\"%s\",\"description\":\"Нужно доделать задание\",\"duration\":\"PT20H15M\",\"startTime\":\"2023-01-01 00:00:00\"} ", "New Task", "DONE");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Возвращается неверный статус.");
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Не получается получить список эпиков.");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray(), "При запросе списка эпиков возвращается не список.");

        JsonArray jsonArray = jsonElement.getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement element = jsonArray.get(i);
            assertTrue(element.isJsonObject(), "При запросе списка эпиков возвращается список не объектов.");
        }
    }

    @Test
    public void testGetEpicByIdFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/3");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Возвращает неверный код при поиске существующего эпика.");

        assertFalse(response.body().isBlank(), "При запросе существующего эпика возвращается пустое тело.");
    }

    @Test
    public void testGetEpicByIdNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1000");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Возвращает неверный код при поиске несуществующего эпика.");

        assertTrue(response.body().isBlank(), "При запросе несуществующего эпика возвращается непустое тело: " + response.body());
    }

    @Test
    public void testCreateEpicSuccess() throws IOException, InterruptedException {
        String taskJson = String.format("{\"name\":\"%s\",\"status\":\"%s\"} ", "New Epic", "NEW");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Эпик не был создан (возвращает неправильный статус).");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject(), "Тело ответа содержит не объект.");

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertTrue(jsonObject.has("id"), "Тело ответа имеет неверную структуру (нет id эпика).");

        int id = jsonObject.get("id").getAsInt();

        URI urlCheck = URI.create(String.format("http://localhost:8080/epics/%d", id));
        HttpRequest requestCheck = HttpRequest.newBuilder().uri(urlCheck).GET().build();
        HttpResponse<String> responseCheck = client.send(requestCheck, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseCheck.statusCode(), "Эпик не был создан (сервер не находит эпик).");
    }

    @Test
    public void testUpdateEpicSuccess() throws IOException, InterruptedException {
        String taskJson = String.format("{\"id\":3,\"name\":\"%s\",\"status\":\"%s\",\"description\":\"Нужно доделать задание\"} ", "New Epic", "NEW");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Эпик не был обновлен (возвращает неправильный статус).");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject(), "Тело ответа содержит не объект.");

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertTrue(jsonObject.has("id"), "Тело ответа имеет неверную структуру (нет id задачи).");

        int id = jsonObject.get("id").getAsInt();

        URI urlCheck = URI.create(String.format("http://localhost:8080/epics/%d", id));
        HttpRequest requestCheck = HttpRequest.newBuilder().uri(urlCheck).GET().build();
        HttpResponse<String> responseCheck = client.send(requestCheck, HttpResponse.BodyHandlers.ofString());
        assertEquals("Нужно доделать задание",
                JsonParser.parseString(responseCheck.body()).getAsJsonObject().get("description").getAsString(),
                "Эпик не был обновлен (осталась в прежнем состоянии).");
    }

    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Не получается получить список подзадач.");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray(), "При запросе списка подзадач возвращается не список.");

        JsonArray jsonArray = jsonElement.getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement element = jsonArray.get(i);
            assertTrue(element.isJsonObject(), "При запросе списка подзадач возвращается список не объектов.");
        }
    }

    @Test
    public void testGetSubtaskByIdFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/5");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Возвращает неверный код при поиске существующей подзадачи.");

        assertFalse(response.body().isBlank(), "При запросе существующей подзадачи возвращается пустое тело.");
    }

    @Test
    public void testGetSubtaskByIdNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/1000");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Возвращает неверный код при поиске несуществующей подзадачи.");

        assertTrue(response.body().isBlank(), "При запросе несуществующей подзадачи возвращается непустое тело: " + response.body());
    }

    @Test
    public void testCreateSubtaskSuccess() throws IOException, InterruptedException {
        String taskJson = String.format("{\"name\":\"%s\",\"status\":\"%s\",\"epicId\":4,\"duration\":\"PT20H15M\",\"startTime\":\"2025-01-02 00:00:00\"} ", "New Subtask", "DONE");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Подадача не была создана (возвращает неправильный статус).");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject(), "Тело ответа содержит не объект.");

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertTrue(jsonObject.has("id"), "Тело ответа имеет неверную структуру (нет id задачи).");

        int id = jsonObject.get("id").getAsInt();

        URI urlCheck = URI.create(String.format("http://localhost:8080/subtasks/%d", id));
        HttpRequest requestCheck = HttpRequest.newBuilder().uri(urlCheck).GET().build();
        HttpResponse<String> responseCheck = client.send(requestCheck, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseCheck.statusCode(), "Подзадача не была создана (сервер не находит задачу).");
    }

    @Test
    public void testCreateSubtaskFailedBecauseOfTimelines() throws IOException, InterruptedException {
        String taskJson = String.format("{\"name\":\"%s\",\"status\":\"%s\",\"epicId\":4,\"duration\":\"PT20H15M\",\"startTime\":\"2025-01-01 00:00:00\"} ", "New Subtask", "DONE");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Возвращается неверный статус.");
    }

    @Test
    public void testUpdateSubtaskSuccess() throws IOException, InterruptedException {
        String taskJson = String.format("{\"id\":5,\"name\":\"%s\",\"status\":\"%s\",\"epicId\":4,\"description\":\"Нужно доделать задание\",\"duration\":\"PT20H15M\",\"startTime\": \"2025-01-01 00:00:00\"} ", "New Subtask", "DONE");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Подзадача не была обновлена (возвращает неправильный статус).");

        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject(), "Тело ответа содержит не объект.");

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertTrue(jsonObject.has("id"), "Тело ответа имеет неверную структуру (нет id задачи).");

        int id = jsonObject.get("id").getAsInt();

        URI urlCheck = URI.create(String.format("http://localhost:8080/subtasks/%d", id));
        HttpRequest requestCheck = HttpRequest.newBuilder().uri(urlCheck).GET().build();
        HttpResponse<String> responseCheck = client.send(requestCheck, HttpResponse.BodyHandlers.ofString());
        assertEquals("Нужно доделать задание",
                JsonParser.parseString(responseCheck.body()).getAsJsonObject().get("description").getAsString(),
                "Подзадача не была обновлена (осталась в прежнем состоянии).");
    }

    @Test
    public void testUpdateSubtaskFailedBecauseOfTimelines() throws IOException, InterruptedException {
        String taskJson = String.format("{\"id\":5,\"name\":\"%s\",\"status\":\"%s\",\"epicId\":4,\"description\":\"Нужно доделать задание\",\"duration\":\"PT20H15M\",\"startTime\": \"2024-01-01 00:00:00\"} ", "New Subtask", "DONE");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Возвращается неверный статус.");
    }

}