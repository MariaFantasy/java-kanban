package ru.yandex.javacource.bochkareva.schedule.manager;

import ru.yandex.javacource.bochkareva.schedule.task.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private class Node {

        public Task data;
        public Node next;
        public Node prev;

        public Node(Task data, Node prev, Node next) {
            this.data = data;
            this.prev = prev;
            this.next = next;
        }
    }

    private final Map<Integer, Node> history = new HashMap<>();
    private Node tail;
    private Node head;

    private void linkLast(Task task) {
        final Node node = new Node(task, tail, null);
        if (head == null) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
    }

    private void removeNode(Node node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        final int id = task.getId();
        remove(id); // удаляем уже существующий в истории таск

        linkLast(task);
        history.put(id, tail);
    }

    @Override
    public void remove(int id) {
        final Node node = history.remove(id);
        if (node == null) {
            return;
        }
        removeNode(node);
    }

    @Override
    public List<Task> getHistory() {
        List<Task> historyToReturn = new ArrayList<>();
        Node currentNode = head;
        while (currentNode != null) {
            historyToReturn.add(currentNode.data);
            currentNode = currentNode.next;
        }
        return historyToReturn;
    }
}
