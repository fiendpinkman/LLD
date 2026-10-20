package rateLimiter;

public interface RateLimiter {

    boolean allowedRequest(String userId);
    
}