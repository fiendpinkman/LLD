package notificationSystem;

public class EmailNotificationSender implements NotificationSender {

    @Override
    public void send(NotificationMessage notificationMessage) {
        System.out.println("Email to " + notificationMessage.getRecipient() + ": " + notificationMessage.getText());
    }
    
}
