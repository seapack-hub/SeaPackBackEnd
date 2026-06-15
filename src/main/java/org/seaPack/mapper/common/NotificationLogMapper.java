package org.seaPack.mapper.common;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.common.NotificationLog;

import java.util.List;

@Mapper
public interface NotificationLogMapper {

    List<NotificationLog> selectLogsByStockId(@Param("stockId") Long stockId);

    List<NotificationLog> selectLogsByUserId(@Param("userId") Long userId);

    int insertLog(NotificationLog log);
}
