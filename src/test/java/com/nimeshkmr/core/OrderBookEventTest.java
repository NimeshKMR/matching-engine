package com.nimeshkmr.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.nimeshkmr.events.EngineEvent;
import com.nimeshkmr.events.OrderAccepted;
import com.nimeshkmr.events.OrderResult;
import com.nimeshkmr.events.TradeExecuted;

public class OrderBookEventTest {
    @Test
    void acceptedOrderProducesOrderAcceptedEvent() {
        OrderBook book = new OrderBook();

        LimitOrder order = new LimitOrder(
                1L,
                10L,
                Side.BUY,
                50,
                new BigDecimal("100")
        );

        OrderResult result = book.processOrder(order);

        assertEquals(1L, result.orderID());
        assertEquals(1, result.events().size());

        EngineEvent event = result.events().get(0);

        assertInstanceOf(OrderAccepted.class, event);

        OrderAccepted accepted = (OrderAccepted) event;

        assertEquals(1L, accepted.orderID());
        assertEquals(10L, accepted.accountID());
    }

    @Test
    void matchingProducesTradeExecutedEvent() {
        OrderBook book = new OrderBook();

        LimitOrder sell = new LimitOrder(
                1L,
                10L,
                Side.SELL,
                50,
                new BigDecimal("100")
        );

        book.processOrder(sell);

        LimitOrder buy = new LimitOrder(
                2L,
                20L,
                Side.BUY,
                50,
                new BigDecimal("105")
        );

        OrderResult result = book.processOrder(buy);

        assertEquals(2, result.events().size());

        assertInstanceOf(OrderAccepted.class, result.events().get(0));
        assertInstanceOf(TradeExecuted.class, result.events().get(1));

        TradeExecuted trade =
                (TradeExecuted) result.events().get(1);

        assertEquals(2L, trade.buyOrderID());
        assertEquals(1L, trade.sellOrderID());
        assertEquals(new BigDecimal("100"), trade.price());
        assertEquals(50, trade.quantity());
    }

    @Test
    void partialFillProducesCorrectTradeEvent() {
        OrderBook book = new OrderBook();

        LimitOrder sell = new LimitOrder(
                1L,
                10L,
                Side.SELL,
                50,
                new BigDecimal("100")
        );

        book.processOrder(sell);

        LimitOrder buy = new LimitOrder(
                2L,
                20L,
                Side.BUY,
                80,
                new BigDecimal("105")
        );

        OrderResult result = book.processOrder(buy);

        TradeExecuted trade =
                (TradeExecuted) result.events().get(1);

        assertEquals(50, trade.quantity());

        assertTrue(book.orders.containsKey(2L));
        assertEquals(
                30,
                book.orders.get(2L).order.getQuantity()
        );
    }

    @Test
    void marketOrderProducesTradeExecutedEvent() {
        OrderBook book = new OrderBook();

        LimitOrder sell = new LimitOrder(
                1L,
                10L,
                Side.SELL,
                50,
                new BigDecimal("100")
        );

        book.processOrder(sell);

        MarketOrder buy = new MarketOrder(
                2L,
                20L,
                Side.BUY,
                50
        );

        OrderResult result = book.processOrder(buy);

        assertEquals(2, result.events().size());

        assertInstanceOf(OrderAccepted.class, result.events().get(0));
        assertInstanceOf(TradeExecuted.class, result.events().get(1));

        TradeExecuted trade =
                (TradeExecuted) result.events().get(1);

        assertEquals(2L, trade.buyOrderID());
        assertEquals(1L, trade.sellOrderID());
        assertEquals(new BigDecimal("100"), trade.price());
        assertEquals(50, trade.quantity());

        assertTrue(book.asks.isEmpty());
    }
}
