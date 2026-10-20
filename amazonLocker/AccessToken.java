import java.time.Instant;

public class AccessToken {
    private String code;
    private Instant expiration;
    private Compartment compartment;

    public AccessToken(String code, Instant expiration, Compartment compartment) {
        this.code = code;
        this.expiration = expiration;
        this.compartment = compartment;
    }

    public boolean isExpired() {
        return !Instant.now().isBefore(expiration);
    }

    public Compartment getCompartment() {
        return compartment;
    }

    public String getCode() {
        return code;
    }
}
