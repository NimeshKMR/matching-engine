package com.nimeshkmr.matching_engine;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nimeshkmr.core.OrderBook;

@SpringBootTest
public class EngineConfigTest {

    @Autowired
    private OrderBook orderBook;
    
    @Test 
    void orderBookBeanExists(){
        assertNotNull(orderBook);
    }
}
