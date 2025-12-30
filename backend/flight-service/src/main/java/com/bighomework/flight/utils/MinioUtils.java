package com.bighomework.flight.utils;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
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

    @Value("${minio.endpoint}")
    private String endpoint;

    /**
     * 上传文件到指定的桶，并返回访问URL
     * @param file 文件对象
     * @param bucketName 目标桶名
     */
    public String uploadFile(MultipartFile file, String bucketName) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString().replace("-", "") +
                (originalFilename != null && originalFilename.contains(".")
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : ".jpg");

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

            log.info("文件成功上传至桶 [{}]: {}", bucketName, fileName);

            // 返回永久有效的公开访问URL (前提是已执行 mc anonymous set download)
            return String.format("%s/%s/%s", endpoint, bucketName, fileName);

        } catch (Exception e) {
            log.error("MinIO上传失败, 桶: {}, 错误: {}", bucketName, e.getMessage());
            throw new RuntimeException("文件上传失败");
        }
    }

    /**
     * 从指定桶中删除文件
     * @param fileUrl 文件的完整URL
     * @param bucketName 目标桶名
     */
    public void deleteFile(String fileUrl, String bucketName) {
        if (fileUrl == null || fileUrl.isEmpty()) return;

        try {
            // 提取文件名
            String objectName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("已从桶 [{}] 中删除文件: {}", bucketName, objectName);
        } catch (Exception e) {
            log.error("MinIO文件删除失败: {}", fileUrl, e);
        }
    }

    /**
     * 获取带有效期的访问链接（仅用于私有桶）
     */
    public String getPresignedObjectUrl(String objectName, String bucketName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取预签名URL失败", e);
            return "";
        }
    }
}