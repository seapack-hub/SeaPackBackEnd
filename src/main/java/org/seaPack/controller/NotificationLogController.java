package org.seaPack.controller;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.NotificationLog;
import org.seaPack.service.NotificationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notificationLog")
public class NotificationLogController {

    @Autowired
    private NotificationLogService notificationLogService;

    /**
     * 查询指定股票的通知日志
     * @param stockId 股票ID
     * @return 通知日志列表
     */
    @GetMapping("/list/{stockId}")
    public ResponseEntity<List<NotificationLog>> list(@PathVariable Long stockId) {
        return ResponseEntity.ok(notificationLogService.getLogsByStockId(stockId));
    }

    /**
     * 写入通知日志
     * @param log 日志信息
     * @return 影响行数
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody NotificationLog log) {
        return ResponseEntity.ok(notificationLogService.insertLog(log));
    }
}
