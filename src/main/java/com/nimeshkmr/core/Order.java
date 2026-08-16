package com.nimeshkmr.core;
import java.math.BigDecimal;

public abstract class Order {
    private final long orderID;
    private final long accountID;
    private final Side side;
    private long quantity;
    protected Order(long orderID, long accountID, Side side, long quantity){
        if(quantity <= 0){
            throw new IllegalArgumentException("Order quantity must be positive!");
        }
        if(side == null){
            throw new IllegalArgumentException("Order side cannot be null!");
        }
        this.orderID = orderID;
        this.accountID = accountID;
        this.side = side;
        this.quantity = quantity;
    }

    public long getOrderID() {
        return orderID;
    }

    public long getAccountID() {
        return accountID;
    }

    public Side getSide() {
        return side;
    }
    public long getQuantity() {
        return quantity;
    }

    public void reduceQuantity(long amount){
        if(amount <= 0 || amount > quantity){
            throw new IllegalArgumentException("Invalid quantity reductiion!");
        }
        quantity -= amount; 
    }
}