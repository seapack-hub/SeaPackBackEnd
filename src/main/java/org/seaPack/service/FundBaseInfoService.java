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

    /**
     * 分页查询基金信息列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param example 查询条件（支持keywords模糊搜索）
     * @return 分页结果
     */
    public PageInfo<FundBaseInfo> getFundBaseInfoList(int pageNum, int pageSize, FundBaseInfo example) {
        PageHelper.startPage(pageNum, pageSize);
        List<FundBaseInfo> fundBaseInfoList = fundBaseInfoMapper.selectFundsList(example);
        for (FundBaseInfo f : fundBaseInfoList) {
            f.setDistanceTime(calcDistance(f.getInceptDate()));
        }
        return new PageInfo<>(fundBaseInfoList);
    }

    /**
     * 新增基金（含唯一性校验）
     * @param fundBaseInfo 基金信息
     * @return 影响行数
     */
    public int insertFundBaseInfo(FundBaseInfo fundBaseInfo) {
        int counts = fundBaseInfoMapper.selectFundBaseInfoByCode(fundBaseInfo.getFundCode());
        if (counts > 0) {
            throw new RuntimeException("基金代码 " + fundBaseInfo.getFundCode() + " 已存在！");
        }
        return fundBaseInfoMapper.insertFundBaseInfo(fundBaseInfo);
    }

    /**
     * 更新基金信息
     * @param fundBaseInfo 待更新数据（必须含fundCode）
     * @return 影响行数
     */
    public int updateFundBaseInfo(FundBaseInfo fundBaseInfo) {
        int counts = fundBaseInfoMapper.selectFundBaseInfoByCode(fundBaseInfo.getFundCode());
        if (counts == 0) {
            throw new RuntimeException("基金代码 " + fundBaseInfo.getFundCode() + " 不存在！");
        }
        return fundBaseInfoMapper.updateFundBaseInfo(fundBaseInfo);
    }

    /**
     * 根据基金代码删除基金记录
     * @param fundCode 基金代码
     * @return 影响行数
     */
    public int deleteFundBaseInfo(String fundCode) {
        int counts = fundBaseInfoMapper.selectFundBaseInfoByCode(fundCode);
        if (counts == 0) {
            throw new RuntimeException("基金代码 " + fundCode + " 不存在！");
        }
        return fundBaseInfoMapper.deleteFundBaseInfoByCode(fundCode);
    }

    /**
     * 根据基金代码查询基金详情
     * @param fundCode 基金代码
     * @return 基金详情
     */
    public FundBaseInfo getFundBaseInfoByCode(String fundCode) {
        return fundBaseInfoMapper.selectFundDetailByCode(fundCode);
    }

    /**
     * 计算基金成立至今的时长（如"3年120天"）
     * @param inceptDate 成立日期
     * @return 时长字符串
     */
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
