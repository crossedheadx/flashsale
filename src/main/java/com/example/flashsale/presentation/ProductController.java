package com.example.flashsale.presentation;

import com.example.flashsale.application.PurchaseUseCase;
import com.example.flashsale.infrastructure.RateLimiterService;

import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final PurchaseUseCase purchaseUseCase;
    private final RateLimiterService rateLimiterService;

    public ProductController(
        PurchaseUseCase purchaseUseCase,
        RateLimiterService rate
    ) {
        this.purchaseUseCase = purchaseUseCase;
        this.rateLimiterService = rate;
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<String> purchaseProduct(
        @PathVariable Long id,
        @RequestParam Integer quantity,
        @RequestHeader(
            value = "X-Client-Id",
            defaultValue = "anonymous"
        ) String clientId
    ) {
        if (!rateLimiterService.isAllowed(clientId)) {
            return ResponseEntity.status(429).body("Too Many Requests !");
        }

        try {
            purchaseUseCase.execute(id, quantity);
            return ResponseEntity.ok("Purchase completed! Thank You!");
        } catch (IllegalStateException exception) {
            // intercept domain excepts like "insufficient stock..."
            return ResponseEntity.badRequest().body(exception.getMessage());
        } catch (ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.status(409).body(
                "Someone else has purchased this, try again later."
            );
        }
    }
}
