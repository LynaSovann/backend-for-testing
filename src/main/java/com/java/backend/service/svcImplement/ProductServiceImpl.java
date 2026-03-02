package com.java.backend.service.svcImplement;

import com.java.backend.model.Product;
import com.java.backend.repository.ProductRepository;
import com.java.backend.service.MinioService;
import com.java.backend.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final MinioService minioService;

    public ProductServiceImpl(ProductRepository productRepository,
                              MinioService minioService) {
        this.productRepository = productRepository;
        this.minioService = minioService;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.getAllProducts();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.getProductById(id);
    }

    @Override
    public Product createProduct(String productName,
                                 String description,
                                 MultipartFile file) {

        String imageUrl = "N/A";

        if (file != null && !file.isEmpty()) {
            imageUrl = minioService.uploadProductImage(file);
        }

        return productRepository.createProduct(productName, description, imageUrl);
    }

    @Override
    public Product updateProduct(Long id,
                                 String productName,
                                 String description,
                                 MultipartFile file) {

        Product existingProduct = productRepository.getProductById(id);

        String imageUrl = existingProduct.getImageUrl();

        if (file != null && !file.isEmpty()) {

            if (!Objects.equals(existingProduct.getImageUrl(), "N/A")) {
                minioService.deleteProductImage(existingProduct.getImageUrl());
            }

            imageUrl = minioService.uploadProductImage(file);
        }

        productRepository.updateProduct(id, productName, description, imageUrl);

        return productRepository.getProductById(id);
    }

    @Override
    public void deleteProduct(Long id) {

        Product product = productRepository.getProductById(id);

        if (!Objects.equals(product.getImageUrl(), "N/A")) {
            minioService.deleteProductImage(product.getImageUrl());
        }

        productRepository.deleteProduct(id);
    }
}