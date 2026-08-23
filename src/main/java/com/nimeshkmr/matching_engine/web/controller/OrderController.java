package com.nimeshkmr.matching_engine.web.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nimeshkmr.core.OrderBookSnapshot;
import com.nimeshkmr.events.OrderResult;
import com.nimeshkmr.matching_engine.web.dto.CancelOrderRequest;
import com.nimeshkmr.matching_engine.web.dto.PlaceOrderRequest;
import com.nimeshkmr.matching_engine.web.dto.PlaceOrderResponse;
import com.nimeshkmr.matching_engine.web.mapper.OrderResultMapper;
import com.nimeshkmr.matching_engine.web.service.OrderBookService;

import jakarta.validation.Valid;

@RestController
public class OrderController {
    private final OrderBookService orderBookService;
    private final OrderResultMapper mapper = new OrderResultMapper();

    public OrderController(OrderBookService orderBookService){
        this.orderBookService = orderBookService;
    }
    
    @PostMapping("/orders")
    public PlaceOrderResponse placeOrder(@Valid @RequestBody PlaceOrderRequest request){
        OrderResult result = orderBookService.placeOrder(request);
        return mapper.map(result);
    }

    @DeleteMapping("/orders/{orderID}")
    public PlaceOrderResponse cancelOrder(@PathVariable long orderID, @RequestParam long accountID){
        CancelOrderRequest request = new CancelOrderRequest(orderID, accountID);
        OrderResult result = orderBookService.cancelOrder(request);
        return mapper.map(result);
    }

    @GetMapping("/orderbook")
    public OrderBookSnapshot getOrderBook(){
        return orderBookService.getSnapshot();
    }
    

}
