import java.util.*;

public class Main {

    public static void main(String[] args) {

        // 1. Configure parking spots
        List<ParkingSpot> spots = Arrays.asList(
            new ParkingSpot("S1", SpotType.MOTORCYCLE),
            new ParkingSpot("S2", SpotType.MOTORCYCLE),
            new ParkingSpot("M1", SpotType.CAR),
            new ParkingSpot("L1", SpotType.LARGE)
        );

        // 2. Initialize parking lot
        ParkingLot parkingLot = new ParkingLot(spots, 100);

        // 3. Car enters
        Ticket carTicket =
            parkingLot.enter(VehicleType.CAR);

        System.out.println(
            "Car parked at: " + carTicket.getSpotId()
        );

        // 4. Motorcycle enters
        Ticket bikeTicket =
            parkingLot.enter(VehicleType.MOTORCYCLE);

        System.out.println(
            "Bike parked at: " + bikeTicket.getSpotId()
        );

        // 5. Car exits
        long fee = parkingLot.exit(carTicket.getId());

        System.out.println("Parking fee: " + fee);

        // 6. Another car enters
        Ticket nextCar =
            parkingLot.enter(VehicleType.CAR);

        System.out.println(
            "Next car parked at: " + nextCar.getSpotId()
        );

        // 7. Attempt to reuse a completed ticket
        try {
            parkingLot.exit(carTicket.getId());
        } catch (RuntimeException e) {
            System.out.println(
                "Expected error: " + e.getMessage()
            );
        }
    }
}