package com.bighomework.flight.utils;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtils {

    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * 上传文件并返回访问URL
     */
    public String uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        // 生成唯一文件名，防止覆盖
        String fileName = UUID.randomUUID().toString().replace("-", "") +
                originalFilename.substring(originalFilename.lastIndexOf("."));
        try {
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 这里为了简单，假设Bucket是public的，直接拼接URL
            // 格式通常是: http://ip:9000/bucketName/fileName
            // 实际上生产环境可能需要获取预签名URL或者配置Nginx
            // 这里我们简单返回文件名，前端配合 endpoint 拼接，或者直接返回预签名URL
            return getPresignedObjectUrl(fileName);

        } catch (Exception e) {
            log.error("MinIO上传失败", e);
            throw new RuntimeException("图片上传失败");
        }
    }

    // 获取一个有效期的访问链接
    public String getPresignedObjectUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS) // 7天有效
                            .build()
            );
        } catch (Exception e) {
            log.error("获取图片URL失败", e);
            return "";
        }
    }
}