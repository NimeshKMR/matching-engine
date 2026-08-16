package com.nimeshkmr.events;

import com.nimeshkmr.core.Order;

public record OrderAccepted(
        long orderID,
        long accountID
) implements EngineEvent {
}