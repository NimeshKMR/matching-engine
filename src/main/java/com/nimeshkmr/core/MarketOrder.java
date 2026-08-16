package com.nimeshkmr.core;

public class MarketOrder extends Order {
    public MarketOrder(long orderID, long accountID, Side side, long quantity){
        super(orderID, accountID, side, quantity);
    }
}
