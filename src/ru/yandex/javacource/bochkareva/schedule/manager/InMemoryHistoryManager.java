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

        public Node(Task data) {
            this.data = data;
            this.next = null;
            this.prev = null;
        }
    }

    private final Map<Integer, Node> history = new HashMap<>();
    private Node tail;
    private Node head;

    private void addLast(Task task) {
        if (task == null) {
            return;
        }
        remove(task.getId()); // удаляем уже существующий в истории таск

        Node newNode = new Node(task);
        if (tail != null) {
            tail.next = newNode;
            newNode.prev = tail;
        }
        tail = newNode;
        if (head == null) {
            head = newNode;
        }
        history.put(task.getId(), newNode);
    }

    private Task removeNode(Node node) {
        if (node == null) {
            return null;
        }
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
        history.remove(node.data.getId());
        return node.data;
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        addLast(task.clone());
    }

    @Override
    public void remove(int id) {
        removeNode(history.get(id));
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
