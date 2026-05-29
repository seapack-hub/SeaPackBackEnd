package org.seaPack.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.HoldingMapper;
import org.seaPack.model.Holding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HoldingService {

    @Autowired
    private HoldingMapper holdingMapper;

    /**
     * 分页查询持仓列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param holding 查询条件
     * @return 分页结果
     */
    public PageInfo<Holding> getHoldingList(int pageNum, int pageSize, Holding holding) {
        PageHelper.startPage(pageNum, pageSize);
        List<Holding> selectHoldingList = holdingMapper.selectHoldingList(holding);
        return new PageInfo<>(selectHoldingList);
    }
}
