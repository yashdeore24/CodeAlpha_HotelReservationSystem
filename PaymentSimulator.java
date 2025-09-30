import java.util.Random;

public class PaymentSimulator {
    private static final Random random = new Random();

    public static boolean charge(double amount) {
        System.out.printf("Charging amount: %.2f ...%n", amount);
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        return random.nextInt(100) < 95;
    }

    public static boolean refund(double amount) {
        System.out.printf("Processing refund: %.2f ...%n", amount);
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        return random.nextInt(100) < 98;
    }
}
