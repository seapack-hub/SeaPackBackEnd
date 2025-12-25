package org.seaPack.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.Holding;

@Mapper
public interface HoldingMapper {
    long countByExample(Holding holding);

    int deleteByExample(Holding holding);

    int deleteByPrimaryKey(Integer id);

    int insert(Holding row);

    int insertSelective(Holding row);

    /**
     * 查询持仓列表
     * @param holding
     * @return
     */
    List<Holding> selectHoldingList(@Param("holding") Holding holding);

    Holding selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("row") Holding row, @Param("holding") Holding holding);

    int updateByExample(@Param("row") Holding row, @Param("holding") Holding holding);

    int updateByPrimaryKeySelective(Holding row);

    int updateByPrimaryKey(Holding row);
}