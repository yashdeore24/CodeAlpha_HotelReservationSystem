import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class Hotel implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<Room> rooms = new ArrayList<>();

    public List<Room> getRooms() { return rooms; }

    public void addRoom(Room r) { rooms.add(r); }

    public List<Room> searchAvailable(LocalDate checkIn, LocalDate checkOut,
                                      List<Reservation> active, RoomCategory category) {
        List<Room> result = new ArrayList<>();
        for (Room room : rooms) {
            if (category != null && room.getCategory() != category) continue;
            boolean occupied = false;
            for (Reservation r : active) {
                if (r.getRoomId().equals(room.getId()) &&
                        checkIn.isBefore(r.getCheckOut()) &&
                        r.getCheckIn().isBefore(checkOut)) {
                    occupied = true; break;
                }
            }
            if (!occupied) result.add(room);
        }
        return result;
    }

    public void save(String fileName) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(this);
        } catch (Exception e) {
            System.err.println("Error saving rooms: " + e.getMessage());
        }
    }

    public static Hotel load(String fileName) {
        File f = new File(fileName);
        if (!f.exists()) return null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            return (Hotel) in.readObject();
        } catch (Exception e) {
            System.err.println("Error loading rooms: " + e.getMessage());
            return null;
        }
    }

    public static Hotel sampleHotel() {
        Hotel h = new Hotel();
        h.addRoom(new Room("101", RoomCategory.STANDARD, 2500));
        h.addRoom(new Room("102", RoomCategory.STANDARD, 2500));
        h.addRoom(new Room("201", RoomCategory.DELUXE, 4000));
        h.addRoom(new Room("202", RoomCategory.DELUXE, 4200));
        h.addRoom(new Room("301", RoomCategory.SUITE, 8000));
        return h;
    }
}
