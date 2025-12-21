package org.seaPack.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code; // 状态码，如200成功、500错误
    private String msg;  // 业务数据，泛型支持任意类型
    private T data;  // 提示信息，如"操作成功"

    //成功响应（无数据）
    public static <T> Result<T> success() {
        return new Result<>(200, "成功", null);
    }

    // 成功响应（带数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "成功", data);
    }

    // 失败响应（自定义消息）
    public static <T> Result<T> error(String msg){
        return new Result<>(500, msg, null);
    }

    // 失败响应 （自定义状态码 + 消息）
    public static <T> Result<T> error(int code, String msg){
        return new Result<>(code, msg, null);
    }
}
