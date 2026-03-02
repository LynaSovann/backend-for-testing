package com.java.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface MinioService {

    String uploadProfileImage(Integer userId, MultipartFile file);

    void deleteProfileImage(String fileName);

    String uploadProductImage(MultipartFile file);

    void deleteProductImage(String fileName);

    String getPresignedUrl(String fileName);

    void ensureBucketExists();
}