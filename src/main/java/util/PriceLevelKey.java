package util;

import model.Side;
import java.util.Objects;

public class PriceLevelKey {
    double price;
    Side side;

    public PriceLevelKey(double price, Side side) {
        this.price = price;
        this.side = side;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceLevelKey that = (PriceLevelKey) o;
        return Double.compare(that.price, price) == 0 && side == that.side;
    }

    @Override
    public int hashCode() {
        return Objects.hash(price, side);
    }}
