package org.seaPack.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;

/**
 * 全局异常处理器
 * <p>
 * 统一捕获并处理各类异常，返回标准化的 Result 响应体。
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 标准错误响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：code={}, msg={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理第三方接口调用异常
     * <p>
     * 当调用东方财富等外部数据源失败时，返回友好提示
     *
     * @param e RestClientException 异常
     * @return 标准错误响应
     */
    @ExceptionHandler(RestClientException.class)
    @ResponseBody
    public Result<Void> handleRestClientException(RestClientException e) {
        log.error("调用第三方数据源失败：{}", e.getMessage());
        return Result.error(502, "股票数据服务暂时不可用，请稍后重试");
    }

    /**
     * 处理请求参数校验异常
     *
     * @param e 参数校验异常
     * @return 标准错误响应（包含校验失败详情）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("请求参数校验失败：{}", errorMsg);
        return Result.error(400, errorMsg);
    }

    /**
     * 兜底异常处理
     * <p>
     * 捕获所有未在以上方法中处理的异常，返回通用错误信息
     *
     * @param e 未处理异常
     * @return 标准错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<Void> handleException(Exception e) {
        log.error("系统内部异常：", e);
        return Result.error(500, "服务繁忙，请稍后重试");
    }
}
