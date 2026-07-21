package org.seaPack.service.finance;

import com.github.pagehelper.PageHelper; // MyBatis 分页插件
import com.github.pagehelper.PageInfo; // 分页信息
import lombok.extern.slf4j.Slf4j; // Lombok 日志
import org.seaPack.mapper.finance.FundBaseInfoMapper; // 基金 Mapper
import org.seaPack.model.finance.FundBaseInfo; // 基金基本信息实体
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.stereotype.Service; // Spring 服务注解
import org.springframework.transaction.annotation.Transactional; // 事务管理
import java.util.List; // List 集合
import java.time.Instant; // 时间戳转 Instant
import java.time.LocalDate; // 本地日期
import java.time.ZoneId; // 时区
import java.time.temporal.ChronoUnit; // 时间单位

/**
 * 基金基本信息服务
 * 提供基金的分页查询、新增、修改、删除以及成立时间计算功能。
 */
@Slf4j // Lombok 日志
@Service // 标识为 Spring 服务 Bean
@Transactional(readOnly = false) // 启用写入事务
public class FundBaseInfoService {

    @Autowired // 注入基金 Mapper
    private FundBaseInfoMapper fundBaseInfoMapper;

    /**
     * 分页查询基金列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param example 查询条件
     * @return 分页结果（含成立时长）
     */
    @Transactional(readOnly = true)
    public PageInfo<FundBaseInfo> getFundBaseInfoList(int pageNum, int pageSize, FundBaseInfo example) {
        PageHelper.startPage(pageNum, pageSize); // 启动分页
        List<FundBaseInfo> fundBaseInfoList = fundBaseInfoMapper.selectFundsList(example); // 查询基金列表
        for (FundBaseInfo f : fundBaseInfoList) { // 遍历计算成立时长
            f.setDistanceTime(calcDistance(f.getInceptDate())); // 设置距离成立日期的描述
        }
        return new PageInfo<>(fundBaseInfoList); // 返回分页结果
    }

    /**
     * 新增基金
     * @param fundBaseInfo 基金实体
     * @return 影响行数
     */
    public int insertFundBaseInfo(FundBaseInfo fundBaseInfo) {
        int counts = fundBaseInfoMapper.selectFundBaseInfoByCode(fundBaseInfo.getFundCode()); // 校验基金代码是否已存在
        if (counts > 0) { // 已存在则抛出异常
            throw new RuntimeException("基金代码 " + fundBaseInfo.getFundCode() + " 已存在！");
        }
        return fundBaseInfoMapper.insertFundBaseInfo(fundBaseInfo); // 执行插入
    }

    /**
     * 修改基金信息
     * @param fundBaseInfo 基金实体（需含 fundCode）
     * @return 影响行数
     */
    public int updateFundBaseInfo(FundBaseInfo fundBaseInfo) {
        int counts = fundBaseInfoMapper.selectFundBaseInfoByCode(fundBaseInfo.getFundCode()); // 校验基金是否存在
        if (counts == 0) { // 不存在则抛出异常
            throw new RuntimeException("基金代码 " + fundBaseInfo.getFundCode() + " 不存在！");
        }
        return fundBaseInfoMapper.updateFundBaseInfo(fundBaseInfo); // 执行更新
    }

    /**
     * 删除基金
     * @param fundCode 基金代码
     * @return 影响行数
     */
    public int deleteFundBaseInfo(String fundCode) {
        int counts = fundBaseInfoMapper.selectFundBaseInfoByCode(fundCode); // 校验基金是否存在
        if (counts == 0) { // 不存在则抛出异常
            throw new RuntimeException("基金代码 " + fundCode + " 不存在！");
        }
        return fundBaseInfoMapper.deleteFundBaseInfoByCode(fundCode); // 执行删除
    }

    /**
     * 根据基金代码查询详情
     * @param fundCode 基金代码
     * @return 基金详情
     */
    @Transactional(readOnly = true)
    public FundBaseInfo getFundBaseInfoByCode(String fundCode) {
        return fundBaseInfoMapper.selectFundDetailByCode(fundCode); // 查询基金详情
    }

    /**
     * 计算基金成立至今的时长描述
     * @param inceptDate 成立日期
     * @return 格式化字符串（如 "5年120天"）
     */
    private String calcDistance(java.util.Date inceptDate) {
        if (inceptDate == null) return null; // 空值处理
        LocalDate start = Instant.ofEpochMilli(inceptDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate(); // Date 转 LocalDate
        LocalDate now = LocalDate.now(); // 当前日期
        if (start.isAfter(now)) return "0天"; // 未来日期返回 0 天
        long years = ChronoUnit.YEARS.between(start, now); // 计算年数差
        LocalDate afterYears = start.plusYears(years); // 加上年数后的日期
        long days = ChronoUnit.DAYS.between(afterYears, now); // 剩余天数
        if(years == 0l){ // 不足一年只显示天数
            return  days + "天";
        }
        return years + "年" + days + "天"; // 返回 "X年X天" 格式
    }
}