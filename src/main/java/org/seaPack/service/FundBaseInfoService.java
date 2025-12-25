package org.seaPack.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.FundBaseInfoMapper;
import org.seaPack.model.FundBaseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FundBaseInfoService {

    @Autowired
    private FundBaseInfoMapper fundBaseInfoMapper;

    public PageInfo<FundBaseInfo> getFundBaseInfoList(int pageNum, int pageSize, FundBaseInfo example) {
        PageHelper.startPage(pageNum, pageSize);
        List<FundBaseInfo> fundBaseInfoList = fundBaseInfoMapper.selectFundsList(example);
        return new PageInfo<>(fundBaseInfoList);
    }
}
