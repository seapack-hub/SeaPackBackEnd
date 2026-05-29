package org.seaPack.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.Dict;

import java.util.List;

@Mapper
public interface DictMapper {

    /**
     * 根据字典类型查询字典列表
     * @param dictType 字典类型编码
     * @return 字典列表
     */
    List<Dict> selectDictListByType(@Param("dictType") String dictType);
}
