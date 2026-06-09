package org.seaPack.controller.finance;

import com.github.pagehelper.PageInfo; // MyBatis 分页信息
import lombok.extern.slf4j.Slf4j; // Lombok 日志
import org.seaPack.model.finance.Holding; // 持仓实体
import org.seaPack.service.finance.HoldingService; // 持仓服务
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.http.ResponseEntity; // HTTP 响应实体
import org.springframework.web.bind.annotation.*; // Spring Web MVC 注解

/**
 * 持仓控制器
 * 提供持仓数据的分页查询接口。
 */
@Slf4j // Lombok 日志
@RestController // 标识为 RESTful 控制器
@RequestMapping("/holding") // 请求基础路径
public class HoldingController {

    @Autowired // 注入持仓服务
    private HoldingService holdingService;

    /**
     * 分页查询持仓列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param holding 查询条件
     */
    @GetMapping("/page")
    public ResponseEntity<PageInfo<Holding>> selectHoldingList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            Holding holding){
        PageInfo<Holding> holdingPageInfo = holdingService.getHoldingList(pageNum, pageSize, holding);
        return ResponseEntity.ok(holdingPageInfo);
    }
}