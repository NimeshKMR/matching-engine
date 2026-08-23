package com.nimeshkmr.matching_engine.web.service;

import org.springframework.stereotype.Service;

import com.nimeshkmr.core.LimitOrder;
import com.nimeshkmr.core.MarketOrder;
import com.nimeshkmr.core.Order;
import com.nimeshkmr.core.OrderBook;
import com.nimeshkmr.core.OrderBookSnapshot;
import com.nimeshkmr.events.OrderResult;
import com.nimeshkmr.matching_engine.web.dto.CancelOrderRequest;
import com.nimeshkmr.matching_engine.web.dto.PlaceOrderRequest;

@Service
public class OrderBookService {
    
    private final OrderBook orderBook;
    
    public OrderBookService(OrderBook orderBook){
        this.orderBook = orderBook;
    }

    public OrderResult placeOrder(PlaceOrderRequest request){
        Order order;
        switch (request.getType()){
            case "LIMIT":
                order = new LimitOrder(request.getOrderID(), request.getAccountID(), request.getSide(), request.getQuantity(), request.getPrice());
                break;
            case "MARKET":
                order = new MarketOrder(request.getOrderID(), request.getAccountID(), request.getSide(), request.getQuantity());
                break;
            default:
                throw new IllegalArgumentException("Unsupported order type!");
        }
        return orderBook.processOrder(order);
    }

    public OrderResult cancelOrder(CancelOrderRequest request){
        return orderBook.cancelOrder(request.getOrderID(), request.getAccountID());
    }

    public OrderBookSnapshot getSnapshot(){
        return orderBook.getSnapshot();
    }
}
