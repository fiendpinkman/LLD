import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Booking
 */
public class Booking {

    private final String id;
    private final String userId;
    private final String hotelId;
    private final String roomId;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final BigDecimal totalPrice;
    private BookingStatus bookingStatus;

    public Booking(String id, String userId, String hotelId, String roomId, LocalDate checkIn, LocalDate checkOut, BigDecimal totalPrice, BookingStatus bookingStatus) {
        this.id = id;
        this.userId = userId;
        this.hotelId = hotelId;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.totalPrice = totalPrice;
        this.bookingStatus = bookingStatus;
    }

    public String getId() {
        return id;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

    @Override
    public String toString() {
        return "Booking " + id
            + ": user=" + userId
            + ", hotel=" + hotelId
            + ", room=" + roomId
            + ", " + checkIn + " to " + checkOut
            + ", total=" + totalPrice
            + ", status=" + bookingStatus;
    }

}