package org.seaPack.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.FundBaseInfo;

@Mapper
public interface FundBaseInfoMapper {
    /**
     * 查询总数
     * @param example
     * @return
     */
    long countByExample(FundBaseInfo example);

    /**
     *
     * @param example
     * @return
     */
    int deleteByExample(FundBaseInfo example);

    /**
     * 根据基金编号删除
     * @param fundCode
     * @return
     */
    int deleteByPrimaryKey(@Param("fundCode") String fundCode);

    /**
     * 插入
     */
    int insert(FundBaseInfo row);

    int insertSelective(FundBaseInfo row);

    /**
     * 查询基金信息列表
     */
    List<FundBaseInfo> selectFundsList(FundBaseInfo fundBaseInfo);

    FundBaseInfo selectByPrimaryKey(String fundCode);

    int updateByExampleSelective(@Param("row") FundBaseInfo row, @Param("example") FundBaseInfo example);

    int updateByExample(@Param("row") FundBaseInfo row, @Param("example") FundBaseInfo example);

    int updateByPrimaryKeySelective(FundBaseInfo row);

    int updateByPrimaryKey(FundBaseInfo row);
}