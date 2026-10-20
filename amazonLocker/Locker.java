import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Locker {
    private final Compartment[] compartments;
    private final Map<String, AccessToken> accessTokenMapping;
    private final Random random;

    public Locker(Compartment[] compartments) {
        this.compartments = compartments;
        this.accessTokenMapping = new HashMap<>();
        this.random = new Random();
    }

    public String depositPackage(Size size) {
        Compartment compartment = getAvailableCompartment(size);
        if (compartment == null) {
            throw new RuntimeException("No available compartment of size " + size);
        }

        compartment.open();
        compartment.markOccupied();
        AccessToken accessToken = generateAccessToken(compartment);
        accessTokenMapping.put(accessToken.getCode(), accessToken);

        return accessToken.getCode();
    }

    public void pickup(String tokenCode) {
        if (tokenCode == null || tokenCode.isEmpty()) {
            throw new RuntimeException("Invalid access token code");
        }

        AccessToken accessToken = accessTokenMapping.get(tokenCode);
        if (accessToken == null) {
            throw new RuntimeException("Invalid access token code");
        }

        if (accessToken.isExpired()) {
            throw new RuntimeException("Access token has expired");
        }

        Compartment compartment = accessToken.getCompartment();
        compartment.open();
        clearDeposit(accessToken);
    }

    public void openExpiredCompartments() {
        for (AccessToken accessToken : accessTokenMapping.values()) {
            if (accessToken.isExpired()) {
                Compartment compartment = accessToken.getCompartment();
                compartment.open();
            }
        }
    }

    private Compartment getAvailableCompartment(Size size) {
        for (Compartment c : compartments) {
            if (c.getSize() == size && !c.isOccupied()) {
                return c;
            }
        }
        return null;
    }

    private AccessToken generateAccessToken(Compartment compartment) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        Instant expiration = Instant.now().plus(7, ChronoUnit.DAYS);
        return new AccessToken(code, expiration, compartment);
    }

    private void clearDeposit(AccessToken accessToken) {
        Compartment compartment = accessToken.getCompartment();
        compartment.markFree();
        accessTokenMapping.remove(accessToken.getCode());
    }
}
