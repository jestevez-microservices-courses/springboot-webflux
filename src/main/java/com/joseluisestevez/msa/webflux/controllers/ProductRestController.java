package com.joseluisestevez.msa.webflux.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.joseluisestevez.msa.webflux.dao.ProductDao;
import com.joseluisestevez.msa.webflux.models.documents.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductRestController.class);

    @Autowired
    private ProductDao productDao;

    @GetMapping
    public Flux<Product> index() {
        Flux<Product> products = productDao.findAll().map(product -> {
            product.setName(product.getName().toUpperCase());
            return product;
        }).doOnNext(p -> LOGGER.info("product=[{}]", p.getName()));

        return products;
    }

    @GetMapping("/{id}")
    public Mono<Product> show(@PathVariable String id) {
        // Mono<Product> product = productDao.findById(id);

        Flux<Product> products = productDao.findAll();
        Mono<Product> product = products.filter(p -> p.getId().equals(id)).next().doOnNext(p -> LOGGER.info("product=[{}]", p.getName()));
        return product;
    }
}
