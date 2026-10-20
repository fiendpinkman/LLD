import java.math.BigDecimal;

/**
 * Room
 */
public class Room {

    private final String id;
    private final String hotelId;
    private final RoomType roomType;
    private final BigDecimal pricePerNight;

    public Room(String id, String hotelId, RoomType roomType, BigDecimal pricePerNight) {
        this.id = id;
        this.hotelId = hotelId;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;

    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public String getHotelId() {
        return hotelId;
    }

    public String getId() {
        return id;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    @Override
    public String toString() {
        return "Room " + id + " (" + roomType
            + ", " + pricePerNight + "/night)";
    }

}