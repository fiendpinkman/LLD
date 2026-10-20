package rateLimiter;

import java.time.Clock;
import java.util.*;

public class SlidingWindowRateLimiter implements RateLimiter {

    static final class UserWindow {
        Queue<Long> queue = new ArrayDeque<>();
    }

    private final Map<String, UserWindow> rateLimiterMap = new HashMap<>();
    private final int maxRequests;
    private final long windowMillis;
    private final Clock clock;

    public SlidingWindowRateLimiter(int maxRequests, long windowMillis, Clock clock) {
        if (maxRequests <=0 || windowMillis <=0) {
            throw new IllegalArgumentException("Limit and Window must be positive");
        }

        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public boolean allowedRequest(String userId) {
        if (userId == null || userId.isBlank()) 
            throw new IllegalArgumentException("Invalid User Id");
        UserWindow userWindow = rateLimiterMap.computeIfAbsent(userId, id -> new UserWindow());
        synchronized (userWindow) {

            long now = clock.millis();

            removeExpiredRequests(userWindow.queue, now);

            if (isLimitReached(userWindow.queue)) {
                return false;
            }
            userWindow.queue.add(now);
            return true;
        }
    }

    public void removeExpiredRequests(Queue<Long> timeStamps, long now) {
        while (!timeStamps.isEmpty() && timeStamps.peek() <= now -windowMillis) {
            timeStamps.remove();
        }
    }

    public boolean isLimitReached(Queue<Long> timeStamps) {
        return timeStamps.size() >= maxRequests;
    }

    public static void main(String[] args) {
        RateLimiter limiter = new SlidingWindowRateLimiter(3, 10_000, Clock.systemDefaultZone());
        System.out.println(limiter.allowedRequest("Alice"));
        System.out.println(limiter.allowedRequest("Alice"));
        System.out.println(limiter.allowedRequest("Alice"));
        System.out.println(limiter.allowedRequest("Alice"));

        System.out.println(limiter.allowedRequest("Bob"));
    }
    
}
