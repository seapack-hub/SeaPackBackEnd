package org.seaPack.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.Holding;
import org.seaPack.model.HoldingExample;

public interface HoldingMapper {
    long countByExample(HoldingExample example);

    int deleteByExample(HoldingExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Holding row);

    int insertSelective(Holding row);

    List<Holding> selectByExample(HoldingExample example);

    Holding selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("row") Holding row, @Param("example") HoldingExample example);

    int updateByExample(@Param("row") Holding row, @Param("example") HoldingExample example);

    int updateByPrimaryKeySelective(Holding row);

    int updateByPrimaryKey(Holding row);
}