package com.joseluisestevez.msa.webflux.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.joseluisestevez.msa.webflux.models.documents.Category;

public interface CategoryDao extends ReactiveMongoRepository<Category, String> {

}
