package com.joseluisestevez.msa.webflux.controllers;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import com.joseluisestevez.msa.webflux.models.documents.Category;
import com.joseluisestevez.msa.webflux.models.documents.Product;
import com.joseluisestevez.msa.webflux.service.ProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SessionAttributes("product")
@Controller
public class ProductController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Value("${config.uploads.path}")
    private String path;

    @ModelAttribute("categories")
    public Flux<Category> categories() {
        return productService.findAllCategory();
    }

    @GetMapping("/uploads/image/{photoName:.+}")
    public Mono<ResponseEntity<Resource>> uploads(@PathVariable String photoName) throws MalformedURLException {
        Path localPath = Paths.get(path).resolve(photoName).toAbsolutePath();
        Resource imagen = new UrlResource(localPath.toUri());

        return Mono.just(
                ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imagen.getFilename() + "\"").body(imagen));
    }

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
        model.addAttribute("button", "Create");
        return Mono.just("form");
    }

    @PostMapping("/form")
    public Mono<String> save(@Valid @ModelAttribute("product") Product product, BindingResult bindingResult, @RequestPart FilePart file,
            SessionStatus status, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Errores in product");
            model.addAttribute("button", "Save");
            return Mono.just("form");
        }

        status.setComplete();

        Mono<Category> category = productService.findCategoryById(product.getCategory().getId());

        return category.flatMap(c -> {
            product.setCategory(c);
            if (product.getCreateAt() == null) {
                product.setCreateAt(new Date());
            }

            if (!file.filename().isEmpty()) {
                product.setPhoto(UUID.randomUUID().toString() + "-" + file.filename().replace(" ", "").replace(":", "").replace("\\", ""));
            }

            return productService.save(product);
        }).doOnNext(p -> {
            LOGGER.info("category=[{}]", p.getCategory());
            LOGGER.info("product=[{}]", p);
        }).flatMap(p -> {
            if (!file.filename().isEmpty()) {
                return file.transferTo(new File(path + p.getPhoto()));
            }
            return Mono.empty();
        }).thenReturn("redirect:/list?success=product+saved+successfully");
    }

    @GetMapping("/delete/{id}")
    public Mono<String> delete(@PathVariable String id) {
        return productService.findById(id).defaultIfEmpty(new Product()).flatMap(p -> {
            if (p.getId() == null) {
                return Mono.error(new InterruptedException("The product does not exist"));
            }
            return Mono.just(p);
        }).flatMap(product -> {
            LOGGER.info("Delete product [{}]", product);
            return productService.delete(product);
        }).then(Mono.just("redirect:/list?success=product+deleted+successfully"))
                .onErrorResume(ex -> Mono.just("redirect:/list?error=The+product+does+not+exist"));

    }

    @GetMapping("/form-v2/{id}")
    public Mono<String> editv2(Model model, @PathVariable String id) {
        // @SessionAttributes It does not work here
        return productService.findById(id).doOnNext(p -> {
            LOGGER.info("product[{}]", p);
            model.addAttribute("product", p);
            model.addAttribute("title", "Product edit");
            model.addAttribute("button", "Edit");
        }).defaultIfEmpty(new Product()).flatMap(p -> {
            if (p.getId() == null) {
                return Mono.error(new InterruptedException("The product does not exist"));
            }
            return Mono.just(p);
        }).then(Mono.just("form")).onErrorResume(ex -> Mono.just("redirect:/list?error=The+product+does+not+exist"));
    }

    @GetMapping("/form/{id}")
    public Mono<String> edit(Model model, @PathVariable String id) {
        Mono<Product> product = productService.findById(id).doOnNext(p -> LOGGER.info("product[{}]", p)).defaultIfEmpty(new Product());

        model.addAttribute("product", product);
        model.addAttribute("title", "Product edit");
        model.addAttribute("button", "Edit");
        return Mono.just("form");
    }

    @GetMapping("/view/{id}")
    public Mono<String> view(Model model, @PathVariable String id) {
        return productService.findById(id).doOnNext(p -> {
            LOGGER.info("product[{}]", p);
            model.addAttribute("product", p);
            model.addAttribute("title", "Product view");
        }).switchIfEmpty(Mono.just(new Product())).flatMap(p -> {
            if (p.getId() == null) {
                return Mono.error(new InterruptedException("The product does not exist"));
            }
            return Mono.just(p);
        }).then(Mono.just("view")).onErrorResume(ex -> Mono.just("redirect:/list?error=The+product+does+not+exist"));

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
