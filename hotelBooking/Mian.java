import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Mian
 */
public class Mian {

    // SIMPLE TEST ASSERTION
    private static void check(
            boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Clock clock = Clock.systemDefaultZone();
        HotelService service = new HotelService(clock);

        // Register hotels
        service.addHotel(
            new Hotel("H1", "Mountain View", "Manali"));

        service.addHotel(
            new Hotel("H2", "Snow Palace", "Manali"));

        service.addHotel(
            new Hotel("H3", "City Inn", "Mumbai"));

        // Register rooms
        service.addRoom(new Room(
            "R101", "H1", RoomType.DELUXE,
            new BigDecimal("3000")));

        service.addRoom(new Room(
            "R102", "H1", RoomType.DELUXE,
            new BigDecimal("3500")));

        service.addRoom(new Room(
            "R103", "H1", RoomType.STANDARD,
            new BigDecimal("1500")));

        service.addRoom(new Room(
            "R201", "H2", RoomType.DELUXE,
            new BigDecimal("2500")));

        service.addRoom(new Room(
            "R301", "H3", RoomType.DELUXE,
            new BigDecimal("4500")));

        LocalDate start =
            LocalDate.now(clock).plusDays(5);

        LocalDate end = start.plusDays(2);

        // TEST 1: Search
        System.out.println("1. Search available rooms:");

        List<Room> rooms =
            service.searchAvailableRooms(
                "Manali",
                RoomType.DELUXE,
                start,
                end
            );

        rooms.forEach(System.out::println);

        check(rooms.size() == 3,
            "Expected three available deluxe rooms");

        // TEST 2: Successful booking
        System.out.println("\n2. Alice books Room R101:");

        Booking alice = service.bookRoom(
            "alice", "R101", start, end);

        System.out.println(alice);

        check(
            alice.getTotalPrice()
                .compareTo(new BigDecimal("6000")) == 0,
            "Incorrect total price"
        );

        // TEST 3: Reject overlapping booking
        System.out.println("\n3. Bob attempts overlapping booking:");

        try {
            service.bookRoom(
                "bob",
                "R101",
                start.plusDays(1),
                end.plusDays(1)
            );

            throw new AssertionError(
                "Overlapping booking should fail");

        } catch (IllegalStateException expected) {
            System.out.println(
                "Rejected: " + expected.getMessage());
        }

        // TEST 4: Adjacent booking is allowed
        System.out.println("\n4. Charlie checks in after Alice:");

        Booking charlie = service.bookRoom(
            "charlie",
            "R101",
            end,
            end.plusDays(2)
        );

        System.out.println(charlie);

        // TEST 5: Cancellation and rebooking
        System.out.println("\n5. Cancel Alice's reservation:");

        check(
            service.cancelBooking(alice.getId()),
            "Cancellation should succeed"
        );

        check(
            !service.cancelBooking(alice.getId()),
            "Already cancelled booking should return false"
        );

        check(
            !service.cancelBooking("UNKNOWN"),
            "Unknown booking should return false"
        );

        List<Room> afterCancellation =
            service.searchAvailableRooms(
                "Manali", RoomType.DELUXE, start, end);

        check(
            afterCancellation.stream()
                .anyMatch(r -> r.getId().equals("R101")),
            "R101 should become available"
        );

        Booking bob = service.bookRoom(
            "bob", "R101", start, end);

        System.out.println("Rebooked: " + bob);

        // TEST 6: Invalid inputs
        System.out.println("\n6. Validate invalid requests:");

        try {
            service.bookRoom("test", "R101", end, end);
            throw new AssertionError(
                "Invalid dates should fail");

        } catch (IllegalArgumentException expected) {
            System.out.println("Invalid dates rejected");
        }

        try {
            service.bookRoom(
                "test", "UNKNOWN", start, end);

            throw new AssertionError(
                "Unknown room should fail");

        } catch (IllegalArgumentException expected) {
            System.out.println("Unknown room rejected");
        }

        // TEST 7: Concurrent booking protection
        System.out.println("\n7. Concurrent double-booking test:");

        LocalDate raceStart = end.plusDays(10);
        LocalDate raceEnd = raceStart.plusDays(2);

        ExecutorService executor =
            Executors.newFixedThreadPool(2);

        CountDownLatch startSignal =
            new CountDownLatch(1);

        try {
            Future<Boolean> first = executor.submit(() -> {

                startSignal.await();

                try {
                    service.bookRoom(
                        "threadA", "R102", raceStart, raceEnd);
                    return true;

                } catch (IllegalStateException e) {
                    return false;
                }
            });

            Future<Boolean> second = executor.submit(() -> {

                startSignal.await();

                try {
                    service.bookRoom(
                        "threadB", "R102", raceStart, raceEnd);
                    return true;

                } catch (IllegalStateException e) {
                    return false;
                }
            });

            startSignal.countDown();

            int successes =
                (first.get() ? 1 : 0)
                + (second.get() ? 1 : 0);

            check(
                successes == 1,
                "Exactly one booking should succeed"
            );

            System.out.println(
                "Successful bookings: " + successes
                + " (expected 1)"
            );

        } finally {
            executor.shutdownNow();
        }

        System.out.println(
            "\nAll Hotel Booking demo checks passed."
        );
    }
}