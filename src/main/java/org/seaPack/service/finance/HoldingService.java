package org.seaPack.service.finance;

import com.github.pagehelper.PageHelper; // MyBatis 分页插件
import com.github.pagehelper.PageInfo; // 分页信息
import org.seaPack.mapper.finance.HoldingMapper; // 持仓 Mapper
import org.seaPack.model.finance.Holding; // 持仓实体
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.stereotype.Service; // Spring 服务注解

import java.util.List; // List 集合

/**
 * 持仓服务
 * 提供持仓数据的分页查询功能。
 */
@Service // 标识为 Spring 服务 Bean
public class HoldingService {

    @Autowired // 注入持仓 Mapper
    private HoldingMapper holdingMapper;

    /**
     * 分页查询持仓列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param holding 查询条件
     * @return 分页结果
     */
    public PageInfo<Holding> getHoldingList(int pageNum, int pageSize, Holding holding) {
        PageHelper.startPage(pageNum, pageSize); // 启动分页
        List<Holding> selectHoldingList = holdingMapper.selectHoldingList(holding); // 查询持仓列表
        return new PageInfo<>(selectHoldingList); // 返回分页结果
    }
}