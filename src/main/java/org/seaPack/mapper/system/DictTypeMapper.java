package org.seaPack.mapper.system;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.system.DictType;

import java.util.List;

@Mapper
public interface DictTypeMapper {

    /** 查询所有字典类型（可选关键字过滤） */
    List<DictType> selectList(@Param("keyword") String keyword);

    /** 根据ID查询 */
    DictType selectById(@Param("id") Long id);

    /** 根据类型编码查询 */
    DictType selectByType(@Param("dictType") String dictType);

    /** 新增 */
    int insert(DictType dictType);

    /** 更新 */
    int update(DictType dictType);

    /** 逻辑删除 */
    int deleteById(@Param("id") Long id);

    /** 批量逻辑删除某个类型编码下的所有字典值（由 DictMapper 调用） */
    // 注：删除字典值由 DictMapper.deleteByType 处理
}
