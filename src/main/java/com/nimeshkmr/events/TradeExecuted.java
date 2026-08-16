package com.nimeshkmr.events;

import java.math.BigDecimal;

public record TradeExecuted(
    long buyOrderID,
    long sellOrderID,
    BigDecimal price,
    long quantity
) implements EngineEvent {
}
