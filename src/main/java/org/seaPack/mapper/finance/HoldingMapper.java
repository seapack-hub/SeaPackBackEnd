package org.seaPack.mapper.finance;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.finance.Holding;

@Mapper
public interface HoldingMapper {

    /**
     * 按条件统计持仓数量
     * @param holding 查询条件
     * @return 记录数
     */
    long countByExample(Holding holding);

    /**
     * 按条件删除持仓记录
     * @param holding 条件
     * @return 影响行数
     */
    int deleteByExample(Holding holding);

    /**
     * 根据主键删除持仓
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * 新增持仓（全字段插入）
     * @param row 持仓数据
     * @return 影响行数
     */
    int insert(Holding row);

    /**
     * 新增持仓（仅非空字段）
     * @param row 持仓数据
     * @return 影响行数
     */
    int insertSelective(Holding row);

    /**
     * 查询持仓列表（支持条件筛选）
     * @param holding 查询条件
     * @return 持仓列表
     */
    List<Holding> selectHoldingList(@Param("holding") Holding holding);

    /**
     * 根据主键查询持仓
     * @param id 主键ID
     * @return 持仓信息
     */
    Holding selectByPrimaryKey(Integer id);

    /**
     * 按条件更新（非空字段）
     * @param row 新数据
     * @param holding 条件
     * @return 影响行数
     */
    int updateByExampleSelective(@Param("row") Holding row, @Param("holding") Holding holding);

    /**
     * 按条件更新（全字段）
     * @param row 新数据
     * @param holding 条件
     * @return 影响行数
     */
    int updateByExample(@Param("row") Holding row, @Param("holding") Holding holding);

    /**
     * 根据主键更新（非空字段）
     * @param row 新数据
     * @return 影响行数
     */
    int updateByPrimaryKeySelective(Holding row);

    /**
     * 根据主键更新（全字段）
     * @param row 新数据
     * @return 影响行数
     */
    int updateByPrimaryKey(Holding row);
}