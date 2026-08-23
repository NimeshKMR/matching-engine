package com.nimeshkmr.matching_engine.web.dto;

import java.math.BigDecimal;

public record TradeExecutedResponse(String type, long buyOrderID, long sellOrderID, BigDecimal price, long quantity)
implements EventResponse {
}
