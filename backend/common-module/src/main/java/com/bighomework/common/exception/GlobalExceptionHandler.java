package com.bighomework.common.exception;

import com.bighomework.common.util.ApiResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j // 1. 引入 Lombok 日志注解
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常 (如：余额不足、库存不足)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException ex) {
        // 业务异常通常是预期的，打印 warn 即可
        log.warn("业务异常: {}", ex.getMessage());
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理参数校验异常 (如：@NotNull, @Size)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("参数校验失败: {}", errorMessage);
        return new ResponseEntity<>(ApiResponse.error(errorMessage), HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理认证/授权异常 (如：密码错误、用户不存在)
     */
    @ExceptionHandler({AuthenticationException.class, UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(Exception ex) {
        log.warn("认证失败: {}", ex.getMessage());
        return new ResponseEntity<>(
            ApiResponse.error(401, "手机号或密码错误"), 
            HttpStatus.UNAUTHORIZED
        );
    }

    /**
     * 处理数据库唯一性约束 (如：手机号重复注册)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "数据提交失败，可能存在重复信息。";
        
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            String causeMessage = ex.getCause().getMessage().toLowerCase();
            if (causeMessage.contains("duplicate entry")) {
                if (causeMessage.contains("phone")) {
                    message = "该手机号已被注册。";
                } else if (causeMessage.contains("id_card")) {
                    message = "该身份证号已被注册。";
                }
            }
        }
        
        log.warn("数据冲突: {}", message);
        return new ResponseEntity<>(
            ApiResponse.error(409, message), 
            HttpStatus.CONFLICT
        );
    }
    
    /**
     * 处理所有未知的系统异常 (兜底策略)
     * 【关键】这里必须打印堆栈信息，否则无法调试 500 错误
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex) {
        // 打印完整的堆栈跟踪信息
        log.error("系统发生未知异常: ", ex);
        return new ResponseEntity<>(ApiResponse.error("服务器内部错误，请联系管理员"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}