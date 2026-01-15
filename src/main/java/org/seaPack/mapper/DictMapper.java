package org.seaPack.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.Dict;

import java.util.List;

@Mapper
public interface DictMapper {

    /**
     * 更具类型查询字典
     * @param dictType
     * @return
     */
    List<Dict> selectDictListByType(@Param("dictType") String dictType);
}
