package com.nimeshkmr.events;
import java.util.List;


public record OrderResult(
    long orderID,
    List<EngineEvent> events
){
}
