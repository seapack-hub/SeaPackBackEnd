package org.seaPack.service.common;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.common.AlertLogMapper;
import org.seaPack.model.common.AlertLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 告警日志服务
 * <p>提供告警日志的查询与写入功能。</p>
 */
@Slf4j
@Service
@Transactional
public class AlertLogService {

    @Autowired
    private AlertLogMapper alertLogMapper;

    /**
     * 分页查询告警日志（按 sent_time 降序）
     */
    @Transactional(readOnly = true)
    public PageInfo<AlertLog> getLogsByUserId(int pageNum, int pageSize, Long userId, String stockCode, String startTime, String endTime) {
        PageHelper.startPage(pageNum, pageSize);
        List<AlertLog> list = alertLogMapper.selectLogsByUserId(userId, stockCode, startTime, endTime);
        return new PageInfo<>(list);
    }

    /**
     * 写入告警日志
     */
    public int insertLog(AlertLog log) {
        return alertLogMapper.insertLog(log);
    }
}
