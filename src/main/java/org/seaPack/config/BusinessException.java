package org.seaPack.config;

import lombok.Getter;

public class BusinessException extends RuntimeException{

    @Getter
    private int code; // 错误码（如 "4001"）

    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
