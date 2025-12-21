package org.seaPack.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.FundBaseInfo;
import org.seaPack.model.FundBaseInfoExample;

public interface FundBaseInfoMapper {
    long countByExample(FundBaseInfoExample example);

    int deleteByExample(FundBaseInfoExample example);

    int deleteByPrimaryKey(String fundCode);

    int insert(FundBaseInfo row);

    int insertSelective(FundBaseInfo row);

    List<FundBaseInfo> selectByExample(FundBaseInfoExample example);

    FundBaseInfo selectByPrimaryKey(String fundCode);

    int updateByExampleSelective(@Param("row") FundBaseInfo row, @Param("example") FundBaseInfoExample example);

    int updateByExample(@Param("row") FundBaseInfo row, @Param("example") FundBaseInfoExample example);

    int updateByPrimaryKeySelective(FundBaseInfo row);

    int updateByPrimaryKey(FundBaseInfo row);
}