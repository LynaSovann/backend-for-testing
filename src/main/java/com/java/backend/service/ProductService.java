package com.java.backend.service;


import com.java.backend.model.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    List<Product> getAllProducts();

    Product getProductById(Long id);

    Product createProduct(String productName, String description, MultipartFile file);

    Product updateProduct(Long id, String productName, String description, MultipartFile file);

    void deleteProduct(Long id);
}