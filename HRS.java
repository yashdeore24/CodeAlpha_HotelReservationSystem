import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HRS {
    private static final String ROOMS_FILE = "rooms.dat";
    private static final String BOOKINGS_FILE = "bookings.dat";
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static Hotel hotel;
    private static BookingManager bookingManager;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadOrCreateData();
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> searchRooms();
                case "2" -> makeReservation();
                case "3" -> cancelReservation();
                case "4" -> viewBookingDetails();
                case "5" -> listAllBookings();
                case "6" -> listAllRooms();
                case "0" -> {
                    running = false;
                    saveData();
                    System.out.println("Goodbye! Data saved.");
                }
                default -> System.out.println("Invalid option. Please enter a valid number.");
            }
            System.out.println();
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("""
            Menu:
            1) Search available rooms
            2) Make a reservation
            3) Cancel a reservation
            4) View booking details
            5) List all bookings
            6) List all rooms
            0) Exit
            Choose: """);
    }

    private static void loadOrCreateData() {
        hotel = Hotel.load(ROOMS_FILE);
        if (hotel == null) {
            hotel = Hotel.sampleHotel();
            hotel.save(ROOMS_FILE);
        }
        bookingManager = BookingManager.load(BOOKINGS_FILE);
        if (bookingManager == null) {
            bookingManager = new BookingManager();
        }
    }

    private static void saveData() {
        if (hotel != null) hotel.save(ROOMS_FILE);
        if (bookingManager != null) bookingManager.save(BOOKINGS_FILE);
    }

    private static void searchRooms() {
        System.out.print("Enter check-in date (yyyy-MM-dd): ");
        LocalDate checkIn = readDate();
        System.out.print("Enter check-out date (yyyy-MM-dd): ");
        LocalDate checkOut = readDate();
        if (!checkOut.isAfter(checkIn)) {
            System.out.println("Error: Check-out must be after check-in.");
            return;
        }
        System.out.print("Choose category (STANDARD, DELUXE, SUITE or ALL): ");
        String cat = scanner.nextLine().trim().toUpperCase(Locale.ROOT);
        RoomCategory category = null;
        if (!cat.equals("ALL") && !cat.isEmpty()) {
            try {
                category = RoomCategory.valueOf(cat);
            } catch (IllegalArgumentException e) {
                System.out.println("Unknown category. Showing all available rooms.");
            }
        }
        List<Room> available = hotel.searchAvailable(checkIn, checkOut,
                bookingManager.getActiveReservations(), category);
        if (available.isEmpty()) {
            System.out.println("No rooms available for the selected dates/category.");
        } else {
            System.out.println("Available rooms:");
            available.forEach(System.out::println);
        }
    }

    private static void makeReservation() {
        System.out.print("Guest name: ");
        String guest = scanner.nextLine().trim();
        if (guest.isEmpty()) {
            System.out.println("Error: Guest name cannot be empty.");
            return;
        }
        System.out.print("Enter check-in date (yyyy-MM-dd): ");
        LocalDate checkIn = readDate();
        System.out.print("Enter check-out date (yyyy-MM-dd): ");
        LocalDate checkOut = readDate();
        if (!checkOut.isAfter(checkIn)) {
            System.out.println("Error: Check-out must be after check-in.");
            return;
        }
        System.out.print("Choose category (STANDARD, DELUXE, SUITE or ANY): ");
        String cat = scanner.nextLine().trim().toUpperCase(Locale.ROOT);
        RoomCategory category = null;
        if (!cat.equals("ANY") && !cat.isEmpty()) {
            try {
                category = RoomCategory.valueOf(cat);
            } catch (IllegalArgumentException e) {
                System.out.println("Unknown category. Searching any available room.");
            }
        }
        List<Room> avail = hotel.searchAvailable(checkIn, checkOut,
                bookingManager.getActiveReservations(), category);
        if (avail.isEmpty()) {
            System.out.println("No rooms available for the selected options.");
            return;
        }
        System.out.println("Choose room id from available rooms:");
        avail.forEach(System.out::println);
        System.out.print("Room id: ");
        String idStr = scanner.nextLine().trim();
        Room room = avail.stream()
                .filter(r -> r.getId().equals(idStr))
                .findFirst().orElse(null);
        if (room == null) {
            System.out.println("Error: Invalid room id.");
            return;
        }
        long nights = checkOut.toEpochDay() - checkIn.toEpochDay();
        double total = nights * room.getPricePerNight();
        System.out.printf("Total for %d nights: %.2f%n", nights, total);
        System.out.print("Proceed to payment? (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("Booking cancelled by user.");
            return;
        }
        if (!PaymentSimulator.charge(total)) {
            System.out.println("Payment failed. Please try again later.");
            return;
        }
        Reservation res = bookingManager.createReservation(guest,
                room.getId(), checkIn, checkOut, total);
        bookingManager.save(BOOKINGS_FILE);
        System.out.println("Booking confirmed. Reservation details:");
        System.out.println(res.detailedString());
    }

    private static void cancelReservation() {
        System.out.print("Enter reservation id to cancel: ");
        String rid = scanner.nextLine().trim();
        if (rid.isEmpty()) {
            System.out.println("Error: Reservation id required.");
            return;
        }
        Optional<Reservation> r = bookingManager.getReservationById(rid);
        if (r.isEmpty()) {
            System.out.println("Reservation not found.");
            return;
        }
        Reservation res = r.get();
        if (res.getStatus() == Reservation.Status.CANCELLED) {
            System.out.println("Reservation is already cancelled.");
            return;
        }
        System.out.print("Are you sure you want to cancel this reservation? (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("Cancellation aborted.");
            return;
        }
        if (PaymentSimulator.refund(res.getTotalPrice())) {
            res.setStatus(Reservation.Status.CANCELLED);
            bookingManager.save(BOOKINGS_FILE);
            System.out.println("Reservation cancelled and refund processed.");
        } else {
            System.out.println("Refund failed. Please contact support.");
        }
    }

    private static void viewBookingDetails() {
        System.out.print("Enter reservation id: ");
        String rid = scanner.nextLine().trim();
        if (rid.isEmpty()) {
            System.out.println("Error: Reservation id required.");
            return;
        }
        bookingManager.getReservationById(rid)
                .ifPresentOrElse(r -> System.out.println(r.detailedString()),
                        () -> System.out.println("Reservation not found."));
    }

    private static void listAllBookings() {
        List<Reservation> all = bookingManager.getAllReservations();
        if (all.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        all.forEach(r -> System.out.println(r.shortString()));
    }

    private static void listAllRooms() {
        hotel.getRooms().forEach(System.out::println);
    }

    private static LocalDate readDate() {
        while (true) {
            try {
                return LocalDate.parse(scanner.nextLine().trim(), DF);
            } catch (Exception e) {
                System.out.print("Invalid date format. Please use yyyy-MM-dd: ");
            }
        }
    }
}
