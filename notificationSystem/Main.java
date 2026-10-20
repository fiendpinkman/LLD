package notificationSystem;

public class Main {
    
    public static void main(String[] args) {
        NotificationService service =
            new NotificationService(
                new NotificationSenderFactory()
            );

        UserSubscription aliceEmail = new UserSubscription(
            "alice", EventType.PRICE_DROP,
            ChannelType.EMAIL, "alice@example.com"
        );

        UserSubscription aliceSms = new UserSubscription(
            "alice", EventType.PRICE_DROP,
            ChannelType.SMS, "+911111111111"
        );

        UserSubscription bobSms = new UserSubscription(
            "bob", EventType.PRICE_DROP,
            ChannelType.SMS, "+912222222222"
        );

        UserSubscription charlieEmail = new UserSubscription(
            "charlie", EventType.BACK_IN_STOCK,
            ChannelType.EMAIL, "charlie@example.com"
        );

        service.subscribe(aliceEmail);
        service.subscribe(aliceSms);
        service.subscribe(bobSms);
        service.subscribe(charlieEmail);

        // Duplicate subscription: ignored by the Set
        service.subscribe(new UserSubscription(
            "alice", EventType.PRICE_DROP,
            ChannelType.EMAIL, "alice@example.com"
        ));

        System.out.println("Before unsubscribe:");
        service.publish(
            EventType.PRICE_DROP,
            "Headphones price reduced"
        );

        System.out.println(
            "\nUnsubscribe Alice's EMAIL: "
            + service.unsubscribe(aliceEmail)
        );

        System.out.println("After unsubscribe:");
        service.publish(
            EventType.PRICE_DROP,
            "Headphones price reduced again"
        );

        System.out.println("\nBack in stock:");
        service.publish(
            EventType.BACK_IN_STOCK,
            "Headphones available now"
        );
    }

}
