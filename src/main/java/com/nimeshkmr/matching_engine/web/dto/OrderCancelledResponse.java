package com.nimeshkmr.matching_engine.web.dto;

public record OrderCancelledResponse(String type, long orderID, long accountID)  implements EventResponse{
}

