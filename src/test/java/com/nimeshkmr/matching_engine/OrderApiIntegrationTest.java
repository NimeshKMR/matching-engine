package com.nimeshkmr.matching_engine;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.annotation.DirtiesContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class OrderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void duplicateOrderIdReturnsConflict() throws Exception{
        String order = """
                {
                    "orderID": 1,
                    "accountID": 100,
                    "side": "BUY",
                    "type": "LIMIT",
                    "quantity": 10,
                    "price": 100
                        }
                """;
        mockMvc.perform(post("/orders")
        .contentType("application/json")
        .content(order))
        .andExpect(status().isOk());

        mockMvc.perform(post("/orders")
            .contentType("application/json")
            .content(order))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("DUPLICATE_ORDER_ID"))
            .andExpect(jsonPath("$.message").value("Duplicate order ID"));
    }

    @Test
    void cancelOrderReturnsCancelledEvent() throws Exception {
        String order = """
            {
                "orderID": 10,
                "accountID": 100,
                "side": "BUY",
                "type": "LIMIT",
                "quantity": 10,
                "price": 100
            }
            """;

        mockMvc.perform(post("/orders")
            .contentType("application/json")
            .content(order))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/orders/10?accountID=100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderID").value(10))
            .andExpect(jsonPath("$.eventResponses[0].type").value("ORDER_CANCELLED"))
            .andExpect(jsonPath("$.eventResponses[0].orderID").value(10))
            .andExpect(jsonPath("$.eventResponses[0].accountID").value(100));
    }

    @Test
    void cancelNonexistentOrderReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/orders/999?accountID=100"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("Order not found"));
    }

    @Test
    void cancelOrderWithWrongAccountReturnsForbidden() throws Exception {
        String order = """
            {
                "orderID": 11,
                "accountID": 100,
                "side": "BUY",
                "type": "LIMIT",
                "quantity": 10,
                "price": 100
            }
            """;
        mockMvc.perform(post("/orders")
            .contentType("application/json")
            .content(order))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/orders/11?accountID=200"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.error").value("UNAUTHORIZED_ORDER_CANCELLATION"))
            .andExpect(jsonPath("$.message").value("Order does not belong to account"));
    }

    @Test
    void getOrderBookReturnsCorrectSnapshot() throws Exception {
        String buy1 = """
            {
                "orderID": 20,
                "accountID": 100,
                "side": "BUY",
                "type": "LIMIT",
                "quantity": 10,
                "price": 99
            }
            """;

        String buy2 = """
            {
                "orderID": 21,
                "accountID": 100,
                "side": "BUY",
                "type": "LIMIT",
                "quantity": 20,
                "price": 101
            }
            """;

        String sell1 = """
            {
                "orderID": 22,
                "accountID": 200,
                "side": "SELL",
                "type": "LIMIT",
                "quantity": 15,
                "price": 103
            }
            """;

        String sell2 = """
            {
                "orderID": 23,
                "accountID": 200,
                "side": "SELL",
                "type": "LIMIT",
                "quantity": 25,
                "price": 102
            }
            """;

        mockMvc.perform(post("/orders")
            .contentType("application/json")
            .content(buy1))
            .andExpect(status().isOk());

        mockMvc.perform(post("/orders")
            .contentType("application/json")
            .content(buy2))
            .andExpect(status().isOk());

        mockMvc.perform(post("/orders")
            .contentType("application/json")
            .content(sell1))
            .andExpect(status().isOk());

        mockMvc.perform(post("/orders")
            .contentType("application/json")
            .content(sell2))
            .andExpect(status().isOk());

        mockMvc.perform(get("/orderbook"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bids[0].price").value(101))
            .andExpect(jsonPath("$.bids[0].quantity").value(20))
            .andExpect(jsonPath("$.bids[1].price").value(99))
            .andExpect(jsonPath("$.bids[1].quantity").value(10))
            .andExpect(jsonPath("$.asks[0].price").value(102))
            .andExpect(jsonPath("$.asks[0].quantity").value(25))
            .andExpect(jsonPath("$.asks[1].price").value(103))
            .andExpect(jsonPath("$.asks[1].quantity").value(15));
    }
}