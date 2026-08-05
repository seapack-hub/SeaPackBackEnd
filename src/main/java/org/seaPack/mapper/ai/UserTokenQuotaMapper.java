package org.seaPack.mapper.ai;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.dto.ai.QuotaStatsVO;
import org.seaPack.model.ai.UserTokenQuota;

import java.util.List;

/**
 * 用户 Token 配额 Mapper
 */
@Mapper
public interface UserTokenQuotaMapper {

    /**
     * 分页查询用户配额列表（JOIN sys_user 获取用户名）
     */
    List<UserTokenQuota> selectList(@Param("userId") Long userId,
                                    @Param("quotaType") String quotaType);

    /**
     * 按用户ID查询全部配额配置
     */
    List<UserTokenQuota> selectByUserId(@Param("userId") Long userId);

    /**
     * 按用户+类型查询已有配额（用于查重）
     */
    UserTokenQuota selectByUserType(@Param("userId") Long userId,
                                    @Param("quotaType") String quotaType);

    /**
     * 按ID查询配额
     */
    UserTokenQuota selectById(@Param("id") Long id);

    /**
     * 更新配额状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 插入配额配置
     */
    int insert(UserTokenQuota quota);

    /**
     * 更新配额配置
     */
    int update(UserTokenQuota quota);

    /**
     * 删除配额配置
     */
    int deleteById(@Param("id") Long id);

    /**
     * 重置某类型所有配额的启用状态（管理员开关）
     */
    int updateEnabled(@Param("id") Long id, @Param("isEnabled") Integer isEnabled);

    /**
     * 查询额度配置统计概览
     */
    QuotaStatsVO selectStats();
}