package com.bighomework.common.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }

    public static <T> ApiResponse<T> success(T data, String msg) {
        return new ApiResponse<>(200, msg, data);
    }

    public static <T> ApiResponse<T> error(String msg) {
        return new ApiResponse<>(500, msg, null);
    }

    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }
}