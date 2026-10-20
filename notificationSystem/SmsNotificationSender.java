package notificationSystem;

/**
 * SmsNotificationSender
 */
public class SmsNotificationSender implements NotificationSender {

    @Override
    public void send(NotificationMessage message) {
        System.out.println("SMS to " + message.getRecipient() + ": " + message.getText());
    }

}
