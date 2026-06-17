package com.example.flashsale.application;

import com.example.flashsale.domain.Product;
import com.example.flashsale.infrastructure.ProductRepository;
import com.example.flashsale.infrastructure.RedisGatekeeper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseUseCase {

    private final ProductRepository productRepository;
    private final RedisGatekeeper redisGatekeeper;

    public PurchaseUseCase(
        ProductRepository productRepository,
        RedisGatekeeper redisGatekeeper
    ) {
        this.productRepository = productRepository;
        this.redisGatekeeper = redisGatekeeper;
    }

    @Transactional
    public void execute(Long productId, Integer quantity) {
        //check on Redis first
        Boolean isReserved = redisGatekeeper.reserveStock(productId, quantity);
        if (!isReserved) {
            throw new IllegalStateException("Sold Out!!");
        }

        // find else exception
        Product product = productRepository
            .findById(productId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Product with ID " +
                        Long.toString(productId) +
                        " not found!"
                )
            );
        // business logic, if exception launched, @Transactional protects procedure and no data will stored
        product.decreaseStock(quantity);
        // db save
        productRepository.save(product);
    }
}
