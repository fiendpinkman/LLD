package rateLimiter;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public final class TokenBucket implements RateLimiter {

    // One bucket per user
    private static final class Bucket {
        double availableTokens;
        long lastRefillMillis;

        Bucket(int capacity, long nowMillis) {
            this.availableTokens = capacity;
            this.lastRefillMillis = nowMillis;
        }
    }

    private final ConcurrentMap<String, Bucket> bucketsByUser = new ConcurrentHashMap<>();

    private final int capacity;
    private final int tokensPerRefill;
    private final long refillIntervalMillis;
    private final Clock clock;

    public TokenBucket(int capacity, int tokensPerRefill, long refillIntervalMillis, Clock clock) {

        if (capacity <= 0 || tokensPerRefill <= 0 || refillIntervalMillis <= 0) {
            throw new IllegalArgumentException("Configuration must be positive");
        }

        if (clock == null) {
            throw new IllegalArgumentException("Clock cannot be null");
        }

        this.capacity = capacity;
        this.tokensPerRefill = tokensPerRefill;
        this.refillIntervalMillis = refillIntervalMillis;
        this.clock = clock;
    }

    @Override
    public boolean allowedRequest(String userId) {

        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        Bucket bucket = bucketsByUser.computeIfAbsent(userId, id -> new Bucket(capacity, clock.millis()));

        synchronized (bucket) {

            refillBucket(bucket, clock.millis());

            if (bucket.availableTokens < 1.0) {
                return false;
            }

            bucket.availableTokens -= 1.0;
            return true;
        }
    }

    private void refillBucket(Bucket bucket, long nowMillis) {

        long elapsedMillis = nowMillis - bucket.lastRefillMillis;

        if (elapsedMillis <= 0) {
            return;
        }

        double earnedTokens = elapsedMillis * (tokensPerRefill / (double) refillIntervalMillis);

        bucket.availableTokens = Math.min(capacity, bucket.availableTokens + earnedTokens);
        bucket.lastRefillMillis = nowMillis;
    }

    // Controllable clock for deterministic testing
    private static final class MutableClock extends Clock {

        private final AtomicLong currentMillis;
        private final ZoneId zone;

        MutableClock() {
            this(new AtomicLong(), ZoneId.of("UTC"));
        }

        private MutableClock(
                AtomicLong currentMillis, ZoneId zone) {
            this.currentMillis = currentMillis;
            this.zone = zone;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(currentMillis, zone);
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(currentMillis.get());
        }

        void advanceMillis(long millis) {
            currentMillis.addAndGet(millis);
        }
    }

    public static void main(String[] args) {

        MutableClock clock = new MutableClock();

        // Capacity 3; refill 1 token every 1000ms
        RateLimiter limiter =
            new TokenBucket(3, 1, 1000, clock);

        // Alice starts with 3 tokens
        System.out.println(limiter.allowedRequest("Alice")); // true
        System.out.println(limiter.allowedRequest("Alice")); // true
        System.out.println(limiter.allowedRequest("Alice")); // true
        System.out.println(limiter.allowedRequest("Alice")); // false

        // Bob has an independent bucket
        System.out.println(limiter.allowedRequest("Bob"));   // true

        // Advance 500ms: Alice earns 0.5 token
        clock.advanceMillis(500);

        System.out.println(limiter.allowedRequest("Alice")); // false

        // Another 500ms: Alice now has 1 token
        clock.advanceMillis(500);

        System.out.println(limiter.allowedRequest("Alice")); // true
        System.out.println(limiter.allowedRequest("Alice")); // false
    }
}
