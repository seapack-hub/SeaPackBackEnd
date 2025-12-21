package org.seaPack.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.NavData;
import org.seaPack.model.NavDataExample;

public interface NavDataMapper {
    long countByExample(NavDataExample example);

    int deleteByExample(NavDataExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(NavData row);

    int insertSelective(NavData row);

    List<NavData> selectByExample(NavDataExample example);

    NavData selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("row") NavData row, @Param("example") NavDataExample example);

    int updateByExample(@Param("row") NavData row, @Param("example") NavDataExample example);

    int updateByPrimaryKeySelective(NavData row);

    int updateByPrimaryKey(NavData row);
}