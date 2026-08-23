package com.nimeshkmr.core;

import java.util.List;

public record OrderBookSnapshot(List<OrderBookLevel> bids, List<OrderBookLevel> asks) {
    
}
