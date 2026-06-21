package com.example.flashsale.application;

import org.springframework.stereotype.Service;

import com.example.flashsale.infrastructure.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class DeleteProductUseCase {
    private final ProductRepository productRepository;

    public DeleteProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void execute(Long id) {
        productRepository.deleteById(id);
    }
}
