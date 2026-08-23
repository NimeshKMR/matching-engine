package com.nimeshkmr.matching_engine.web.mapper;

import java.util.ArrayList;
import java.util.List;

import com.nimeshkmr.events.EngineEvent;
import com.nimeshkmr.events.OrderAccepted;
import com.nimeshkmr.events.OrderCancelled;
import com.nimeshkmr.events.OrderResult;
import com.nimeshkmr.events.TradeExecuted;
import com.nimeshkmr.matching_engine.web.dto.EventResponse;
import com.nimeshkmr.matching_engine.web.dto.OrderAcceptedResponse;
import com.nimeshkmr.matching_engine.web.dto.OrderCancelledResponse;
import com.nimeshkmr.matching_engine.web.dto.PlaceOrderResponse;
import com.nimeshkmr.matching_engine.web.dto.TradeExecutedResponse;

public class OrderResultMapper {
    
    public PlaceOrderResponse map(OrderResult result){
        List<EventResponse> eventResponses = new ArrayList<>();
        for(EngineEvent event : result.events()){
            if(event instanceof OrderAccepted accepted){
                eventResponses.add(new OrderAcceptedResponse("ORDER_ACCEPTED", accepted.orderID(), accepted.accountID()));
            }
            else if(event instanceof TradeExecuted trade){
                eventResponses.add(new TradeExecutedResponse("TRADE_EXECUTED", trade.buyOrderID(), trade.sellOrderID(), trade.price(), trade.quantity()));
            }
            else if(event instanceof OrderCancelled cancelled){
                eventResponses.add(new OrderCancelledResponse("ORDER_CANCELLED", cancelled.orderID(), cancelled.accountID()));
            }
        }
        return new PlaceOrderResponse(result.orderID(), eventResponses);
    }
}
