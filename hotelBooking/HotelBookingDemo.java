
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

public class HotelBookingDemo {

    enum RoomType {
        STANDARD, DELUXE, SUITE
    }

    enum BookingStatus {
        CONFIRMED, CANCELLED
    }

    // 1. HOTEL
    static final class Hotel {
        private final String id;
        private final String name;
        private final String city;

        Hotel(String id, String name, String city) {
            this.id = requireText(id, "hotel ID");
            this.name = requireText(name, "hotel name");
            this.city = requireText(city, "city");
        }

        String getId() { return id; }
        String getName() { return name; }
        String getCity() { return city; }
    }

    // 2. ROOM
    static final class Room {
        private final String id;
        private final String hotelId;
        private final RoomType roomType;
        private final BigDecimal pricePerNight;

        Room(String id, String hotelId,
             RoomType roomType, BigDecimal pricePerNight) {

            this.id = requireText(id, "room ID");
            this.hotelId = requireText(hotelId, "hotel ID");
            this.roomType = Objects.requireNonNull(roomType);

            if (pricePerNight == null
                    || pricePerNight.signum() <= 0) {
                throw new IllegalArgumentException(
                    "Nightly price must be positive");
            }

            this.pricePerNight = pricePerNight;
        }

        String getId() { return id; }
        String getHotelId() { return hotelId; }
        RoomType getRoomType() { return roomType; }
        BigDecimal getPricePerNight() { return pricePerNight; }

        @Override
        public String toString() {
            return "Room " + id + " (" + roomType
                + ", " + pricePerNight + "/night)";
        }
    }

    // 3. BOOKING
    static final class Booking {
        private final String id;
        private final String userId;
        private final String hotelId;
        private final String roomId;
        private final LocalDate checkIn;
        private final LocalDate checkOut;
        private final BigDecimal totalPrice;

        private volatile BookingStatus status;

        Booking(String id, String userId, String hotelId,
                String roomId, LocalDate checkIn,
                LocalDate checkOut, BigDecimal totalPrice) {

            this.id = id;
            this.userId = userId;
            this.hotelId = hotelId;
            this.roomId = roomId;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.totalPrice = totalPrice;
            this.status = BookingStatus.CONFIRMED;
        }

        String getId() { return id; }
        String getRoomId() { return roomId; }
        LocalDate getCheckIn() { return checkIn; }
        LocalDate getCheckOut() { return checkOut; }
        BookingStatus getStatus() { return status; }
        BigDecimal getTotalPrice() { return totalPrice; }

        private void cancel() {
            status = BookingStatus.CANCELLED;
        }

        @Override
        public String toString() {
            return "Booking " + id
                + ": user=" + userId
                + ", hotel=" + hotelId
                + ", room=" + roomId
                + ", " + checkIn + " to " + checkOut
                + ", total=" + totalPrice
                + ", status=" + status;
        }
    }

    // 4. BUSINESS SERVICE
    static final class HotelService {

        private final Map<String, Hotel> hotelsById =
            new HashMap<>();

        private final Map<String, Room> roomsById =
            new HashMap<>();

        private final Map<String, Booking> bookingsById =
            new HashMap<>();

        private final Map<String, List<Booking>> bookingsByRoomId =
            new HashMap<>();

        private final Clock clock;

        HotelService(Clock clock) {
            this.clock = Objects.requireNonNull(clock);
        }

        // REGISTER HOTEL
        public synchronized void addHotel(Hotel hotel) {

            Objects.requireNonNull(hotel);

            if (hotelsById.containsKey(hotel.getId())) {
                throw new IllegalArgumentException(
                    "Duplicate hotel ID");
            }

            hotelsById.put(hotel.getId(), hotel);
        }

        // REGISTER ROOM
        public synchronized void addRoom(Room room) {

            Objects.requireNonNull(room);

            if (!hotelsById.containsKey(room.getHotelId())) {
                throw new IllegalArgumentException(
                    "Hotel not found");
            }

            if (roomsById.containsKey(room.getId())) {
                throw new IllegalArgumentException(
                    "Duplicate room ID");
            }

            roomsById.put(room.getId(), room);
        }

