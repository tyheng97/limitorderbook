package model;

import java.util.LinkedList;

public class PriceLevel {
    public double price;
    public Side side;
    public LinkedList<Order> queue;

    public PriceLevel(double price, Side side) {
        this.price = price;
        this.side = side;
        this.queue = new LinkedList<>();
    }
}
