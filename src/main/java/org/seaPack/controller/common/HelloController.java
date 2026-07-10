package org.seaPack.controller.common;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器
 * 提供基础服务可用性检测接口。
 */
@RestController
public class HelloController {

    /**
     * 健康检查接口
     * @return "Hello World!" 字符串
     */
    @RequestMapping("/hello")
    public String index(){
        return "Hello World!";
    }
}