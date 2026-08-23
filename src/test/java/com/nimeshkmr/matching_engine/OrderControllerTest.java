package com.nimeshkmr.matching_engine;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nimeshkmr.core.Side;
import com.nimeshkmr.events.OrderAccepted;
import com.nimeshkmr.events.OrderResult;
import com.nimeshkmr.matching_engine.web.controller.OrderController;
import com.nimeshkmr.matching_engine.web.service.OrderBookService;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderBookService orderBookService;

    @Test
    void rejectNegativeQuantity() throws Exception{
         mockMvc.perform(post("/orders")
        .contentType("application/json")
        .content("""
            {
                "orderID": 1,
                "accountID": 100,
                "side": "BUY",
                "type": "LIMIT",
                "quantity": -10,
                "price": 100
            }
            """))
        .andExpect(status().isBadRequest());
    }


    @Test
    void placeOrder() throws Exception {
        OrderResult result = new OrderResult(
            1,
            List.of(new OrderAccepted(1, 100))
        );

        when(orderBookService.placeOrder(org.mockito.ArgumentMatchers.any()))
            .thenReturn(result);

        mockMvc.perform(post("/orders")
            .contentType("application/json")
            .content("""
                {
                    "orderID": 1,
                    "accountID": 100,
                    "side": "BUY",
                    "type": "LIMIT",
                    "quantity": 10,
                    "price": 100
                }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderID").value(1))
            .andExpect(jsonPath("$.eventResponses[0].type").value("ORDER_ACCEPTED"))
            .andExpect(jsonPath("$.eventResponses[0].orderID").value(1))
            .andExpect(jsonPath("$.eventResponses[0].accountID").value(100));
    }
}