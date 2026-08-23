package com.nimeshkmr.core;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderBookTest {

    private LimitOrder createLimitOrder(
            long id,
            Side side,
            String price,
            long quantity
    ) {
        return new LimitOrder(
                id,
                1L,
                side,
                quantity,
                new BigDecimal(price)
        );
    }

    private MarketOrder createMarketOrder(
            long id,
            Side side,
            long quantity
    ) {
        return new MarketOrder(
                id,
                1L,
                side,
                quantity
        );
    }

    // ==================== ORDER STORAGE ====================

    @Test
    void addBuyOrder() {
        OrderBook book = new OrderBook();

        LimitOrder order = createLimitOrder(1, Side.BUY, "100", 50);

        List<Trade> trades = book.placeOrder(order);

        assertTrue(trades.isEmpty());

        assertFalse(book.bids.isEmpty());
        assertTrue(book.asks.isEmpty());

        assertNotNull(book.bids.get(new BigDecimal("100")));

        assertSame(
                order,
                book.bids.get(new BigDecimal("100")).getHead().order
        );

        assertSame(
                book.bids.get(new BigDecimal("100")).getHead(),
                book.orders.get(1L)
        );
    }

    @Test
    void addSellOrder() {
        OrderBook book = new OrderBook();

        LimitOrder order = createLimitOrder(1, Side.SELL, "105", 50);

        List<Trade> trades = book.placeOrder(order);

        assertTrue(trades.isEmpty());

        assertTrue(book.bids.isEmpty());
        assertFalse(book.asks.isEmpty());

        assertNotNull(book.asks.get(new BigDecimal("105")));

        assertSame(
                order,
                book.asks.get(new BigDecimal("105")).getHead().order
        );

        assertSame(
                book.asks.get(new BigDecimal("105")).getHead(),
                book.orders.get(1L)
        );
    }

    @Test
    void duplicateOrderIdIsRejected() {
        OrderBook book = new OrderBook();

        LimitOrder first =
                createLimitOrder(1, Side.BUY, "100", 50);

        LimitOrder duplicate =
                createLimitOrder(1, Side.BUY, "105", 100);

        book.placeOrder(first);

        assertThrows(
                DuplicateOrderIDException.class,
                () -> book.placeOrder(duplicate)
        );

        assertSame(
                first,
                book.orders.get(1L).order
        );

        assertTrue(
                book.bids.containsKey(new BigDecimal("100"))
        );

        assertFalse(
                book.bids.containsKey(new BigDecimal("105"))
        );
    }

    @Test
    void multipleOrdersAtSamePriceMaintainFifo() {
        OrderBook book = new OrderBook();

        LimitOrder a =
                createLimitOrder(1, Side.BUY, "100", 50);

        LimitOrder b =
                createLimitOrder(2, Side.BUY, "100", 75);

        LimitOrder c =
                createLimitOrder(3, Side.BUY, "100", 100);

        book.placeOrder(a);
        book.placeOrder(b);
        book.placeOrder(c);

        OrderQueue queue =
                book.bids.get(new BigDecimal("100"));

        assertSame(a, queue.getHead().order);
        assertSame(c, queue.getTail().order);

        assertSame(
                b,
                queue.getHead().next.order
        );

        assertSame(
                a,
                queue.getHead().next.prev.order
        );
    }

    // ==================== BEST PRICES ====================

    @Test
    void differentBidPricesAreOrderedHighestFirst() {
        OrderBook book = new OrderBook();

        book.placeOrder(
                createLimitOrder(1, Side.BUY, "100", 50)
        );

        book.placeOrder(
                createLimitOrder(2, Side.BUY, "105", 50)
        );

        book.placeOrder(
                createLimitOrder(3, Side.BUY, "102", 50)
        );

        assertEquals(
                new BigDecimal("105"),
                book.getBestBid()
        );
    }

    @Test
    void differentAskPricesAreOrderedLowestFirst() {
        OrderBook book = new OrderBook();

        book.placeOrder(
                createLimitOrder(1, Side.SELL, "110", 50)
        );

        book.placeOrder(
                createLimitOrder(2, Side.SELL, "105", 50)
        );

        book.placeOrder(
                createLimitOrder(3, Side.SELL, "108", 50)
        );

        assertEquals(
                new BigDecimal("105"),
                book.getBestAsk()
        );
    }

    @Test
    void emptyBookHasNoBestBidOrAsk() {
        OrderBook book = new OrderBook();

        assertNull(book.getBestBid());
        assertNull(book.getBestAsk());
    }

    // ==================== REMOVE ORDER ====================

    @Test
    void removeNonexistentOrder() {
        OrderBook book = new OrderBook();

        assertFalse(book.removeOrder(999L));
    }

    @Test
    void removeOnlyOrderAtPrice() {
        OrderBook book = new OrderBook();

        LimitOrder order =
                createLimitOrder(1, Side.BUY, "100", 50);

        book.placeOrder(order);

        assertTrue(book.removeOrder(1L));

        assertTrue(book.bids.isEmpty());
        assertFalse(book.orders.containsKey(1L));
    }

    @Test
    void removeHeadWhileOrdersRemainAtPrice() {
        OrderBook book = new OrderBook();

        LimitOrder a =
                createLimitOrder(1, Side.BUY, "100", 50);

        LimitOrder b =
                createLimitOrder(2, Side.BUY, "100", 75);

        book.placeOrder(a);
        book.placeOrder(b);

        assertTrue(book.removeOrder(1L));

        OrderQueue queue =
                book.bids.get(new BigDecimal("100"));

        assertNotNull(queue);

        assertSame(b, queue.getHead().order);
        assertSame(b, queue.getTail().order);

        assertFalse(book.orders.containsKey(1L));
        assertTrue(book.orders.containsKey(2L));
    }

    @Test
    void removeMiddleOrder() {
        OrderBook book = new OrderBook();

        LimitOrder a =
                createLimitOrder(1, Side.BUY, "100", 50);

        LimitOrder b =
                createLimitOrder(2, Side.BUY, "100", 75);

        LimitOrder c =
                createLimitOrder(3, Side.BUY, "100", 100);

        book.placeOrder(a);
        book.placeOrder(b);
        book.placeOrder(c);

        assertTrue(book.removeOrder(2L));

        OrderQueue queue =
                book.bids.get(new BigDecimal("100"));

        assertSame(a, queue.getHead().order);
        assertSame(c, queue.getTail().order);

        assertSame(
                c,
                queue.getHead().next.order
        );

        assertSame(
                a,
                queue.getTail().prev.order
        );

        assertFalse(book.orders.containsKey(2L));
    }

    @Test
    void removeTailOrder() {
        OrderBook book = new OrderBook();

        LimitOrder a =
                createLimitOrder(1, Side.BUY, "100", 50);

        LimitOrder b =
                createLimitOrder(2, Side.BUY, "100", 75);

        LimitOrder c =
                createLimitOrder(3, Side.BUY, "100", 100);

        book.placeOrder(a);
        book.placeOrder(b);
        book.placeOrder(c);

        assertTrue(book.removeOrder(3L));

        OrderQueue queue =
                book.bids.get(new BigDecimal("100"));

        assertSame(a, queue.getHead().order);
        assertSame(b, queue.getTail().order);

        assertNull(queue.getTail().next);
        assertFalse(book.orders.containsKey(3L));
    }

    @Test
    void removeSellOrder() {
        OrderBook book = new OrderBook();

        LimitOrder order =
                createLimitOrder(1, Side.SELL, "105", 50);

        book.placeOrder(order);

        assertTrue(book.removeOrder(1L));

        assertTrue(book.asks.isEmpty());
        assertFalse(book.orders.containsKey(1L));
    }

    @Test
    void removeLastOrderOnOneSide() {
        OrderBook book = new OrderBook();

        LimitOrder buy =
                createLimitOrder(1, Side.BUY, "100", 50);

        LimitOrder sell =
                createLimitOrder(2, Side.SELL, "105", 50);

        book.placeOrder(buy);
        book.placeOrder(sell);

        assertTrue(book.removeOrder(1L));

        assertNull(book.getBestBid());
        assertEquals(
                new BigDecimal("105"),
                book.getBestAsk()
        );
    }

    // ==================== LIMIT MATCHING ====================

    @Test
    void noMatchAddsOrderToBook() {
        OrderBook book = new OrderBook();

        LimitOrder buy =
                createLimitOrder(1, Side.BUY, "100", 50);

        List<Trade> trades =
                book.placeOrder(buy);

        assertTrue(trades.isEmpty());

        assertEquals(
                new BigDecimal("100"),
                book.getBestBid()
        );

        assertNull(book.getBestAsk());

        assertTrue(book.orders.containsKey(1L));

        assertEquals(
                50,
                book.orders.get(1L).order.getQuantity()
        );
    }

    @Test
    void fullFill() {
        OrderBook book = new OrderBook();

        LimitOrder sell =
                createLimitOrder(1, Side.SELL, "101", 50);

        LimitOrder buy =
                createLimitOrder(2, Side.BUY, "105", 50);

        book.placeOrder(sell);

        List<Trade> trades =
                book.placeOrder(buy);

        assertEquals(1, trades.size());

        Trade trade = trades.get(0);

        assertEquals(2L, trade.buyOrderID);
        assertEquals(1L, trade.sellOrderID);
        assertEquals(new BigDecimal("101"), trade.price);
        assertEquals(50, trade.quantity);

        assertFalse(book.orders.containsKey(2L));
        assertTrue(book.asks.isEmpty());
        assertTrue(book.bids.isEmpty());
    }

    @Test
    void partialFill() {
        OrderBook book = new OrderBook();

        LimitOrder sell =
                createLimitOrder(1, Side.SELL, "101", 50);

        LimitOrder buy =
                createLimitOrder(2, Side.BUY, "105", 80);

        book.placeOrder(sell);

        List<Trade> trades =
                book.placeOrder(buy);

        assertEquals(1, trades.size());

        Trade trade = trades.get(0);

        assertEquals(new BigDecimal("101"), trade.price);
        assertEquals(50, trade.quantity);

        assertTrue(book.orders.containsKey(2L));

        assertEquals(
                30,
                book.orders.get(2L).order.getQuantity()
        );

        assertEquals(
                new BigDecimal("105"),
                book.getBestBid()
        );

        assertNull(book.getBestAsk());
    }

    @Test
    void matchesMultiplePriceLevels() {
        OrderBook book = new OrderBook();

        book.placeOrder(
                createLimitOrder(1, Side.SELL, "101", 50)
        );

        book.placeOrder(
                createLimitOrder(2, Side.SELL, "102", 70)
        );

        book.placeOrder(
                createLimitOrder(3, Side.SELL, "104", 100)
        );

        LimitOrder buy =
                createLimitOrder(4, Side.BUY, "103", 150);

        List<Trade> trades =
                book.placeOrder(buy);

        assertEquals(2, trades.size());

        assertEquals(
                new BigDecimal("101"),
                trades.get(0).price
        );

        assertEquals(50, trades.get(0).quantity);

        assertEquals(
                new BigDecimal("102"),
                trades.get(1).price
        );

        assertEquals(70, trades.get(1).quantity);

        assertEquals(
                30,
                book.orders.get(4L).order.getQuantity()
        );

        assertEquals(
                new BigDecimal("103"),
                book.getBestBid()
        );

        assertEquals(
                new BigDecimal("104"),
                book.getBestAsk()
        );
    }

    @Test
    void sellMatchesMultipleBidLevels() {
        OrderBook book = new OrderBook();

        book.placeOrder(
                createLimitOrder(1, Side.BUY, "105", 60)
        );

        book.placeOrder(
                createLimitOrder(2, Side.BUY, "104", 80)
        );

        LimitOrder sell =
                createLimitOrder(3, Side.SELL, "103", 100);

        List<Trade> trades =
                book.placeOrder(sell);

        assertEquals(2, trades.size());

        assertEquals(
                new BigDecimal("105"),
                trades.get(0).price
        );

        assertEquals(60, trades.get(0).quantity);

        assertEquals(
                new BigDecimal("104"),
                trades.get(1).price
        );

        assertEquals(40, trades.get(1).quantity);

        assertFalse(book.orders.containsKey(3L));


        assertEquals(
            40,
            book.orders.get(2L).order.getQuantity()
        );

        assertEquals(
                new BigDecimal("104"),
                book.getBestBid()
        );

        assertNull(book.getBestAsk());
    }

    @Test
    void matchingUsesFifoAtSamePrice() {
        OrderBook book = new OrderBook();

        LimitOrder a =
                createLimitOrder(1, Side.SELL, "101", 50);

        LimitOrder b =
                createLimitOrder(2, Side.SELL, "101", 50);

        book.placeOrder(a);
        book.placeOrder(b);

        LimitOrder buy =
                createLimitOrder(3, Side.BUY, "105", 60);

        List<Trade> trades =
                book.placeOrder(buy);

        assertEquals(2, trades.size());

        assertEquals(1L, trades.get(0).sellOrderID);
        assertEquals(50, trades.get(0).quantity);

        assertEquals(2L, trades.get(1).sellOrderID);
        assertEquals(10, trades.get(1).quantity);

        assertFalse(book.orders.containsKey(3L));
        assertFalse(book.orders.containsKey(1L));

        assertTrue(book.orders.containsKey(2L));

        assertEquals(
                40,
                book.orders.get(2L).order.getQuantity()
        );

        assertEquals(
                new BigDecimal("101"),
                book.getBestAsk()
        );

        assertNull(book.getBestBid());
    }

    @Test
    void buyEqualsBestAsk() {
        OrderBook book = new OrderBook();

        LimitOrder sell =
                createLimitOrder(1, Side.SELL, "100", 50);

        book.placeOrder(sell);

        LimitOrder buy =
                createLimitOrder(3, Side.BUY, "100", 50);

        List<Trade> trades =
                book.placeOrder(buy);

        assertEquals(1, trades.size());

        Trade trade = trades.get(0);

        assertEquals(new BigDecimal("100"), trade.price);
        assertEquals(50, trade.quantity);
        assertEquals(3L, trade.buyOrderID);
        assertEquals(1L, trade.sellOrderID);

        assertTrue(book.bids.isEmpty());
        assertTrue(book.asks.isEmpty());
    }

    @Test
    void sellEqualsBestBid() {
        OrderBook book = new OrderBook();

        LimitOrder buy =
                createLimitOrder(1, Side.BUY, "100", 50);

        book.placeOrder(buy);

        LimitOrder sell =
                createLimitOrder(2, Side.SELL, "100", 50);

        List<Trade> trades =
                book.placeOrder(sell);

        assertEquals(1, trades.size());

        Trade trade = trades.get(0);

        assertEquals(new BigDecimal("100"), trade.price);
        assertEquals(50, trade.quantity);
        assertEquals(1L, trade.buyOrderID);
        assertEquals(2L, trade.sellOrderID);

        assertTrue(book.bids.isEmpty());
        assertTrue(book.asks.isEmpty());
    }

    @Test
    void sellConsumesAll() {
        OrderBook book = new OrderBook();

        book.placeOrder(
                createLimitOrder(1, Side.BUY, "100", 50)
        );

        book.placeOrder(
                createLimitOrder(2, Side.BUY, "101", 75)
        );

        book.placeOrder(
                createLimitOrder(3, Side.BUY, "103", 100)
        );

        LimitOrder sell =
                createLimitOrder(4, Side.SELL, "95", 225);

        List<Trade> trades =
                book.placeOrder(sell);

        assertEquals(3, trades.size());

        Trade trade1 = trades.get(0);
        Trade trade2 = trades.get(1);
        Trade trade3 = trades.get(2);

        assertEquals(100, trade1.quantity);
        assertEquals(
                new BigDecimal("103"),
                trade1.price
        );

        assertEquals(75, trade2.quantity);
        assertEquals(
                new BigDecimal("101"),
                trade2.price
        );

        assertEquals(50, trade3.quantity);
        assertEquals(
                new BigDecimal("100"),
                trade3.price
        );

        assertTrue(book.bids.isEmpty());
        assertTrue(book.asks.isEmpty());

        assertFalse(book.orders.containsKey(4L));
    }

    @Test
    void noMatchWhenPricesDoNotCross() {
        OrderBook book = new OrderBook();

        LimitOrder buy =
                createLimitOrder(1, Side.BUY, "100", 50);

        book.placeOrder(buy);

        LimitOrder sell =
                createLimitOrder(2, Side.SELL, "103", 50);

        List<Trade> trades =
                book.placeOrder(sell);

        assertTrue(trades.isEmpty());

        assertEquals(
                new BigDecimal("100"),
                book.getBestBid()
        );

        assertEquals(
                new BigDecimal("103"),
                book.getBestAsk()
        );

        assertTrue(book.orders.containsKey(1L));
        assertTrue(book.orders.containsKey(2L));
    }

    // ==================== MARKET ORDERS ====================

    @Test
    void marketBuyConsumesBestAsk() {
        OrderBook book = new OrderBook();

        LimitOrder sell =
                createLimitOrder(1, Side.SELL, "100", 50);

        book.placeOrder(sell);

        MarketOrder buy =
                createMarketOrder(2, Side.BUY, 50);

        List<Trade> trades =
                book.placeOrder(buy);

        assertEquals(1, trades.size());

        Trade trade = trades.get(0);

        assertEquals(2L, trade.buyOrderID);
        assertEquals(1L, trade.sellOrderID);
        assertEquals(new BigDecimal("100"), trade.price);
        assertEquals(50, trade.quantity);
        assertTrue(book.asks.isEmpty());
        assertTrue(book.bids.isEmpty());
        assertFalse(book.orders.containsKey(2L));
        assertFalse(book.orders.containsKey(1L));
    }

    @Test
    void marketSellConsumesBestBid() {
        OrderBook book = new OrderBook();

        LimitOrder buy =
                createLimitOrder(1, Side.BUY, "100", 50);

        book.placeOrder(buy);

        MarketOrder sell =
                createMarketOrder(2, Side.SELL, 50);

        List<Trade> trades =
                book.placeOrder(sell);

        assertEquals(1, trades.size());

        Trade trade = trades.get(0);

        assertEquals(1L, trade.buyOrderID);
        assertEquals(2L, trade.sellOrderID);
        assertEquals(new BigDecimal("100"), trade.price);
        assertEquals(50, trade.quantity);

        assertTrue(book.bids.isEmpty());
        assertTrue(book.asks.isEmpty());

        assertFalse(book.orders.containsKey(1L));
        assertFalse(book.orders.containsKey(2L));
    }

    @Test
    void marketBuyConsumesMultipleAskLevels() {
        OrderBook book = new OrderBook();

        book.placeOrder(
                createLimitOrder(1, Side.SELL, "100", 50)
        );

        book.placeOrder(
                createLimitOrder(2, Side.SELL, "101", 75)
        );

        book.placeOrder(
                createLimitOrder(3, Side.SELL, "103", 100)
        );

        MarketOrder buy =
                createMarketOrder(4, Side.BUY, 125);

        List<Trade> trades =
                book.placeOrder(buy);

        assertEquals(2, trades.size());

        assertEquals(50, trades.get(0).quantity);
        assertEquals(
                new BigDecimal("100"),
                trades.get(0).price
        );

        assertEquals(75, trades.get(1).quantity);
        assertEquals(
                new BigDecimal("101"),
                trades.get(1).price
        );

        assertTrue(book.orders.containsKey(3L));
        assertEquals(
                100,
                book.orders.get(3L).order.getQuantity()
        );

        assertNull(book.getBestBid());
        assertEquals(
                new BigDecimal("103"),
                book.getBestAsk()
        );

        // Market order is never added to the book.
        assertFalse(book.orders.containsKey(4L));
    }

    @Test
    void marketSellConsumesMultipleBidLevels() {
        OrderBook book = new OrderBook();

        book.placeOrder(
                createLimitOrder(1, Side.BUY, "103", 100)
        );

        book.placeOrder(
                createLimitOrder(2, Side.BUY, "101", 75)
        );

        book.placeOrder(
                createLimitOrder(3, Side.BUY, "100", 50)
        );

        MarketOrder sell =
                createMarketOrder(4, Side.SELL, 175);

        List<Trade> trades =
                book.placeOrder(sell);

        assertEquals(2, trades.size());

        assertEquals(100, trades.get(0).quantity);
        assertEquals(
                new BigDecimal("103"),
                trades.get(0).price
        );

        assertEquals(75, trades.get(1).quantity);
        assertEquals(
                new BigDecimal("101"),
                trades.get(1).price
        );

        assertTrue(book.orders.containsKey(3L));
        assertEquals(
                50,
                book.orders.get(3L).order.getQuantity()
        );

        assertEquals(
                new BigDecimal("100"),
                book.getBestBid()
        );

        assertNull(book.getBestAsk());

        assertFalse(book.orders.containsKey(4L));
    }

    @Test
    void marketBuyLargerThanAvailableLiquidityCancelsRemainder() {
        OrderBook book = new OrderBook();

        book.placeOrder(
                createLimitOrder(1, Side.SELL, "100", 50)
        );

        MarketOrder buy =
                createMarketOrder(2, Side.BUY, 100);

        List<Trade> trades =
                book.placeOrder(buy);

        assertEquals(1, trades.size());

        assertEquals(50, trades.get(0).quantity);
        assertEquals(
                new BigDecimal("100"),
                trades.get(0).price
        );

        assertEquals(50, buy.getQuantity());

        assertTrue(book.asks.isEmpty());
        assertTrue(book.bids.isEmpty());

        // Remaining market quantity is discarded.
        assertFalse(book.orders.containsKey(2L));
    }

    @Test
    void marketSellLargerThanAvailableLiquidityCancelsRemainder() {
        OrderBook book = new OrderBook();

        book.placeOrder(
                createLimitOrder(1, Side.BUY, "100", 50)
        );

        MarketOrder sell =
                createMarketOrder(2, Side.SELL, 100);

        List<Trade> trades =
                book.placeOrder(sell);

        assertEquals(1, trades.size());

        assertEquals(50, trades.get(0).quantity);
        assertEquals(
                new BigDecimal("100"),
                trades.get(0).price
        );

        assertEquals(50, sell.getQuantity());

        assertTrue(book.bids.isEmpty());
        assertTrue(book.asks.isEmpty());

        // Remaining market quantity is discarded.
        assertFalse(book.orders.containsKey(2L));
    }

    @Test
    void marketBuyOnEmptyBookProducesNoTrades() {
        OrderBook book = new OrderBook();

        MarketOrder buy =
                createMarketOrder(1, Side.BUY, 100);

        List<Trade> trades =
                book.placeOrder(buy);

        assertTrue(trades.isEmpty());

        assertTrue(book.bids.isEmpty());
        assertTrue(book.asks.isEmpty());

        assertFalse(book.orders.containsKey(1L));
        assertEquals(100, buy.getQuantity());
    }

    @Test
    void marketSellOnEmptyBookProducesNoTrades() {
        OrderBook book = new OrderBook();

        MarketOrder sell =
                createMarketOrder(1, Side.SELL, 100);

        List<Trade> trades =
                book.placeOrder(sell);

        assertTrue(trades.isEmpty());

        assertTrue(book.bids.isEmpty());
        assertTrue(book.asks.isEmpty());

        assertFalse(book.orders.containsKey(1L));
        assertEquals(100, sell.getQuantity());
    }

    @Test
    void marketOrderNeverRestsOnBook() {
        OrderBook book = new OrderBook();

        MarketOrder buy =
                createMarketOrder(1, Side.BUY, 100);

        book.placeOrder(buy);

        assertTrue(book.bids.isEmpty());
        assertTrue(book.asks.isEmpty());
        assertFalse(book.orders.containsKey(1L));
    }

    @Test
    void marketBuyMaintainsPricePriority() {
        OrderBook book = new OrderBook();

        book.placeOrder(
                createLimitOrder(1, Side.SELL, "105", 50)
        );

        book.placeOrder(
                createLimitOrder(2, Side.SELL, "101", 50)
        );

        book.placeOrder(
                createLimitOrder(3, Side.SELL, "103", 50)
        );

        MarketOrder buy =
                createMarketOrder(4, Side.BUY, 150);

        List<Trade> trades =
                book.placeOrder(buy);

        assertEquals(3, trades.size());

        assertEquals(
                new BigDecimal("101"),
                trades.get(0).price
        );

        assertEquals(
                new BigDecimal("103"),
                trades.get(1).price
        );

        assertEquals(
                new BigDecimal("105"),
                trades.get(2).price
        );
    }

    // ==================== VALIDATION ====================

    @Test
    void zeroQuantityIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createLimitOrder(
                        1,
                        Side.BUY,
                        "100",
                        0
                )
        );
    }

    @Test
    void negativeQuantityIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createLimitOrder(
                        1,
                        Side.BUY,
                        "100",
                        -50
                )
        );
    }

    @Test
    void zeroPriceIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createLimitOrder(
                        1,
                        Side.BUY,
                        "0",
                        50
                )
        );
    }

    @Test
    void negativePriceIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createLimitOrder(
                        1,
                        Side.BUY,
                        "-100",
                        50
                )
        );
    }

    @Test
    void nullSideIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createLimitOrder(
                        1,
                        null,
                        "100",
                        50
                )
        );
    }

    @Test
    void marketOrderCanBeCreatedWithoutPrice() {
        MarketOrder order =
                createMarketOrder(1, Side.BUY, 100);

        assertEquals(1L, order.getOrderID());
        assertEquals(Side.BUY, order.getSide());
        assertEquals(100, order.getQuantity());
    }
}