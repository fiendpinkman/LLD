package notificationSystem;

import java.util.Objects;

public final class UserSubscription {
    private final String userId;
    private final EventType eventType;
    private final ChannelType channelType;
    private final String destination;

    public UserSubscription(String userId, EventType eventType, ChannelType channelType, String destination) {

        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (destination == null || destination.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid destination");
        }

        this.userId = userId;
        this.eventType = Objects.requireNonNull(eventType);
        this.channelType = Objects.requireNonNull(channelType);
        this.destination = destination;
    }

    public String getUserId() { return userId; }
    public EventType getEventType() { return eventType; }
    public ChannelType getChannelType() { return channelType; }
    public String getDestination() { return destination; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UserSubscription)) return false;

        UserSubscription other = (UserSubscription) obj;

        return userId.equals(other.userId)
            && eventType == other.eventType
            && channelType == other.channelType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, eventType, channelType);
    }
    
}
