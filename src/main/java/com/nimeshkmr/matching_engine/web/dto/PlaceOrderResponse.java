package com.nimeshkmr.matching_engine.web.dto;

import java.util.List;

public record PlaceOrderResponse(long orderID, List<EventResponse> eventResponses) {
}
