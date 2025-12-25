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

    public PageInfo<Holding> getHoldingList(int pageNum, int pageSize, Holding holding) {
        PageHelper.startPage(pageNum, pageSize);
        List<Holding> selectHoldingList = holdingMapper.selectHoldingList(holding);
        return new PageInfo<>(selectHoldingList);
    }

}
