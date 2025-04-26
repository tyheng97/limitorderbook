package core;

import model.Order;
import model.PriceLevel;
import model.Side;
import util.PriceLevelKey;

import java.util.*;

public class LimitOrderBook {
    private PriorityQueue<PriceLevel> bids; // Max-heap for bids (highest price first)
    private PriorityQueue<PriceLevel> asks; // Min-heap for asks (lowest price first)
    private Map<String, Order> orderMap; // orderId -> Order
    private Map<PriceLevelKey, Integer> volumeMap; // [price, side] -> total volume
    private Map<PriceLevelKey, PriceLevel> priceLevelMap; // [price, side] -> PriceLevel

    public LimitOrderBook() {
        // Max-heap for bids: highest price first
        bids = new PriorityQueue<>((a, b) -> Double.compare(b.price, a.price));
        // Min-heap for asks: lowest price first
        asks = new PriorityQueue<>((a, b) -> Double.compare(a.price, b.price));
        orderMap = new HashMap<>();
        volumeMap = new HashMap<>();
        priceLevelMap = new HashMap<>();
    }

    /**
     * Places an order, attempting to fill it with opposite side orders first.
     * Prints trade details when trades occur.
     */
    public void placeOrder(Order order) {
        orderMap.put(order.orderId, order);
        PriorityQueue<PriceLevel> oppositeBook = (order.side == Side.BUY) ? asks : bids;
        PriorityQueue<PriceLevel> sameBook = (order.side == Side.BUY) ? bids : asks;

        // Try to fill the order with opposite side orders
        while (order.volume > 0 && !oppositeBook.isEmpty()) {
            PriceLevel pl = oppositeBook.peek();
            if ((order.side == Side.BUY && pl.price > order.price) ||
                    (order.side == Side.SELL && pl.price < order.price)) {
                break; // No matching price available
            }
            Iterator<Order> iter = pl.queue.iterator();
            while (order.volume > 0 && iter.hasNext()) {
                Order otherOrder = iter.next();
                int tradeVolume = Math.min(order.volume, otherOrder.volume);
                order.volume -= tradeVolume;
                otherOrder.volume -= tradeVolume;
                PriceLevelKey key = new PriceLevelKey(pl.price, pl.side);
                volumeMap.merge(key, -tradeVolume, Integer::sum);
                System.out.println("Made by " + otherOrder.client + ", taken by " + order.client +
                        ", " + tradeVolume + " shares @ " + otherOrder.price);
                if (otherOrder.volume == 0) {
                    iter.remove();
                    orderMap.remove(otherOrder.orderId);
                }
            }
            if (pl.queue.isEmpty()) {
                oppositeBook.poll();
                priceLevelMap.remove(new PriceLevelKey(pl.price, pl.side));
                volumeMap.remove(new PriceLevelKey(pl.price, pl.side));
            }
        }

        // If order still has volume, add it to the book
        if (order.volume > 0) {
            addOrderToBook(order, sameBook);
        } else {
            orderMap.remove(order.orderId);
        }
    }

    /**
     * Adds an order to the specified order book side.
     */
    private void addOrderToBook(Order order, PriorityQueue<PriceLevel> book) {
        PriceLevelKey key = new PriceLevelKey(order.price, order.side);
        PriceLevel pl = priceLevelMap.get(key);
        if (pl == null) {
            pl = new PriceLevel(order.price, order.side);
            priceLevelMap.put(key, pl);
            book.offer(pl);
        }
        pl.queue.add(order);
        volumeMap.merge(key, order.volume, Integer::sum);
    }

    /**
     * Cancels an order by orderId if it exists and has not been fully filled.
     */
    public void cancelOrder(String orderId) {
        Order order = orderMap.get(orderId);
        if (order == null) return;

        PriceLevelKey key = new PriceLevelKey(order.price, order.side);
        PriceLevel pl = priceLevelMap.get(key);
        if (pl != null && pl.queue.remove(order)) {
            volumeMap.merge(key, -order.volume, Integer::sum);
            if (pl.queue.isEmpty()) {
                PriorityQueue<PriceLevel> book = (order.side == Side.BUY) ? bids : asks;
                book.remove(pl); // O(n) but could be optimized with custom heap
                priceLevelMap.remove(key);
                volumeMap.remove(key);
            }
            orderMap.remove(orderId);
        }
    }

    /**
     * Returns the total volume of open orders at a specific price for the given side.
     */
    public int getVolumeAtPrice(double price, Side side) {
        PriceLevelKey key = new PriceLevelKey(price, side);
        return volumeMap.getOrDefault(key, 0);
    }
}
