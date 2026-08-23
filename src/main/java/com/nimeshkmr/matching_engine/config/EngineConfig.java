package com.nimeshkmr.matching_engine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import com.nimeshkmr.core.OrderBook;

@Configuration
public class EngineConfig {
    @Bean
    public OrderBook orderBook(){
        return new OrderBook();
    }
}
