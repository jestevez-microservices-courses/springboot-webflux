package com.joseluisestevez.msa.webflux.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.joseluisestevez.msa.webflux.models.documents.Product;

public interface ProductDao extends ReactiveMongoRepository<Product, String> {

}
