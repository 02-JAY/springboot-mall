package com.jay.springbootmall.config;

import com.jay.springbootmall.exception.IllegalOperationException;
import com.jay.springbootmall.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 捕捉 404 找不到商品
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildResponseBody(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 捕捉 400 惡意修改或狀態錯誤
    @ExceptionHandler(IllegalOperationException.class)
    public ResponseEntity<Object> handleIllegalOperation(IllegalOperationException ex) {
        return buildResponseBody(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 💡 額外加碼：捕捉 JPA 樂觀鎖失敗 (當兩個使用者同時修改同一筆資料時)
    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Object> handleOptimisticLockingFailure(Exception ex) {
        return buildResponseBody(HttpStatus.CONFLICT, "資料已被其他使用者更新，請重新整理頁面後再試一次。");
    }

    // 統一的 JSON 回傳格式工具
    private ResponseEntity<Object> buildResponseBody(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}
