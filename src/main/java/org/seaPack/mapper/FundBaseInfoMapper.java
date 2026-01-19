package org.seaPack.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.FundBaseInfo;

@Mapper
public interface FundBaseInfoMapper {

    /**
     * 查询基金信息列表
     */
    List<FundBaseInfo> selectFundsList(FundBaseInfo fundBaseInfo);

    /**
     * 插入基金持仓表
     * @param fundBaseInfo
     * @return
     */
    int insertFundBaseInfo(FundBaseInfo fundBaseInfo);

    /**
     * 更新基金信息
     * @param fundBaseInfo
     * @return
     */
    int updateFundBaseInfo(FundBaseInfo fundBaseInfo);

    /**
     * 根据Code查询基金表
     * @param fundCode
     * @return
     */
    int selectFundBaseInfoByCode(@Param("fundCode") String fundCode);

    /**
     * 根据Code查询基金详情
     * @param fundCode
     * @return
     */
    FundBaseInfo selectFundDetailByCode(@Param("fundCode") String fundCode);

    /**
     * 根据Code删除基金持仓表
     * @param fundCode
     * @return
     */
    int deleteFundBaseInfoByCode(@Param("fundCode") String fundCode);
}
