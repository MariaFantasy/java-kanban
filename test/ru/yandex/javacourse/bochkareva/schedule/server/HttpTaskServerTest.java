package ru.yandex.javacourse.bochkareva.schedule.server;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import ru.yandex.javacource.bochkareva.schedule.manager.FileBackedTaskManager;
import ru.yandex.javacource.bochkareva.schedule.server.HttpTaskServer;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    public static HttpTaskServer server = new HttpTaskServer();
    @BeforeAll
    public static void beforeAll() throws IOException {
        server.start();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        server.stop();
    }

}