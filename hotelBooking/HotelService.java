import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * HotelService
 */
public class HotelService {

    private final Map<String, Hotel> hotelsById = new HashMap<>();
    private final Map<String, Room> roomsById = new HashMap<>();
    private final Map<String, Booking> bookingsById = new HashMap<>();
    private final Map<String, List<Booking>> bookingsByRoomId = new HashMap<>();

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
    
    public synchronized List<Room> searchAvailableRooms(String city, RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
    if (city == null || city.trim().isEmpty()) throw new IllegalArgumentException("Invalid city");

    if (roomType == null) throw new IllegalArgumentException("Invalid room type");

    List<Room> availableRooms = new ArrayList<>();

    // 2. Check all registered rooms
    for (Room room : roomsById.values()) {

        Hotel hotel = hotelsById.get(room.getHotelId());

        // 3. Match city
        if (hotel == null || !hotel.getCity().equalsIgnoreCase(city.trim())) {
            continue;
        }

        // 4. Match room type
        if (room.getRoomType() != roomType) {
            continue;
        }

        // 5. Check date availability
        if (isRoomAvailable(room.getId(), checkIn, checkOut)) {
            availableRooms.add(room);
        }
    }

    return availableRooms;
}


    public synchronized Booking bookRoom(String userId, String roomId, LocalDate checkInDate, LocalDate checkOuDate) {
        // validate input
        Room room = roomsById.get(roomId);

        if (room == null) throw new IllegalArgumentException("Room Not Found");

        if (!isRoomAvailable(roomId, checkInDate, checkOuDate)) throw new IllegalArgumentException("Room Not Is Not Available");

        long nights = ChronoUnit.DAYS.between(checkInDate, checkOuDate);

        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Booking booking = new Booking(UUID.randomUUID().toString(), userId, room.getHotelId(), roomId, checkInDate, checkOuDate, totalPrice, BookingStatus.BOOKED);

        bookingsById.put(booking.getId(), booking);

        bookingsByRoomId.computeIfAbsent(roomId, id -> new ArrayList<>()).add(booking);

        return booking;

    }

    public synchronized boolean cancelBooking(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            return false;
        }
        Booking booking = bookingsById.get(bookingId);
        if (booking == null) return false;
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) return false;
        booking.setBookingStatus(BookingStatus.CANCELLED);
        return true;
    }

    boolean isRoomAvailable(String roomId, LocalDate checkIn, LocalDate checkOut) {
        List<Booking> bookings = bookingsByRoomId.getOrDefault(roomId, Collections.emptyList());
        for (Booking booking : bookings) {
            if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
                continue;
            }

            boolean overlap = checkIn.isBefore(booking.getCheckOut()) && checkOut.isAfter(booking.getCheckIn());
            if (overlap) {
                return false;
            }
        }
        return true;
    }
    
}