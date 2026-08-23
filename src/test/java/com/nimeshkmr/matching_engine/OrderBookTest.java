package com.nimeshkmr.matching_engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.nimeshkmr.core.LimitOrder;
import com.nimeshkmr.core.OrderBook;
import com.nimeshkmr.core.OrderBookSnapshot;
import com.nimeshkmr.core.Side;

public class OrderBookTest {
    @Test
    void snapshotOfEmptyBookIsEmpty() {
        OrderBook orderBook = new OrderBook();

        OrderBookSnapshot snapshot = orderBook.getSnapshot();

        assertTrue(snapshot.bids().isEmpty());
        assertTrue(snapshot.asks().isEmpty());
    }
    
    @Test
    void snapshotAggregatesOrdersAtSamePrice() {
        OrderBook orderBook = new OrderBook();

        orderBook.processOrder(
            new LimitOrder(1, 100, Side.BUY, 10, new BigDecimal("100"))
        );

        orderBook.processOrder(
            new LimitOrder(2, 200, Side.BUY, 20, new BigDecimal("100"))
        );

        OrderBookSnapshot snapshot = orderBook.getSnapshot();

        assertEquals(1, snapshot.bids().size());
        assertEquals(new BigDecimal("100"), snapshot.bids().get(0).price());
        assertEquals(30, snapshot.bids().get(0).quantity());
    }

    @Test
    void snapshotOrdersBidsAndAsksCorrectly() {
        OrderBook orderBook = new OrderBook();
        orderBook.processOrder(
            new LimitOrder(1, 100, Side.BUY, 10, new BigDecimal("99"))
        );
        orderBook.processOrder(
            new LimitOrder(2, 100, Side.BUY, 10, new BigDecimal("101"))
        );
        orderBook.processOrder(
            new LimitOrder(3, 100, Side.BUY, 10, new BigDecimal("100"))
        );
        orderBook.processOrder(
            new LimitOrder(4, 100, Side.SELL, 10, new BigDecimal("103"))
        );
        orderBook.processOrder(
            new LimitOrder(5, 100, Side.SELL, 10, new BigDecimal("104"))
        );
        orderBook.processOrder(
            new LimitOrder(6, 100, Side.SELL, 10, new BigDecimal("102"))
        );
        OrderBookSnapshot snapshot = orderBook.getSnapshot();
        assertEquals(new BigDecimal("101"), snapshot.bids().get(0).price());
        assertEquals(new BigDecimal("100"), snapshot.bids().get(1).price());
        assertEquals(new BigDecimal("99"), snapshot.bids().get(2).price());

        assertEquals(new BigDecimal("102"), snapshot.asks().get(0).price());
        assertEquals(new BigDecimal("103"), snapshot.asks().get(1).price());
        assertEquals(new BigDecimal("104"), snapshot.asks().get(2).price());
    }
}
