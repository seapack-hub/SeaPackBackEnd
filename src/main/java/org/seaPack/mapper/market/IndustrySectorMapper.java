package org.seaPack.mapper.market;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.market.IndustrySector;

import java.util.List;

@Mapper
public interface IndustrySectorMapper {

    /**
     * 查询行业节点列表（可选筛选条件，无参数时返回全部）
     * @param keyword 关键字（匹配code或label，可选）
     * @param nodeLevel 层级筛选（可选）
     * @param parentId 父节点筛选（可选）
     * @return 节点列表
     */
    List<IndustrySector> selectList(@Param("keyword") String keyword,
                                        @Param("nodeLevel") Integer nodeLevel,
                                        @Param("parentId") Long parentId);

    /**
     * 根据主键ID查询行业节点
     * @param id 主键ID
     * @return 节点信息
     */
    IndustrySector selectById(@Param("id") Long id);

    /**
     * 根据业务编码查询（用于新增时的唯一性校验）
     * @param code 业务编码
     * @return 节点信息
     */
    IndustrySector selectByCode(@Param("code") String code);

    /**
     * 根据父节点ID查询直接子节点
     * @param parentId 父节点ID
     * @return 子节点列表
     */
    List<IndustrySector> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 统计指定父ID下的子节点数量
     * @param parentId 父节点ID
     * @return 子节点数
     */
    int countChildren(@Param("parentId") Long parentId);

    /**
     * 新增行业节点（ID自增，无需传入）
     * @param sector 节点信息
     * @return 影响行数
     */
    int insert(IndustrySector sector);

    /**
     * 更新行业节点
     * @param sector 待更新数据（必须含id）
     * @return 影响行数
     */
    int update(IndustrySector sector);

    /**
     * 删除行业节点（关联子节点将级联删除）
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
}