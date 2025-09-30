import java.io.Serializable;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final RoomCategory category;
    private final double pricePerNight;

    public Room(String id, RoomCategory category, double pricePerNight) {
        this.id = id;
        this.category = category;
        this.pricePerNight = pricePerNight;
    }

    public String getId() {
        return id;
    }

    public RoomCategory getCategory() {
        return category;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    @Override
    public String toString() {
        return String.format("Room{id=%s, category=%s, price=%.2f}", id, category, pricePerNight);
    }
}
