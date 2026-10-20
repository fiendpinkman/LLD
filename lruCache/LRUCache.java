import java.util.*;

public class LRUCache {

    private final int capacity;
    private final LinkedHashMap<Integer, Integer> cache;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        cache = new LinkedHashMap<Integer, Integer>(capacity, 0.75f, true) {

            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                return size() > LRUCache.this.capacity;
            }
        }; 
    }

    public synchronized int get(int key) {
        Integer value = cache.get(key);
        return value == null ? -1 : value;
    }

    public synchronized void put(int key, int value) {
        cache.put(key, value);
    }

    public static void main(String[] args) {
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