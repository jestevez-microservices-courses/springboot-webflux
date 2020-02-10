package com.joseluisestevez.msa.webflux;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.joseluisestevez.msa.webflux.dao.ProductDao;
import com.joseluisestevez.msa.webflux.models.documents.Product;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringbootWebfluxApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringbootWebfluxApplication.class);

    @Autowired
    private ProductDao dao;
    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    public static void main(String[] args) {
        SpringApplication.run(SpringbootWebfluxApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        reactiveMongoTemplate.dropCollection("products").subscribe();

        Flux.just(new Product("TV Panasonic Pantalla LCD", 456.89), new Product("Sony Camara HD Digital", 177.89), new Product("Apple iPod", 46.89),
                new Product("Sony Notebook", 846.89), new Product("Hewlett Packard Multifuncional", 200.89), new Product("Bianchi Bicicleta", 70.89),
                new Product("HP Notebook Omen 17", 2500.89), new Product("Mica Cómoda 5 Cajones", 150.89),
                new Product("TV Sony Bravia OLED 4K Ultra HD", 2255.89)).flatMap(p -> dao.save(p))
                .subscribe(p -> LOGGER.info("Insert product id= [{}] name=[{}]", p.getId(), p.getName()));
    }

}
