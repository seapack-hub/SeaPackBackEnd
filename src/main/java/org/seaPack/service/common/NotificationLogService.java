package org.seaPack.service.common;

import lombok.extern.slf4j.Slf4j; // Lombok 日志
import org.seaPack.mapper.common.NotificationLogMapper; // 通知日志 Mapper
import org.seaPack.model.common.NotificationLog; // 通知日志实体
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.stereotype.Service; // Spring 服务注解
import org.springframework.transaction.annotation.Transactional; // 事务管理

import java.util.List; // List 集合

/**
 * 通知日志服务
 * 提供通知日志的查询与新增功能。
 */
@Slf4j // Lombok 日志
@Service // 标识为 Spring 服务 Bean
@Transactional(readOnly = false) // 启用写入事务
public class NotificationLogService {

    @Autowired // 注入通知日志 Mapper
    private NotificationLogMapper notificationLogMapper;

    /**
     * 根据股票 ID 查询通知日志
     * @param stockId 股票 ID
     * @return 通知日志列表
     */
    public List<NotificationLog> getLogsByStockId(Long stockId) {
        return notificationLogMapper.selectLogsByStockId(stockId); // 调用 Mapper 查询
    }

    /**
     * 新增通知日志
     * @param log 通知日志实体
     * @return 影响行数
     */
    public int insertLog(NotificationLog log) {
        return notificationLogMapper.insertLog(log); // 调用 Mapper 插入
    }
}