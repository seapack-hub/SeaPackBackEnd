package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.ai.UserTokenUsage;

import java.util.List;

/**
 * 用户每日 Token 使用量 Mapper
 */
@Mapper
public interface UserTokenUsageMapper {

    /**
     * 查询指定用户在指定日期区间的使用量，按天汇总
     *
     * @param userId    用户ID
     * @param startDate 起始日期
     * @param endDate   结束日期
     * @return 使用量列表（含每日汇总）
     */
    List<UserTokenUsage> selectByDateRange(@Param("userId") Long userId,
                                            @Param("startDate") String startDate,
                                            @Param("endDate") String endDate);

    /**
     * 查询指定用户在指定日期区间的使用量总和
     *
     * @param userId    用户ID
     * @param startDate 起始日期
     * @param endDate   结束日期
     * @return 累计 token 数（无记录返回 0）
     */
    Long sumByDateRange(@Param("userId") Long userId,
                        @Param("startDate") String startDate,
                        @Param("endDate") String endDate);

    /**
     * 累加指定用户当日的 token 使用量（原子 upsert）
     *
     * @param userId    用户ID
     * @param usageDate 统计日期
     * @param tokens    本次消耗 token 数
     * @return 受影响行数
     */
    int incrementUsage(@Param("userId") Long userId,
                       @Param("usageDate") String usageDate,
                       @Param("tokens") long tokens);

    /**
     * 插入当日的使用量记录
     */
    int insert(UserTokenUsage usage);

    /**
     * 清理指定日期之前的历史数据
     */
    int deleteBefore(@Param("usageDate") String usageDate);
}