package org.limitorderbook;

import core.LimitOrderBook;
import model.Order;
import model.Side;

public class Main {
    private static long currentTimestamp = System.currentTimeMillis();

    // Helper to create orders with incrementing timestamps
    private static Order createOrder(Side side, double price, int volume, String client) {
        return new Order(currentTimestamp++, side, price, volume, client);
    }

    public static void main(String[] args) {
        LimitOrderBook lob = new LimitOrderBook();
        System.out.println("=== Starting Limit Order Book Tests ===");

        // Test 1: Basic Trade Matching
        System.out.println("\nTest 1: Basic Trade Matching");
        Order s1 = createOrder(Side.SELL, 100.0, 50, "Alice"); // Sell 50 @ 100.0
        lob.placeOrder(s1);
        lob.placeOrder(createOrder(Side.BUY, 100.0, 30, "Bob")); // Buy 30 @ 100.0
        // Expected: Trade 30 shares @ 100.0, s1 has 20 remaining
        System.out.println("Volume at 100.0 (SELL): " + lob.getVolumeAtPrice(100.0, Side.SELL)); // Should be 20
        System.out.println("Volume at 100.0 (BUY): " + lob.getVolumeAtPrice(100.0, Side.BUY));   // Should be 0

        // Test 2: Partial Fills Across Multiple Orders
        System.out.println("\nTest 2: Partial Fills Across Multiple Orders");
        lob.placeOrder(createOrder(Side.SELL, 99.0, 40, "Charlie")); // Sell 40 @ 99.0
        lob.placeOrder(createOrder(Side.BUY, 101.0, 100, "Dave"));   // Buy 100 @ 101.0
        // Expected:
        // - Trade 20 @ 100.0 with s1 (s1 fully filled)
        // - Trade 40 @ 99.0 with Charlie (fully filled)
        // - Dave has 40 remaining, added to book at 101.0
        System.out.println("Volume at 99.0 (SELL): " + lob.getVolumeAtPrice(99.0, Side.SELL));   // Should be 0
        System.out.println("Volume at 100.0 (SELL): " + lob.getVolumeAtPrice(100.0, Side.SELL)); // Should be 0
        System.out.println("Volume at 101.0 (BUY): " + lob.getVolumeAtPrice(101.0, Side.BUY));   // Should be 40

        // Test 3: FIFO Order Matching at Same Price
        System.out.println("\nTest 3: FIFO Order Matching at Same Price");
        Order s3 = createOrder(Side.SELL, 102.0, 20, "Eve");    // Sell 20 @ 102.0
        Order s4 = createOrder(Side.SELL, 102.0, 30, "Frank");  // Sell 30 @ 102.0
        lob.placeOrder(s3);
        lob.placeOrder(s4);
        lob.placeOrder(createOrder(Side.BUY, 102.0, 25, "Grace")); // Buy 25 @ 102.0
        // Expected:
        // - Trade 20 @ 102.0 with s3 (s3 fully filled, FIFO)
        // - Trade 5 @ 102.0 with s4 (s4 has 25 remaining)
        System.out.println("Volume at 102.0 (SELL): " + lob.getVolumeAtPrice(102.0, Side.SELL)); // Should be 25
        System.out.println("Volume at 102.0 (BUY): " + lob.getVolumeAtPrice(102.0, Side.BUY));   // Should be 0

        // Test 4: No Matching Orders
        System.out.println("\nTest 4: No Matching Orders");
        Order s5 = createOrder(Side.SELL, 105.0, 50, "Hannah"); // Sell 50 @ 105.0
        Order b4 = createOrder(Side.BUY, 98.0, 60, "Ian");     // Buy 60 @ 98.0
        lob.placeOrder(s5);
        lob.placeOrder(b4);
        // Expected: No trades, both orders added to book
        System.out.println("Volume at 105.0 (SELL): " + lob.getVolumeAtPrice(105.0, Side.SELL)); // Should be 50
        System.out.println("Volume at 98.0 (BUY): " + lob.getVolumeAtPrice(98.0, Side.BUY));     // Should be 60

        // Test 5: Cancel Orders
        System.out.println("\nTest 5: Cancel Orders");
        lob.cancelOrder(s4.orderId); // Cancel s4 (25 @ 102.0)
        lob.cancelOrder(b4.orderId); // Cancel b4 (60 @ 98.0)
        // Expected: s4 and b4 removed, volumes updated
        System.out.println("Volume at 102.0 (SELL): " + lob.getVolumeAtPrice(102.0, Side.SELL)); // Should be 0
        System.out.println("Volume at 98.0 (BUY): " + lob.getVolumeAtPrice(98.0, Side.BUY));     // Should be 0
        System.out.println("Volume at 105.0 (SELL): " + lob.getVolumeAtPrice(105.0, Side.SELL)); // Should be 50

        // Test 6: Empty Book and Non-Existent Cancellation
        System.out.println("\nTest 6: Empty Book and Non-Existent Cancellation");
        lob.cancelOrder(s5.orderId); // Cancel s5 (50 @ 105.0)
        System.out.println("Volume at 105.0 (SELL): " + lob.getVolumeAtPrice(105.0, Side.SELL)); // Should be 0
        lob.cancelOrder("X1"); // Non-existent order
        // Expected: No-op for non-existent order, book empty for 105.0

        // Test 7: Matching After Cancellation
        System.out.println("\nTest 7: Matching After Cancellation");
        lob.placeOrder(createOrder(Side.SELL, 101.0, 50, "Jill"));  // Sell 50 @ 101.0
        lob.placeOrder(createOrder(Side.BUY, 101.0, 60, "Kevin"));  // Buy 60 @ 101.0
        // Expected: Trade 40 @ 101.0 with Dave, trade 10 @ 101.0 with Jill (Jill fully filled)
        System.out.println("Volume at 101.0 (SELL): " + lob.getVolumeAtPrice(101.0, Side.SELL)); // Should be 0
        System.out.println("Volume at 101.0 (BUY): " + lob.getVolumeAtPrice(101.0, Side.BUY));   // Should be 10

        System.out.println("\n=== Tests Completed ===");
    }
}