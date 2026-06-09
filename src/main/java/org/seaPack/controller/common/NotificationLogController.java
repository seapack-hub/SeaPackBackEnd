package org.seaPack.controller.common;

import lombok.extern.slf4j.Slf4j; // Lombok 日志
import org.seaPack.model.common.NotificationLog; // 通知日志实体
import org.seaPack.service.common.NotificationLogService; // 通知日志服务
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.http.ResponseEntity; // HTTP 响应实体
import org.springframework.web.bind.annotation.*; // Spring Web MVC 注解

import java.util.List; // List 集合

/**
 * 通知日志控制器
 * 提供通知日志的查询与新增接口。
 */
@Slf4j // Lombok 日志
@RestController // 标识为 RESTful 控制器
@RequestMapping("/notificationLog") // 请求基础路径
public class NotificationLogController {

    @Autowired // 注入通知日志服务
    private NotificationLogService notificationLogService;

    /**
     * 根据股票 ID 查询通知日志列表
     * @param stockId 股票 ID
     */
    @GetMapping("/list/{stockId}")
    public ResponseEntity<List<NotificationLog>> list(@PathVariable Long stockId) {
        return ResponseEntity.ok(notificationLogService.getLogsByStockId(stockId));
    }

    /**
     * 新增通知日志
     * @param log 通知日志实体
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody NotificationLog log) {
        return ResponseEntity.ok(notificationLogService.insertLog(log));
    }
}