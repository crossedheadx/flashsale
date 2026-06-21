package com.example.flashsale.presentation;

import com.example.flashsale.application.CreateProductUseCase;
import com.example.flashsale.application.DeleteProductUseCase;
import com.example.flashsale.application.PurchaseUseCase;
import com.example.flashsale.application.UpdateProductUseCase;
import com.example.flashsale.domain.Product;
import com.example.flashsale.infrastructure.RateLimiterService;

import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final PurchaseUseCase purchaseUseCase;
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final RateLimiterService rateLimiterService;

    public ProductController(
            PurchaseUseCase purchaseUseCase,
            CreateProductUseCase createProductUseCase,
            UpdateProductUseCase updateProductUseCase,
            DeleteProductUseCase deleteProductUseCase,
            RateLimiterService rate) {
        this.purchaseUseCase = purchaseUseCase;
        this.createProductUseCase = createProductUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
        this.rateLimiterService = rate;
    }

    // DTO for http request
    public record ProductRequest(String name, int stock, double price) {
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody ProductRequest request) {
        Product product = createProductUseCase.execute(request.name(), request.stock(), request.price());
        return ResponseEntity.ok(product);
    }

    // UPDATE / PUT
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        Product product = updateProductUseCase.execute(id, request.name(), request.stock(), request.price());
        return ResponseEntity.ok(product);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        deleteProductUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<String> purchaseProduct(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @RequestHeader(value = "X-Client-Id", defaultValue = "anonymous") String clientId) {
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
                    "Someone else has purchased this, try again later.");
        }
    }
}
