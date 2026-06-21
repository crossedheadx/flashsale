package com.example.flashsale.application;

import org.springframework.stereotype.Service;

import com.example.flashsale.infrastructure.ProductRepositoryAdapter;

import jakarta.transaction.Transactional;

@Service
public class DeleteProductUseCase {
    private final ProductRepositoryAdapter productRepository;

    public DeleteProductUseCase(ProductRepositoryAdapter productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void execute(Long id) {
        productRepository.deleteById(id);
    }
}
