package com.nimeshkmr.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class OrderBookDeterminism {
@Test
void sameOrderSequenceProducesSameState() {

    OrderBook book1 = new OrderBook();
    OrderBook book2 = new OrderBook();

    LimitOrder buy1 =
            new LimitOrder(
                    1L,
                    1L,
                    Side.BUY,
                    50,
                    new BigDecimal("100")
            );

    LimitOrder buy2 =
            new LimitOrder(
                    2L,
                    1L,
                    Side.BUY,
                    75,
                    new BigDecimal("101")
            );

    LimitOrder sell =
            new LimitOrder(
                    3L,
                    1L,
                    Side.SELL,
                    60,
                    new BigDecimal("100")
            );

    book1.placeOrder(buy1);
    book1.placeOrder(buy2);
    book1.placeOrder(sell);

    book2.placeOrder(
            new LimitOrder(
                    1L, 1L, Side.BUY,
                    50, new BigDecimal("100")
            )
    );

    book2.placeOrder(
            new LimitOrder(
                    2L, 1L, Side.BUY,
                    75, new BigDecimal("101")
            )
    );

    book2.placeOrder(
            new LimitOrder(
                    3L, 1L, Side.SELL,
                    60, new BigDecimal("100")
            )
    );

    assertEquals(book1.getBestBid(), book2.getBestBid());
    assertEquals(book1.getBestAsk(), book2.getBestAsk());
}
}
