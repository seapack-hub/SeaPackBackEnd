package org.seapack.config;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * @param e 异常类型
     * @return
     */
    @ExceptionHandler(BusinessException.class) // 指定捕获的异常类型
    @ResponseBody // 返回JSON格式数据
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }


    /**
     * 处理系统异常（如空指针）
     * @param e 异常类型
     * @return
     */
//    @ExceptionHandler(Exception.class)  // 捕获所有未处理的异常
//    @ResponseBody
//    public Result<Void> handleException(Exception e) {
//        //log.error("系统异常:", e);
//        System.out.print(e);
//        // 返回模糊错误信息
//        return Result.error(500, "服务繁忙，请重试");
//    }

    /**
     * 处理常见异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(400, errorMsg); // 返回校验失败详情
    }
}
