package notificationSystem;

import java.util.EnumMap;
import java.util.Map;

public final class NotificationSenderFactory {

    private final Map<ChannelType, NotificationSender> senders =
        new EnumMap<>(ChannelType.class);

    NotificationSenderFactory() {
        senders.put(ChannelType.EMAIL, new EmailNotificationSender());

        senders.put(ChannelType.SMS, new SmsNotificationSender());
    }

    NotificationSender getSender(ChannelType channelType) {

        NotificationSender sender = senders.get(channelType);

        if (sender == null) {
            throw new IllegalArgumentException(
                "Unsupported channel: " + channelType
            );
        }

        return sender;
    }
    
}