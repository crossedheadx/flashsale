package com.example.flashsale.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.flashsale.domain.Product;
import com.example.flashsale.infrastructure.ProductRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class PurchaseUseCaseTest {

    @Autowired
    private PurchaseUseCase purchaseUseCase;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach //called automatically before test
    void setUp(){
        productRepository.deleteAll();

    }

    @Test
    public void testOptimisticLockingPreventsOverselling()
        throws InterruptedException {
            try{

                Product product = new Product("Phone 1", 1);
                product = productRepository.save(product);
                final Long productId = product.getId();
        
                redisTemplate.opsForValue().set("product:" + productId + ":stock", "1");
        
                ExecutorService executor = Executors.newFixedThreadPool(2);
                CountDownLatch latch = new CountDownLatch(2); // wait for both end
        
                AtomicInteger successCnt = new AtomicInteger(0);
                AtomicInteger errorCnt = new AtomicInteger(0);
        
                Runnable purchaseTask = () -> {
                    try {
                        purchaseUseCase.execute(productId, 1);
                        successCnt.incrementAndGet(); //thread safe increment
                    } catch (Exception e) {
                        errorCnt.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                };
        
                executor.submit(purchaseTask);
                executor.submit(purchaseTask);
                latch.await(); // await threads end
        
                assertEquals(
                    1,
                    successCnt.get(),
                    "only one purchase must be completed"
                );
                assertEquals(
                    1,
                    errorCnt.get(),
                    "one must be blocked by optimistic locking"
                );
                Product finalProduct = productRepository
                    .findById(productId)
                    .orElseThrow();
                assertEquals(0, finalProduct.getStock());
        
                redisTemplate.delete("rate_limit:test-hacker");
            } catch (Exception e) {
                e.printStackTrace();
                throw new InterruptedException();
            }

    }
}
