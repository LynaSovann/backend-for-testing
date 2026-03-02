package com.java.backend.controller;

import com.java.backend.model.Product;
import com.java.backend.model.response.APIResponse;
import com.java.backend.model.response.DataWrapper;
import com.java.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // =========================
    // GET ALL PRODUCTS
    // =========================
    @GetMapping
    @Operation(summary = "Get all products")
    public ResponseEntity<APIResponse<?>> getAllProducts() {

        APIResponse<List<Product>> res = APIResponse.<List<Product>>builder()
                .message("Get all products successfully")
                .payload(productService.getAllProducts())
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(res);
    }

    // =========================
    // GET PRODUCT BY ID
    // =========================
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<APIResponse<?>> getProductById(@PathVariable Long id) {

        APIResponse<Product> res = APIResponse.<Product>builder()
                .message("Get product successfully")
                .payload(productService.getProductById(id))
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(res);
    }

    // =========================
    // CREATE PRODUCT
    // =========================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create product")
    public ResponseEntity<APIResponse<DataWrapper>> createProduct(
            @RequestParam("product_name") String productName,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {

        Product product = productService.createProduct(productName, description, file);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("product_name", product.getProductName());
        attributes.put("image_url", product.getImageUrl());
        attributes.put("description", product.getDescription());

        DataWrapper data = DataWrapper.builder()
                .type("product")
                .id(product.getProductId().toString())
                .attributes(attributes)
                .build();

        APIResponse<DataWrapper> res = APIResponse.<DataWrapper>builder()
                .message("Product created successfully")
                .payload(data)
                .status(HttpStatus.CREATED)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // =========================
    // UPDATE PRODUCT
    // =========================
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update product")
    public ResponseEntity<APIResponse<DataWrapper>> updateProduct(
            @PathVariable Long id,
            @RequestParam("product_name") String productName,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {

        Product product = productService.updateProduct(id, productName, description, file);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("product_name", product.getProductName());
        attributes.put("image_url", product.getImageUrl());
        attributes.put("description", product.getDescription());

        DataWrapper data = DataWrapper.builder()
                .type("product")
                .id(product.getProductId().toString())
                .attributes(attributes)
                .build();

        APIResponse<DataWrapper> res = APIResponse.<DataWrapper>builder()
                .message("Product updated successfully")
                .payload(data)
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(res);
    }

    // =========================
    // DELETE PRODUCT
    // =========================
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product")
    public ResponseEntity<APIResponse<DataWrapper>> deleteProduct(@PathVariable Long id) {

        productService.deleteProduct(id);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("deleted", true);
        attributes.put("deletedAt", LocalDateTime.now());

        DataWrapper data = DataWrapper.builder()
                .type("product")
                .id(id.toString())
                .attributes(attributes)
                .build();

        APIResponse<DataWrapper> res = APIResponse.<DataWrapper>builder()
                .message("Product deleted successfully")
                .payload(data)
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(res);
    }
}