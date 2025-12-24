package org.seaPack.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.DimIndustry;

import java.util.List;

@Mapper
public interface DimIndustryMapper {
    /**
     * 查询所有启用的行业数据（按行业代码排序，便于构建树）
     */
    List<DimIndustry> selectAllEnabledIndustries();

    /**
     * 根据父行业代码查询子节点
     */
    List<DimIndustry> selectByParentCode(@Param("parentCode") String parentCode);

    /**
     * 根据行业代码查询特定节点
     */
    DimIndustry selectByIndustryCode(@Param("industryCode") String industryCode);

    /**
     * 统计启用状态的数据量
     */
    Long countEnabledIndustries();

    /**
     * 根据行业名称模糊搜索
     */
    List<DimIndustry> searchByIndustryName(@Param("keyword") String keyword);
}
