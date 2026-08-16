package com.nimeshkmr.core;
import java.util.*;
import java.math.BigDecimal;

import com.nimeshkmr.events.*;

public class OrderBook{
    TreeMap<BigDecimal, OrderQueue> asks;
    TreeMap <BigDecimal, OrderQueue> bids;
    HashMap<Long, OrderNode> orders;

    private LimitOrder matchLimitBid(LimitOrder buy, List<EngineEvent> events) {
        while (buy.getQuantity() > 0 && !asks.isEmpty()) {
            BigDecimal bestAsk = getBestAsk();
            if (buy.getPrice().compareTo(bestAsk) < 0) {
                break;
            }
            OrderQueue queue = asks.get(bestAsk);
            LimitOrder sellOrder = queue.getHead().order;
            long tradeQuantity = Math.min(buy.getQuantity(), sellOrder.getQuantity());
            events.add( new TradeExecuted( buy.getOrderID(), sellOrder.getOrderID(), sellOrder.getPrice(), tradeQuantity));
            buy.reduceQuantity(tradeQuantity);
            sellOrder.reduceQuantity(tradeQuantity);
            if (sellOrder.getQuantity() == 0) {
                removeOrder(sellOrder.getOrderID());
            }
        }
        return buy;
    }

    private LimitOrder matchLimitAsk(LimitOrder sell, List<EngineEvent> events) {
        while (sell.getQuantity() > 0 && !bids.isEmpty()) {
            BigDecimal bestBid = getBestBid();
            if (sell.getPrice().compareTo(bestBid) > 0) {
                break;
            }
            OrderQueue queue = bids.get(bestBid);
            LimitOrder buyOrder = queue.getHead().order;
            long tradeQuantity = Math.min(sell.getQuantity(), buyOrder.getQuantity());
            events.add(
                new TradeExecuted(
                    buyOrder.getOrderID(),
                    sell.getOrderID(),
                    buyOrder.getPrice(),
                    tradeQuantity
                )
            );
            sell.reduceQuantity(tradeQuantity);
            buyOrder.reduceQuantity(tradeQuantity);
            if (buyOrder.getQuantity() == 0) {
                removeOrder(buyOrder.getOrderID());
            }
        }
        return sell;
    }

    private void matchMarketBid(MarketOrder buy, List<EngineEvent> events){
        while (buy.getQuantity() > 0 && !asks.isEmpty()) {
            BigDecimal bestAsk = getBestAsk();
            OrderQueue queue = asks.get(bestAsk);
            LimitOrder sellOrder = queue.getHead().order;
            long tradeQuantity = Math.min(buy.getQuantity(), sellOrder.getQuantity());
            events.add(
                new TradeExecuted(
                    buy.getOrderID(),
                    sellOrder.getOrderID(),
                    sellOrder.getPrice(),
                    tradeQuantity
                )
            );
            buy.reduceQuantity(tradeQuantity);
            sellOrder.reduceQuantity(tradeQuantity);
            if (sellOrder.getQuantity() == 0) {
                removeOrder(sellOrder.getOrderID());
            }
        }
    }

    private void matchMarketAsk(MarketOrder sell, List<EngineEvent> events){
        while (sell.getQuantity() > 0 && !bids.isEmpty()) {
            BigDecimal bestBid = getBestBid();
            OrderQueue queue = bids.get(bestBid);
            LimitOrder buyOrder = queue.getHead().order;
            long tradeQuantity = Math.min(sell.getQuantity(), buyOrder.getQuantity());
            events.add(new TradeExecuted(buyOrder.getOrderID(), sell.getOrderID(), buyOrder.getPrice(), tradeQuantity));
            sell.reduceQuantity(tradeQuantity);
            buyOrder.reduceQuantity(tradeQuantity);
            if (buyOrder.getQuantity() == 0) {
                removeOrder(buyOrder.getOrderID());
            }
        }
    }

    public List<Trade> placeOrder(Order order){
        List<Trade> trades = new ArrayList<>();
        OrderResult result = processOrder(order);
        for(EngineEvent event : result.events()){
            if(event instanceof TradeExecuted trade){
                trades.add(new Trade(trade.buyOrderID(), trade.sellOrderID(), trade.price(), trade.quantity()));
            }
        }
        return trades;
    }

    public boolean removeOrder(Long orderID){
        if(!orders.containsKey(orderID)){
            return false;
        }
        OrderNode node = orders.get(orderID);
        Side side = node.order.getSide();
        if(side == Side.BUY){
            OrderQueue queue = bids.get(node.order.getPrice());
            queue.cancel(node);
            if(queue.isEmpty()){
                bids.remove(node.order.getPrice());
            }
        }
        else{
            OrderQueue queue = asks.get(node.order.getPrice());
            queue.cancel(node);
            if(queue.isEmpty()){
                asks.remove(node.order.getPrice());
            }
        }
        orders.remove(orderID);
        return true;
    }

    public OrderResult processOrder(Order order){
        if (orders.containsKey(order.getOrderID())) {
            throw new IllegalArgumentException("Duplicate order ID");
        }
        List<EngineEvent> events = new ArrayList<>();
        events.add(new OrderAccepted(order.getOrderID(), order.getAccountID()));
        if (order instanceof LimitOrder limitOrder) {
            if (limitOrder.getSide() == Side.BUY) {
                limitOrder = matchLimitBid(limitOrder, events);
            } else {
                limitOrder = matchLimitAsk(limitOrder, events);
            }
            if (limitOrder.getQuantity() > 0) {
                addLimitOrderToBook(limitOrder);
            }
        } 
        else if (order instanceof MarketOrder marketOrder) {
            if (marketOrder.getSide() == Side.BUY) {
                matchMarketBid(marketOrder, events);
            } else {
                matchMarketAsk(marketOrder, events);
            }
        }
        return new OrderResult(order.getOrderID(), events);
    }

    private void addLimitOrderToBook(LimitOrder order){
        OrderNode node = new OrderNode(order);
            if(order.getSide() == Side.BUY){
                if(!this.bids.containsKey(order.getPrice())){
                    this.bids.put(order.getPrice(), new OrderQueue());
                }
                OrderQueue oq = this.bids.get(order.getPrice());
                oq.append(node);
            }
            else{
                if(!this.asks.containsKey(order.getPrice())){
                    this.asks.put(order.getPrice(), new OrderQueue());
                }
                OrderQueue oq = this.asks.get(order.getPrice());
                oq.append(node);
            }
            orders.put(order.getOrderID(), node);
    }


    public BigDecimal getBestBid(){
        if(!bids.isEmpty()){
            return bids.firstKey();
        }
        else{
            return null;
        }
    }

    public BigDecimal getBestAsk(){
        if(!asks.isEmpty()){
            return asks.firstKey();
        }
        else{
            return null;
        }
    }

    public OrderBook(){
        this.asks = new TreeMap<>();
        this.bids = new TreeMap<>(Collections.reverseOrder());
        this.orders = new HashMap<>();
    }
}