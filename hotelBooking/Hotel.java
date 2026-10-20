/**
 * Hotel
 */
public class Hotel {

    private final String id;
    private final String name;
    private final String city;

    public Hotel(String id, String name, String city) {
        // validations
        this.id = id;
        this.name = name;
        this.city = city;
    }

    public String getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

}