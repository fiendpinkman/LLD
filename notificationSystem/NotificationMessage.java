package notificationSystem;

import java.util.Objects;

/**
 * NotificationMessage
 */
public final class NotificationMessage {

    private final String recipient;
    private final String text;

    NotificationMessage(String recipient, String text) {
        this.recipient = Objects.requireNonNull(recipient);
        this.text = Objects.requireNonNull(text);
    }

    String getRecipient() { return recipient; }
    String getText() { return text; }
}