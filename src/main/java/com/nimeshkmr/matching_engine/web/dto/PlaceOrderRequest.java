package com.nimeshkmr.matching_engine.web.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.nimeshkmr.core.Side;

public class PlaceOrderRequest {
    @Positive
    private long orderID;

    @Positive
    private long accountID;

    @NotNull
    private Side side;

    @NotBlank
    private String type;

    @Positive
    private long quantity;

    private BigDecimal price;

    public PlaceOrderRequest(long orderID, long accountID, Side side, String type, long quantity, BigDecimal price){
        this.orderID = orderID;
        this.accountID = accountID;
        this.side = side;
        this.type = type;
        this.quantity = quantity;
        this.price = price; 
    }

    public PlaceOrderRequest(){

    }

    public long getOrderID(){
        return orderID;
    }
    public long getAccountID(){
        return accountID;
    }
    public Side getSide(){
        return side;
    }
    public String getType(){
        return type;
    }
    public long getQuantity(){
        return quantity;
    }
    public BigDecimal getPrice(){
        return price;
    }
}
