package com.bighomework.flight.controller;

import com.bighomework.common.util.ApiResponse;
import com.bighomework.flight.utils.MinioUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final MinioUtils minioUtils;

    @PostMapping("/upload")
    public ApiResponse<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bizType", defaultValue = "avatars") String bizType) {

        // 动态决定桶名
        String targetBucket = "avatars"; // 默认用户头像
        if ("airline".equals(bizType)) {
            targetBucket = "airline-logos"; // 航司Logo
        }

        String url = minioUtils.uploadFile(file, targetBucket);
        return ApiResponse.success(url, "上传成功");
    }
}