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

        String taskJson = String.format("{\"name\"=\"%s\",\"status\"=\"%s\",\"duration\"=\"PT20H15M\",\"startTime\": \"2024-01-01 00:00:00\"} ", "New Task", "DONE");
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        taskJson = String.format("{\"name\"=\"%s\",\"status\"=\"%s\",\"duration\"=\"PT20H15M\",\"startTime\": \"2023-01-01 00:00:00\"} ", "New Task 2", "DONE");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
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
        String taskJson = String.format("{\"name\"=\"%s\",\"status\"=\"%s\"} ", "New Task", "NEW");

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
        String taskJson = String.format("{\"name\"=\"%s\",\"status\"=\"%s\",\"startTime\": \"2024-01-01 00:00:00\"} ", "New Task", "NEW");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Возвращается неверный статус.");
    }

    @Test
    public void testUpdateTaskSuccess() throws IOException, InterruptedException {
        String taskJson = String.format("{\"id\":1,\"name\"=\"%s\",\"status\"=\"%s\",\"description\": \"Нужно доделать задание\",\"duration\"=\"PT20H15M\",\"startTime\": \"2024-01-01 00:00:00\"} ", "New Task", "DONE");

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
        String taskJson = String.format("{\"id\":1,\"name\"=\"%s\",\"status\"=\"%s\",\"description\": \"Нужно доделать задание\",\"duration\"=\"PT20H15M\",\"startTime\": \"2023-01-01 00:00:00\"} ", "New Task", "DONE");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Возвращается неверный статус.");
    }
}