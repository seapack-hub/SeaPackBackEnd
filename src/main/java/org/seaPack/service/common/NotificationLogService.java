package org.seaPack.service.common;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.common.NotificationLogMapper;
import org.seaPack.model.common.NotificationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知日志服务
 * <p>提供通知日志的查询与新增功能。</p>
 */
@Slf4j
@Service
@Transactional(readOnly = false)
public class NotificationLogService {

    @Autowired
    private NotificationLogMapper notificationLogMapper;

    /**
     * 根据股票 ID 查询通知日志
     */
    public List<NotificationLog> getLogsByStockId(Long stockId) {
        return notificationLogMapper.selectLogsByStockId(stockId);
    }

    /**
     * 根据用户 ID 查询通知日志
     */
    public List<NotificationLog> getLogsByUserId(Long userId) {
        return notificationLogMapper.selectLogsByUserId(userId);
    }

    /**
     * 新增通知日志
     */
    public int insertLog(NotificationLog log) {
        return notificationLogMapper.insertLog(log);
    }
}
