package model;

import java.util.UUID;

public class Order {
    public String orderId;
    long timestamp;
    public Side side;
    public double price;
    public int volume;
    public String client;

    public Order(long timestamp, Side side, double price, int volume, String client) {
        this.orderId = UUID.randomUUID().toString();
        this.timestamp = timestamp;
        this.side = side;
        this.price = price;
        this.volume = volume;
        this.client = client;
    }
}