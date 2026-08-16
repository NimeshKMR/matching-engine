package com.nimeshkmr.core;
import java.math.BigDecimal;


public class Trade {
    Long buyOrderID;
    Long sellOrderID;
    BigDecimal price;
    long quantity;

    public Trade(Long buyOrderID, Long sellOrderID, BigDecimal price, Long quantity){
        this.buyOrderID = buyOrderID;
        this.sellOrderID = sellOrderID;
        this.price = price;
        this.quantity = quantity;
    }
}
