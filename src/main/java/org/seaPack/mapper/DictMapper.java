package org.seaPack.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.Dict;

import java.util.List;

@Mapper
public interface DictMapper {

    /**
     * 分页查询字典列表（支持按类型/关键字/状态筛选）
     * @param dictType 字典类型（精确匹配，可选）
     * @param keyword 关键字（模糊匹配dict_code或dict_name，可选）
     * @param status 状态（可选）
     * @return 字典列表
     */
    List<Dict> selectList(@Param("dictType") String dictType,
                          @Param("keyword") String keyword,
                          @Param("status") String status);

    /**
     * 根据主键ID查询字典（仅查未删除的）
     * @param id 主键ID
     * @return 字典信息
     */
    Dict selectById(@Param("id") Long id);

    /**
     * 根据类型+编码查询（用于新增时的唯一性校验）
     * @param dictType 字典类型
     * @param dictCode 字典编码
     * @return 字典信息
     */
    Dict selectByTypeAndCode(@Param("dictType") String dictType,
                             @Param("dictCode") String dictCode);

    /**
     * 新增字典（ID自增无需传入）
     * @param dict 字典信息
     * @return 影响行数
     */
    int insert(Dict dict);

    /**
     * 更新字典
     * @param dict 待更新数据（必须含id）
     * @return 影响行数
     */
    int update(Dict dict);

    /**
     * 逻辑删除字典（设置is_deleted = 1）
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
}
