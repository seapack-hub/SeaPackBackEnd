package org.seaPack.controller.common;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration; // Spring Boot 自动配置
import org.springframework.web.bind.annotation.RequestMapping; // 请求映射
import org.springframework.web.bind.annotation.RestController; // REST 控制器
import org.springframework.boot.SpringApplication; // Spring Boot 启动类

/**
 * 健康检查控制器
 * 提供基础服务可用性检测接口，也可作为独立应用入口启动。
 */
@RestController // 标识为 RESTful 控制器
@EnableAutoConfiguration // 启用 Spring Boot 自动配置
public class HelloController {

    /**
     * 健康检查接口
     * @return "Hello World!" 字符串
     */
    @RequestMapping("/hello")
    public String index(){
        return "Hello World!";
    };

    /**
     * 独立启动入口（可直接运行此类）
     */
    public static void main(String[] args) {
        SpringApplication.run(HelloController.class, args);
    }
}