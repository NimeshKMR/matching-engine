package com.nimeshkmr.matching_engine.web.dto;

public class CancelOrderRequest {
    
    private long orderID;
    private long accountID;

    public CancelOrderRequest(long orderID, long accountID){
        this.orderID = orderID;
        this.accountID = accountID;
    }

    public CancelOrderRequest(){

    }

    public long getOrderID(){
        return orderID;
    }
    public long getAccountID(){
        return accountID;
    }
}
