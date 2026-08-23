package com.nimeshkmr.matching_engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.nimeshkmr.core.OrderBook;
import com.nimeshkmr.events.OrderAccepted;
import com.nimeshkmr.events.OrderResult;
import com.nimeshkmr.matching_engine.web.dto.PlaceOrderRequest;
import com.nimeshkmr.matching_engine.web.service.OrderBookService;

public class OrderBookServiceTest {
    
    @Test
    void PlaceLimitOrder(){
        OrderBook orderBook = new OrderBook();
        OrderBookService service = new OrderBookService(orderBook);
        PlaceOrderRequest request = new PlaceOrderRequest(1, 100, com.nimeshkmr.core.Side.BUY, "LIMIT", 10, new BigDecimal("100"));
        OrderResult result = service.placeOrder(request);

        assertEquals(1, result.orderID());
        assertTrue(result.events().get(0) instanceof OrderAccepted);    }
}
