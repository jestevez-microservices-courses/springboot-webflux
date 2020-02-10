package com.joseluisestevez.msa.webflux.controllers;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import com.joseluisestevez.msa.webflux.models.documents.Product;
import com.joseluisestevez.msa.webflux.service.ProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class ProductController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @GetMapping({ "/list", "/" })
    public String list(Model model) {
        Flux<Product> products = productService.findAllWitNameUppercase();

        products.subscribe(product -> LOGGER.info("product: [{}]", product.getName()));

        model.addAttribute("products", products);
        model.addAttribute("title", "Product list");
        return "list";
    }

    @GetMapping("/form")
    public Mono<String> create(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("title", "Product create");
        return Mono.just("form");
    }

    @PostMapping("/form")
    public Mono<String> save(Product product) {
        return productService.save(product).doOnNext(p -> LOGGER.info("product=[{}]", p)).thenReturn("redirect:/list");
    }

    @GetMapping("/list-data-driver")
    public String listDataDriver(Model model) {
        Flux<Product> products = productService.findAllWitNameUppercase().delayElements(Duration.ofSeconds(1));

        products.subscribe(product -> LOGGER.info("product: [{}]", product.getName()));

        model.addAttribute("products", new ReactiveDataDriverContextVariable(products, 1));

        model.addAttribute("title", "Product list");
        return "list";
    }

    @GetMapping("/list-full")
    public String listFull(Model model) {
        Flux<Product> products = productService.findAllWitNameUppercaseRepeat();

        model.addAttribute("products", products);
        model.addAttribute("title", "Product list");
        return "list";
    }

    @GetMapping("/list-chunked")
    public String listChunked(Model model) {
        Flux<Product> products = productService.findAllWitNameUppercaseRepeat();

        model.addAttribute("products", products);
        model.addAttribute("title", "Product list");
        return "list-chunked";
    }
}
