package org.seaPack.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.NotificationLog;

import java.util.List;

@Mapper
public interface NotificationLogMapper {

    /**
     * 查询指定股票的所有通知记录
     * @param stockId 股票ID
     * @return 通知日志列表
     */
    List<NotificationLog> selectLogsByStockId(@Param("stockId") Long stockId);

    /**
     * 写入通知日志
     * @param log 日志信息
     * @return 影响行数
     */
    int insertLog(NotificationLog log);
}
