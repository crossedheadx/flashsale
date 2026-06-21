package com.example.flashsale.application;

import org.springframework.stereotype.Service;

import com.example.flashsale.domain.Product;
import com.example.flashsale.infrastructure.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class CreateProductUseCase {
    private final ProductRepository productRepository;

    public CreateProductUseCase(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    @Transactional
    public Product execute(String name, int stock, double price){
        Product product = new Product();
        
        product.setName(name);
        product.setStock(stock);
        product.setPrice(price);
        // redis will be notified alonside db
        return productRepository.save(product);
    }
}
