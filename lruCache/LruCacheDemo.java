import java.util.*;

public class LruCacheDemo {

    static class Node {

        int key;
        int value;

        Node next;
        Node prev;

        Node(int key, int value) {
            this.key = key;
            this.value = value;
        }

    }

    class LRUCache {
        private final Map<Integer, Node> lookUp = new HashMap<>();
        private final Node head = new Node(-1, -1);
        private final Node tail = new Node(-1, -1);
        private final int capacity;

        public LRUCache(int capacity) {
            if (capacity <= 0) throw new IllegalArgumentException("Invalid Capacity");
            this.capacity = capacity;
            head.next = tail;
            tail.prev = head;

        }

        public int get(int key) {
            Node node = lookUp.get(key);
            if (node == null) return -1;
            moveToFront(node);
            return node.value;
        }

        public void put(int key, int value) {
            Node node = lookUp.get(key);
            if (node == null) {
                node = new Node(key, value);
                lookUp.put(key, node);
                addToFront(node);
            } else {
                node.value = value;
                moveToFront(node);
            }

            if (lookUp.size() > capacity) {
                Node lru = tail.prev;
                removeNode(tail.prev);
                lookUp.remove(lru.key);
            }
        }

        void moveToFront(Node node) {
            removeNode(node);
            addToFront(node);
        }

        void removeLRU() {

        }

        void addToFront(Node node) {
            Node next = head.next;
            head.next = node;
            node.prev = head;
            node.next = next;
            next.prev = node;
        }

        void removeNode(Node node) {
            Node prev = node.prev;
            Node next = node.next;
            prev.next = next;
            next.prev = prev;
        }

    }

    public void main(String[] args) {
        LRUCache cache = new LRUCache(2);
        cache.put(1, 10);
        cache.put(2, 20);

        System.out.println(cache.get(1));

        cache.put(3, 30);

        cache.put(1, 100);

        System.out.println(cache.get(2));
        System.out.println(cache.get(1));
    }

}