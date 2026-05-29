package org.seaPack.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.DimIndustry;

import java.util.List;

@Mapper
public interface DimIndustryMapper {

    /**
     * 查询所有启用的行业数据（按行业代码排序，用于构建行业树）
     * @return 行业列表
     */
    List<DimIndustry> selectAllEnabledIndustries();

    /**
     * 根据父行业代码查询直接子节点
     * @param parentCode 父行业代码
     * @return 子行业列表
     */
    List<DimIndustry> selectByParentCode(@Param("parentCode") String parentCode);

    /**
     * 根据行业代码查询特定节点
     * @param industryCode 行业代码
     * @return 行业信息
     */
    DimIndustry selectByIndustryCode(@Param("industryCode") String industryCode);

    /**
     * 统计已启用的行业数量
     * @return 启用行业总数
     */
    Long countEnabledIndustries();

    /**
     * 根据行业名称模糊搜索
     * @param keyword 搜索关键字
     * @return 匹配的行业列表
     */
    List<DimIndustry> searchByIndustryName(@Param("keyword") String keyword);
}
