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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = false)
public class FundBaseInfoService {

    @Autowired
    private FundBaseInfoMapper fundBaseInfoMapper;

    public PageInfo<FundBaseInfo> getFundBaseInfoList(int pageNum, int pageSize, FundBaseInfo example) {
        PageHelper.startPage(pageNum, pageSize);
        List<FundBaseInfo> fundBaseInfoList = fundBaseInfoMapper.selectFundsList(example);
        for (FundBaseInfo f : fundBaseInfoList) {
            f.setDistanceTime(calcDistance(f.getInceptDate()));
        }
        return new PageInfo<>(fundBaseInfoList);
    }

    public int insertFundBaseInfo(FundBaseInfo fundBaseInfo) {
        // 1.插入前校验
        int counts = fundBaseInfoMapper.selectFundBaseInfoByCode(fundBaseInfo.getFundCode());
        if (counts > 0) {
            throw new RuntimeException("基金代码 " + fundBaseInfo.getFundCode() + " 已存在！");
        }

        // 2.执行插入
        return fundBaseInfoMapper.insertFundBaseInfo(fundBaseInfo);
    }

    public int updateFundBaseInfo(FundBaseInfo fundBaseInfo) {
        int counts = fundBaseInfoMapper.selectFundBaseInfoByCode(fundBaseInfo.getFundCode());
        if (counts == 0) {
            throw new RuntimeException("基金代码 " + fundBaseInfo.getFundCode() + " 不存在！");
        }
        return fundBaseInfoMapper.updateFundBaseInfo(fundBaseInfo);
    }

    public int deleteFundBaseInfo(String fundCode) {
        int counts = fundBaseInfoMapper.selectFundBaseInfoByCode(fundCode);
        if (counts == 0) {
            throw new RuntimeException("基金代码 " + fundCode + " 不存在！");
        }
        return fundBaseInfoMapper.deleteFundBaseInfoByCode(fundCode);
    }

    public FundBaseInfo getFundBaseInfoByCode(String fundCode) {
        return fundBaseInfoMapper.selectFundDetailByCode(fundCode);
    }

    private String calcDistance(java.util.Date inceptDate) {
        if (inceptDate == null) return null;
        LocalDate start = Instant.ofEpochMilli(inceptDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate now = LocalDate.now();
        if (start.isAfter(now)) return "0天";
        long years = ChronoUnit.YEARS.between(start, now);
        LocalDate afterYears = start.plusYears(years);
        long days = ChronoUnit.DAYS.between(afterYears, now);
        if(years == 0l){
            return  days + "天";
        }
        return years + "年" + days + "天";
    }
}
