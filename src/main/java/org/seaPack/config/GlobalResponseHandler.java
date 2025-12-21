package org.seaPack.config;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import com.alibaba.fastjson.JSON;

@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    /**
     * 若返回值类型已是 Result 类（如手动返回 Result.success()），则跳过处理（return false），
     * 避免重复包装, 若其他类型（如 String、User 等）返回 true，进入 beforeBodyWrite() 处理
     * @param returnType 控制器方法的返回值类型（含泛型信息）
     * @param converterType 当前选定的消息转换器（如 MappingJackson2HttpMessageConverter）
     * @return 返回true或false ,true继续向下执行beforeBodyWrite方法，false就此结束
     */
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return !returnType.getParameterType().isAssignableFrom(Result.class);
    }

    /**
     * 统一封装响应体
     * @param body  控制器方法的原始返回值（未封装）
     * @param returnType 控制器方法的返回值类型（含泛型信息）
     * @param mediaType 当前响应的媒体类型（如 application/json）
     * @param converterType 当前选定的消息转换器
     * @param request HTTP 请求对象，可用于修改头信息
     * @param response HTTP 响应对象，可用于修改状态码
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType mediaType, Class converterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // String类型需特殊处理（避免被解析为文本）
        if (body instanceof String) {
            return JSON.toJSONString(Result.success(body));
        }
        // 其他类型自动封装为Result
        return Result.success(body);
    }
}
