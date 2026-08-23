package com.nimeshkmr.matching_engine.web.dto;

public record OrderAcceptedResponse(String type, long orderID, long accountID) implements EventResponse {
}  
