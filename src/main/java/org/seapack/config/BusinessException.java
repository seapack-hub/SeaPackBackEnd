package org.seapack.config;

import lombok.Getter;

public class BusinessException extends RuntimeException{

    @Getter
    private int code; // 错误码（如 "4001"）
    private String message; // 错误描述（如 "用户不存在"）

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