        // SEARCH AVAILABLE ROOMS
        public synchronized List<Room> searchAvailableRooms(
                String city,
                RoomType roomType,
                LocalDate checkIn,
                LocalDate checkOut) {

            String requestedCity = requireText(city, "city");
            Objects.requireNonNull(roomType);
            validateDates(checkIn, checkOut);

            List<Room> availableRooms = new ArrayList<>();

            for (Room room : roomsById.values()) {

                Hotel hotel =
                    hotelsById.get(room.getHotelId());

                if (!hotel.getCity().equalsIgnoreCase(requestedCity)) {
                    continue;
                }

                if (room.getRoomType() != roomType) {
                    continue;
                }

                if (isRoomAvailable(
                        room.getId(), checkIn, checkOut)) {
                    availableRooms.add(room);
                }
            }

            availableRooms.sort(
                Comparator.comparing(Room::getId));

            return availableRooms;
        }

        // BOOK ROOM
        public synchronized Booking bookRoom(
                String userId,
                String roomId,
                LocalDate checkIn,
                LocalDate checkOut) {

            String validUserId = requireText(userId, "user ID");
            String validRoomId = requireText(roomId, "room ID");

            validateDates(checkIn, checkOut);

            Room room = roomsById.get(validRoomId);

            if (room == null) {
                throw new IllegalArgumentException(
                    "Room not found");
            }

            if (!isRoomAvailable(
                    validRoomId, checkIn, checkOut)) {
                throw new IllegalStateException(
                    "Room already booked for these dates");
            }

            long nights =
                ChronoUnit.DAYS.between(checkIn, checkOut);

            BigDecimal totalPrice =
                room.getPricePerNight()
                    .multiply(BigDecimal.valueOf(nights));

            Booking booking = new Booking(
                UUID.randomUUID().toString(),
                validUserId,
                room.getHotelId(),
                validRoomId,
                checkIn,
                checkOut,
                totalPrice
            );

            bookingsById.put(booking.getId(), booking);

            bookingsByRoomId
                .computeIfAbsent(
                    validRoomId,
                    id -> new ArrayList<>()
                )
                .add(booking);

            return booking;
        }

        // CANCEL BOOKING
        public synchronized boolean cancelBooking(
                String bookingId) {

            if (bookingId == null
                    || bookingId.trim().isEmpty()) {
                return false;
            }

            Booking booking =
                bookingsById.get(bookingId.trim());

            if (booking == null
                    || booking.getStatus() == BookingStatus.CANCELLED) {
                return false;
            }

            booking.cancel();
            return true;
        }

        // CHECK ROOM AVAILABILITY
        private boolean isRoomAvailable(
                String roomId,
                LocalDate checkIn,
                LocalDate checkOut) {

            List<Booking> bookings =
                bookingsByRoomId.getOrDefault(
                    roomId,
                    Collections.emptyList()
                );

            for (Booking booking : bookings) {

                if (booking.getStatus() == BookingStatus.CANCELLED) {
                    continue;
                }

                boolean overlaps =
                    checkIn.isBefore(booking.getCheckOut())
                    && booking.getCheckIn().isBefore(checkOut);

                if (overlaps) {
                    return false;
                }
            }

            return true;
        }

        // VALIDATE DATES
        private void validateDates(
                LocalDate checkIn,
                LocalDate checkOut) {

            if (checkIn == null
                    || checkOut == null
                    || !checkIn.isBefore(checkOut)) {
                throw new IllegalArgumentException(
                    "Check-out must be after check-in");
            }

            if (checkIn.isBefore(LocalDate.now(clock))) {
                throw new IllegalArgumentException(
                    "Check-in cannot be in the past");
            }
        }
    }

    // COMMON VALIDATION
    private static String requireText(
            String value, String field) {

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Invalid " + field);
        }

        return value.trim();
    }

    // SIMPLE TEST ASSERTION
    private static void check(
            boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    // 5. MAIN METHOD — END-TO-END DEMONSTRATION
    public static void main(String[] args) throws Exception {

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
