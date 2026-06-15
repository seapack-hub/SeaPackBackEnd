package org.seaPack.controller.common;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.common.NotificationLog;
import org.seaPack.service.common.NotificationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知日志控制器
 * <p>提供通知日志的查询与新增接口。</p>
 */
@Slf4j
@RestController
@RequestMapping("/notificationLog")
public class NotificationLogController {

    @Autowired
    private NotificationLogService notificationLogService;

    /**
     * 根据股票 ID 查询通知日志列表
     */
    @GetMapping("/list/{stockId}")
    public ResponseEntity<List<NotificationLog>> list(@PathVariable Long stockId) {
        return ResponseEntity.ok(notificationLogService.getLogsByStockId(stockId));
    }

    /**
     * 根据用户 ID 查询通知日志列表（按时间倒序）
     */
    @GetMapping("/listByUser/{userId}")
    public ResponseEntity<List<NotificationLog>> listByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationLogService.getLogsByUserId(userId));
    }

    /**
     * 新增通知日志
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody NotificationLog log) {
        return ResponseEntity.ok(notificationLogService.insertLog(log));
    }
}
