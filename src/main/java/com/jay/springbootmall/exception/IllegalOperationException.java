package com.jay.springbootmall.exception;

// 2. 業務邏輯衝突/非法操作的異常 (對應 HTTP 400 或 409)
public class IllegalOperationException extends RuntimeException {
    public IllegalOperationException(String message) {
        super(message);
    }
}
