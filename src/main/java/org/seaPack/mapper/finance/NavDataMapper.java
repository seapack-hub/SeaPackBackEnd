package org.seaPack.mapper.finance;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.finance.NavData;
import org.seaPack.model.finance.NavDataExample;

@Mapper
public interface NavDataMapper {

    /**
     * 按条件统计净值记录数
     * @param example 查询条件
     * @return 记录数
     */
    long countByExample(NavDataExample example);

    /**
     * 按条件删除净值记录
     * @param example 条件
     * @return 影响行数
     */
    int deleteByExample(NavDataExample example);

    /**
     * 根据主键删除净值记录
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * 新增净值记录（全字段插入）
     * @param row 净值数据
     * @return 影响行数
     */
    int insert(NavData row);

    /**
     * 新增净值记录（仅非空字段）
     * @param row 净值数据
     * @return 影响行数
     */
    int insertSelective(NavData row);

    /**
     * 按条件查询净值列表
     * @param example 查询条件
     * @return 净值列表
     */
    List<NavData> selectByExample(NavDataExample example);

    /**
     * 根据主键查询净值
     * @param id 主键ID
     * @return 净值信息
     */
    NavData selectByPrimaryKey(Integer id);

    /**
     * 按条件更新（非空字段）
     * @param row 新数据
     * @param example 条件
     * @return 影响行数
     */
    int updateByExampleSelective(@Param("row") NavData row, @Param("example") NavDataExample example);

    /**
     * 按条件更新（全字段）
     * @param row 新数据
     * @param example 条件
     * @return 影响行数
     */
    int updateByExample(@Param("row") NavData row, @Param("example") NavDataExample example);

    /**
     * 根据主键更新（非空字段）
     * @param row 新数据
     * @return 影响行数
     */
    int updateByPrimaryKeySelective(NavData row);

    /**
     * 根据主键更新（全字段）
     * @param row 新数据
     * @return 影响行数
     */
    int updateByPrimaryKey(NavData row);
}