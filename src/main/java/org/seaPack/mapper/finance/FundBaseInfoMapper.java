package org.seaPack.mapper.finance;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.finance.FundBaseInfo;

@Mapper
public interface FundBaseInfoMapper {

    /**
     * 查询基金信息列表
     * @param fundBaseInfo 查询条件（支持keywords模糊搜索）
     * @return 基金列表
     */
    List<FundBaseInfo> selectFundsList(FundBaseInfo fundBaseInfo);

    /**
     * 新增基金信息
     * @param fundBaseInfo 基金信息
     * @return 影响行数
     */
    int insertFundBaseInfo(FundBaseInfo fundBaseInfo);

    /**
     * 更新基金信息
     * @param fundBaseInfo 待更新数据
     * @return 影响行数
     */
    int updateFundBaseInfo(FundBaseInfo fundBaseInfo);

    /**
     * 根据基金代码查询是否存在（用于唯一性校验）
     * @param fundCode 基金代码
     * @return 记录数
     */
    int selectFundBaseInfoByCode(@Param("fundCode") String fundCode);

    /**
     * 根据基金代码查询基金详情
     * @param fundCode 基金代码
     * @return 基金详情
     */
    FundBaseInfo selectFundDetailByCode(@Param("fundCode") String fundCode);

    /**
     * 根据基金代码删除基金记录
     * @param fundCode 基金代码
     * @return 影响行数
     */
    int deleteFundBaseInfoByCode(@Param("fundCode") String fundCode);
}