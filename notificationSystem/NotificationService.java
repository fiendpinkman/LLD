package notificationSystem;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class NotificationService {

    private final Map<EventType, Set<UserSubscription>> subscriptionsByEvent = new EnumMap<>(EventType.class);

    private final NotificationSenderFactory senderFactory;

    NotificationService(NotificationSenderFactory senderFactory) {
        this.senderFactory = Objects.requireNonNull(senderFactory);
    }

    void subscribe(UserSubscription subscription) {
        Objects.requireNonNull(subscription);
        subscriptionsByEvent.computeIfAbsent(subscription.getEventType(), event -> new LinkedHashSet<>()).add(subscription);
    }

    boolean unsubscribe(UserSubscription subscription) {
        Objects.requireNonNull(subscription);
        Set<UserSubscription> subscribers = subscriptionsByEvent.get(subscription.getEventType());
        if (subscribers == null) {
            return false;
        }
        boolean removed = subscribers.remove(subscription);
        if (subscribers.isEmpty()) {
            subscriptionsByEvent.remove(subscription.getEventType());
        }
        return removed;
    }

    void publish(EventType eventType, String text) {
        Objects.requireNonNull(eventType);
        Objects.requireNonNull(text);
        Set<UserSubscription> subscribers = subscriptionsByEvent.getOrDefault(eventType, Collections.emptySet());
        for (UserSubscription subscription : subscribers) {
            NotificationSender sender = senderFactory.getSender(subscription.getChannelType());
            NotificationMessage notification = new NotificationMessage(subscription.getDestination(), text);
            sender.send(notification);
        }
    }
    
}
