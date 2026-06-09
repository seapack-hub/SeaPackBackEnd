package org.seaPack.service.common;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.common.NotificationLogMapper;
import org.seaPack.model.common.NotificationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = false)
public class NotificationLogService {

    @Autowired
    private NotificationLogMapper notificationLogMapper;

    public List<NotificationLog> getLogsByStockId(Long stockId) {
        return notificationLogMapper.selectLogsByStockId(stockId);
    }

    public int insertLog(NotificationLog log) {
        return notificationLogMapper.insertLog(log);
    }
}