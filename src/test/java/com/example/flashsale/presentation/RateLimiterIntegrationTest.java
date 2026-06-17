package com.example.flashsale.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.flashsale.domain.Product;
import com.example.flashsale.infrastructure.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class RateLimiterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll(); // ONLY for testing
        productRepository.save(new Product("Flash product", 100));
    }

    @Test
    public void testRateLimiterBlockExcessiveRequests() throws Exception {
        Long productId = productRepository.findAll().get(0).getId();

        String clientId = "test-malicious";

        for (int i = 0; i < 5; i++) {
            mockMvc
                .perform(
                    post("/api/products/" + productId + "/purchase")
                        .param("quantity", "1")
                        .header("X-Client-id", clientId)
                )
                .andExpect(status().isOk());
        }

        mockMvc
            .perform(
                post("/api/products/" + productId + "/purchase")
                    .param("quantity", "1")
                    .header("X-Client-id", clientId)
            )
            .andExpect(status().isTooManyRequests());
    }
}
