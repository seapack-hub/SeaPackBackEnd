package org.seaPack.mapper.common;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.common.AlertLog;

import java.util.List;

@Mapper
public interface AlertLogMapper {

    List<AlertLog> selectLogsByUserId(@Param("userId") Long userId,
                                      @Param("stockCode") String stockCode,
                                      @Param("startTime") String startTime,
                                      @Param("endTime") String endTime);

    int insertLog(AlertLog log);
}
