import java.io.Serializable;
import java.time.LocalDate;

public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status { CONFIRMED, CANCELLED }

    private final String reservationId;
    private final String guestName;
    private final String roomId;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final double totalPrice;
    private Status status;
    private final LocalDate createdAt;

    public Reservation(String reservationId, String guestName, String roomId,
                       LocalDate checkIn, LocalDate checkOut, double totalPrice) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.totalPrice = totalPrice;
        this.status = Status.CONFIRMED;
        this.createdAt = LocalDate.now();
    }

    public String getRoomId() {
        return roomId;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status s) {
        this.status = s;
    }

    public String shortString() {
        return String.format("%s | %s | Room %s | %s -> %s | %s",
                reservationId, guestName, roomId, checkIn, checkOut, status);
    }

    public String detailedString() {
        return "Reservation ID: " + reservationId + "\n" +
                "Guest: " + guestName + "\n" +
                "Room: " + roomId + "\n" +
                "Check-in: " + checkIn + "\n" +
                "Check-out: " + checkOut + "\n" +
                String.format("Total: %.2f\n", totalPrice) +
                "Status: " + status + "\n" +
                "Created at: " + createdAt;
    }
}
