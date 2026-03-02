package com.java.backend.service.svcImplement;

import com.java.backend.service.MinioService;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String maxFileSizeProperty;

    public MinioServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    // =========================
    // PROFILE IMAGE UPLOAD
    // =========================
    @Override
    public String uploadProfileImage(Integer userId, MultipartFile file) {

        validateFile(file);
        ensureBucketExists();

        try {

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";

            String fileName = "profiles/" + userId + "_" + System.currentTimeMillis() + extension;

            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            return minioUrl + "/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            System.out.println("Failed to upload profile image: " + e.getMessage());
            return null;
        }
    }

    // =========================
    // PRODUCT IMAGE UPLOAD
    // =========================
    @Override
    public String uploadProductImage(MultipartFile file) {

        validateFile(file);
        ensureBucketExists();

        try {

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";

            String fileName = "products/" + System.currentTimeMillis() + extension;

            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            return minioUrl + "/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            System.out.println("Failed to upload product image: " + e.getMessage());
            return null;
        }
    }

    // =========================
    // DELETE PROFILE IMAGE
    // =========================
    @Override
    public void deleteProfileImage(String fileName) {
        deleteObject(fileName);
    }

    // =========================
    // DELETE PRODUCT IMAGE
    // =========================
    @Override
    public void deleteProductImage(String fileName) {
        deleteObject(fileName);
    }

    // =========================
    // GENERIC DELETE LOGIC
    // =========================
    private void deleteObject(String fileName) {
        try {

            String objectName = fileName;

            if (fileName.contains(bucketName + "/")) {
                objectName = fileName.substring(
                        fileName.indexOf(bucketName + "/") + bucketName.length() + 1
                );
            }

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

        } catch (Exception e) {
            System.out.println("Failed to delete file: " + e.getMessage());
        }
    }

    // =========================
    // PRESIGNED URL
    // =========================
    @Override
    public String getPresignedUrl(String fileName) {

        try {

            String objectName = fileName;

            if (fileName.contains(bucketName + "/")) {
                objectName = fileName.substring(
                        fileName.indexOf(bucketName + "/") + bucketName.length() + 1
                );
            }

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );

        } catch (Exception e) {
            System.out.println("Failed to generate presigned URL: " + e.getMessage());
            return null;
        }
    }

    // =========================
    // ENSURE BUCKET EXISTS
    // =========================
    @Override
    public void ensureBucketExists() {

        try {

            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {

                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );

                String policy = """
                        {
                            "Version": "2012-10-17",
                            "Statement": [
                                {
                                    "Effect": "Allow",
                                    "Principal": {"AWS": "*"},
                                    "Action": ["s3:GetObject"],
                                    "Resource": ["arn:aws:s3:::%s/*"]
                                }
                            ]
                        }
                        """.formatted(bucketName);

                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policy)
                                .build()
                );

                System.out.println("Bucket created and configured: " + bucketName);
            }

        } catch (Exception e) {
            System.out.println("Failed to ensure bucket exists: " + e.getMessage());
        }
    }

    // =========================
    // FILE VALIDATION
    // =========================
    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            System.out.println("File is required");
            return;
        }

        long maxSize = getMaxFileSize();
        if (file.getSize() > maxSize) {
            System.out.println("File too large: " + file.getSize());
            return;
        }

        String contentType = file.getContentType();
        boolean validType = false;

        String[] allowedTypes = {
                "image/jpeg", "image/jpg", "image/png",
                "image/gif", "image/webp"
        };

        for (String type : allowedTypes) {
            if (type.equals(contentType)) {
                validType = true;
                break;
            }
        }

        if (!validType) {
            System.out.println("Invalid file type: " + file.getOriginalFilename());
        }
    }

    // =========================
    // FILE SIZE PARSER
    // =========================
    private long getMaxFileSize() {

        if (maxFileSizeProperty.endsWith("MB")) {
            return Long.parseLong(maxFileSizeProperty.replace("MB", "")) * 1024 * 1024;
        } else if (maxFileSizeProperty.endsWith("KB")) {
            return Long.parseLong(maxFileSizeProperty.replace("KB", "")) * 1024;
        } else if (maxFileSizeProperty.endsWith("GB")) {
            return Long.parseLong(maxFileSizeProperty.replace("GB", "")) * 1024 * 1024 * 1024;
        }

        return 10 * 1024 * 1024; // default 10MB
    }
}