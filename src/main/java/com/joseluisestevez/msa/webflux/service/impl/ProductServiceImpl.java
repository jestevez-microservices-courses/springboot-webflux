package com.joseluisestevez.msa.webflux.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.joseluisestevez.msa.webflux.dao.ProductDao;
import com.joseluisestevez.msa.webflux.models.documents.Product;
import com.joseluisestevez.msa.webflux.service.ProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDao productDao;

    @Override
    public Flux<Product> findAll() {
        return productDao.findAll();
    }

    @Override
    public Mono<Product> findById(String id) {
        return productDao.findById(id);
    }

    @Override
    public Mono<Product> save(Product product) {
        return productDao.save(product);
    }

    @Override
    public Mono<Void> delete(Product product) {
        return productDao.delete(product);
    }

    @Override
    public Flux<Product> findAllWitNameUppercase() {
        return productDao.findAll().map(product -> {
            product.setName(product.getName().toUpperCase());
            return product;
        });
    }

    @Override
    public Flux<Product> findAllWitNameUppercaseRepeat() {
        return findAllWitNameUppercase().repeat(5000);
    }

}
