package com.joseluisestevez.msa.webflux.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.joseluisestevez.msa.webflux.dao.ProductDao;
import com.joseluisestevez.msa.webflux.models.documents.Product;

import reactor.core.publisher.Flux;

@Controller
public class ProductController {

    @Autowired
    private ProductDao productDao;

    @GetMapping({ "/list", "/" })
    public String list(Model model) {
        Flux<Product> products = productDao.findAll();
        model.addAttribute("products", products);
        model.addAttribute("title", "Product list");
        return "list";
    }
}
