package com.bighomework.planeTicketWeb.exception;

import com.bighomework.planeTicketWeb.util.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException; // 【新增】导入
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException ex) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return new ResponseEntity<>(ApiResponse.error(errorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({AuthenticationException.class, UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(Exception ex) {
        // ...
        return new ResponseEntity<>(
            ApiResponse.error(401, "手机号或密码错误"), 
            HttpStatus.UNAUTHORIZED
        );
    }

    /**
     * 【核心新增】
     * 专门处理数据库层面的数据完整性约束违规异常。
     * 这通常发生在尝试插入一个已存在的唯一键（如手机号、身份证号）时。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        
        String message = "数据提交失败，可能存在重复信息。";
        // 尝试从异常信息中解析出更具体的错误原因
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            String causeMessage = ex.getCause().getMessage().toLowerCase();
            if (causeMessage.contains("duplicate entry") && causeMessage.contains("phone")) {
                message = "该手机号已被注册。";
            } else if (causeMessage.contains("duplicate entry") && causeMessage.contains("id_card")) {
                message = "该身份证号已被注册。";
            }
        }
        
        // 向前端返回一个 409 Conflict 状态码，表示请求与服务器当前状态冲突
        return new ResponseEntity<>(
            ApiResponse.error(409, message), 
            HttpStatus.CONFLICT
        );
    }
    
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex) {
// 打印堆栈信息，方便调试
    return new ResponseEntity<>(ApiResponse.error("服务器内部错误，请联系管理员"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}