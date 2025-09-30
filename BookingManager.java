import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BookingManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<String, Reservation> reservations = new LinkedHashMap<>();
    private static final AtomicInteger idCounter = new AtomicInteger(1000);

    public static BookingManager load(String fileName) {
        File f = new File(fileName);
        if (!f.exists()) return null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            BookingManager bm = (BookingManager) in.readObject();
            int max = bm.reservations.keySet().stream()
                    .mapToInt(s -> s.replaceAll("\\D+", "").isEmpty()
                            ? 1000 : Integer.parseInt(s.replaceAll("\\D+", "")))
                    .max().orElse(1000);
            idCounter.set(max + 1);
            return bm;
        } catch (Exception e) {
            System.err.println("Error loading bookings: " + e.getMessage());
            return null;
        }
    }

    public void save(String fileName) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(this);
        } catch (Exception e) {
            System.err.println("Error saving bookings: " + e.getMessage());
        }
    }

    public Reservation createReservation(String guest, String roomId,
                                         java.time.LocalDate checkIn, java.time.LocalDate checkOut, double total) {
        String rid = "R" + idCounter.getAndIncrement();
        Reservation r = new Reservation(rid, guest, roomId, checkIn, checkOut, total);
        reservations.put(rid, r);
        return r;
    }

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations.values());
    }

    public Optional<Reservation> getReservationById(String id) {
        return Optional.ofNullable(reservations.get(id));
    }

    public List<Reservation> getActiveReservations() {
        List<Reservation> out = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            if (r.getStatus() == Reservation.Status.CONFIRMED) out.add(r);
        }
        return out;
    }
}
