package com.example.flashsale.application;

import org.springframework.stereotype.Service;

import com.example.flashsale.domain.Product;
import com.example.flashsale.infrastructure.ProductRepositoryAdapter;

import jakarta.transaction.Transactional;

@Service
public class UpdateProductUseCase {
    private final ProductRepositoryAdapter productRepository;

    public UpdateProductUseCase(ProductRepositoryAdapter productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product execute(Long id, String name, int stock, double price) {
        Product product = productRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Product NOT FOUND!")
        );

        product.setId(id);
        product.setName(name);
        product.setStock(stock);
        product.setPrice(price);

        return productRepository.save(product);
    }
}
