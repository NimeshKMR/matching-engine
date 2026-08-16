package com.nimeshkmr.events;

public record OrderCancelled(
        long orderID,
        long accountID
) implements EngineEvent {
}