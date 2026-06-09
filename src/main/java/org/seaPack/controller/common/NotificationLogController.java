package org.seaPack.controller.common;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.common.NotificationLog;
import org.seaPack.service.common.NotificationLogService;
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

    @GetMapping("/list/{stockId}")
    public ResponseEntity<List<NotificationLog>> list(@PathVariable Long stockId) {
        return ResponseEntity.ok(notificationLogService.getLogsByStockId(stockId));
    }

    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody NotificationLog log) {
        return ResponseEntity.ok(notificationLogService.insertLog(log));
    }
}