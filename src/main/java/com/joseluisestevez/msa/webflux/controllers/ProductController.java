package com.joseluisestevez.msa.webflux.controllers;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import com.joseluisestevez.msa.webflux.dao.ProductDao;
import com.joseluisestevez.msa.webflux.models.documents.Product;

import reactor.core.publisher.Flux;

@Controller
public class ProductController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductDao productDao;

    @GetMapping({ "/list", "/" })
    public String list(Model model) {
        Flux<Product> products = productDao.findAll().map(product -> {
            product.setName(product.getName().toUpperCase());
            return product;
        });

        products.subscribe(product -> LOGGER.info("product: [{}]", product.getName()));

        model.addAttribute("products", products);
        model.addAttribute("title", "Product list");
        return "list";
    }

    @GetMapping("/list-data-driver")
    public String listDataDriver(Model model) {
        Flux<Product> products = productDao.findAll().map(product -> {
            product.setName(product.getName().toUpperCase());
            return product;
        }).delayElements(Duration.ofSeconds(1));

        products.subscribe(product -> LOGGER.info("product: [{}]", product.getName()));

        model.addAttribute("products", new ReactiveDataDriverContextVariable(products, 1));

        model.addAttribute("title", "Product list");
        return "list";
    }

    @GetMapping("/list-full")
    public String listFull(Model model) {
        Flux<Product> products = productDao.findAll().map(product -> {
            product.setName(product.getName().toUpperCase());
            return product;
        }).repeat(5000);

        model.addAttribute("products", products);
        model.addAttribute("title", "Product list");
        return "list";
    }
}
