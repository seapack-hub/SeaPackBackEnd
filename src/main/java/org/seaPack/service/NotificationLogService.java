package org.seaPack.service;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.NotificationLogMapper;
import org.seaPack.model.NotificationLog;
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

    /**
     * 查询指定股票的所有通知记录
     * @param stockId 股票ID
     * @return 通知日志列表
     */
    public List<NotificationLog> getLogsByStockId(Long stockId) {
        return notificationLogMapper.selectLogsByStockId(stockId);
    }

    /**
     * 写入通知日志
     * @param log 日志信息
     * @return 影响行数
     */
    public int insertLog(NotificationLog log) {
        return notificationLogMapper.insertLog(log);
    }
}
