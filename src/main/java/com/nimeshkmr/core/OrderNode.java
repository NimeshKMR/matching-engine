package com.nimeshkmr.core;

public class OrderNode {
    OrderNode next;
    OrderNode prev; 
    LimitOrder order; 

    public OrderNode(LimitOrder order){
        this.order = order;
    }
}
