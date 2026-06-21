package com.example.flashsale.infrastructure;

import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.flashsale.domain.Product;

@Repository
public class ProductRepositoryAdapter {
    private final ProductRepository jpaRepository;
    private final StringRedisTemplate redisTemplate;

    public ProductRepositoryAdapter(
        ProductRepository productRepositoryAdapter,
        StringRedisTemplate stringRedisTemplate
    ){
        this.jpaRepository = productRepositoryAdapter;
        this.redisTemplate = stringRedisTemplate;
    }

    public Product save(Product product){
        // persist
        Product savedProduct = jpaRepository.save(product);
        //update redis
        redisTemplate.opsForValue().set("product:" + savedProduct.getId() + ":stock",  Integer.toString(savedProduct.getStock()));
        
        return savedProduct;
    }

    public void deleteById(Long id){
        // hard delete
        jpaRepository.deleteById(id);
        // update redis
        redisTemplate.delete("product:" + id + ":stock");
    }

    public Optional<Product> findById(Long id){
        return jpaRepository.findById(id);
    }
}
