package com.nimeshkmr.core;
import java.math.BigDecimal;

public class LimitOrder extends Order{
    private final BigDecimal price;

    public LimitOrder(long orderID, long accountID, Side side, long quantity, BigDecimal price){
        super(orderID, accountID, side, quantity);
        if(price == null || price.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Limit order price must be positive!");
        }
        this.price = price;
    }

    public BigDecimal getPrice(){
        return price;
    }
}
