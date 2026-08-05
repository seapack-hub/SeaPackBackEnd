package org.seaPack.service.ai;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.ai.QuotaStatsVO;
import org.seaPack.dto.ai.UserQuotaListDTO;
import org.seaPack.dto.ai.UserQuotaMyDTO;
import org.seaPack.dto.ai.UserQuotaSaveRequest;
import org.seaPack.dto.ai.UserUsageDetailDTO;
import org.seaPack.mapper.ai.UserTokenQuotaMapper;
import org.seaPack.mapper.ai.UserTokenUsageMapper;
import org.seaPack.mapper.system.UserMapper;
import org.seaPack.model.ai.UserTokenQuota;
import org.seaPack.model.ai.UserTokenUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Token 额度管理服务
 * <p>提供额度校验、扣减、重置、状态同步及管理接口所需的业务逻辑。</p>
 */
@Slf4j
@Service
public class TokenQuotaService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private UserTokenQuotaMapper quotaMapper;

    @Autowired
    private UserTokenUsageMapper usageMapper;

    @Autowired
    private UserMapper sysUserMapper;

    // ========================================================================
    //  核心校验（AI 调用入口处调用）
    // ========================================================================

    /**
     * 检查用户额度是否超限
     * <p>在调用大模型之前调用。无配置或 quotaLimit=0 时放行，配置启用时检查实际用量。</p>
     *
     * @param userId 当前用户ID
     * @return null 表示放行，非 null 表示拒绝原因
     */
    public String checkQuota(Long userId) {
        if (userId == null) {
            return null; // 未登录不限制
        }
        List<UserTokenQuota> quotas = quotaMapper.selectByUserId(userId);
        if (quotas == null || quotas.isEmpty()) {
            return null; // 无配置，放行
        }

        for (UserTokenQuota q : quotas) {
            if (q.getIsEnabled() == null || q.getIsEnabled() != 1) {
                continue; // 已禁用，跳过
            }
            if (q.getQuotaLimit() == null || q.getQuotaLimit() <= 0) {
                continue; // quotaLimit=0 表示不限制，放行
            }

            long used = getUsedTokens(userId, q);
            long limit = q.getQuotaLimit();

            if (used >= limit) {
                // 已超限
                if (!"exceeded".equals(q.getStatus())) {
                    quotaMapper.updateStatus(q.getId(), "exceeded");
                }
                return "Token额度已用完，请联系管理员";
            }

            // 状态同步：预警
            int threshold = q.getAlertThreshold() != null ? q.getAlertThreshold() : 80;
            BigDecimal percent = BigDecimal.valueOf(used).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(limit), 1, RoundingMode.HALF_UP);
            if (percent.intValue() >= threshold) {
                if (!"warning".equals(q.getStatus())) {
                    quotaMapper.updateStatus(q.getId(), "warning");
                }
            } else {
                if ("warning".equals(q.getStatus()) || "exceeded".equals(q.getStatus())) {
                    quotaMapper.updateStatus(q.getId(), "normal");
                }
            }
        }
        return null; // 全部通过，放行
    }

    /**
     * 获取用户额度使用情况（供响应头/前端展示）
     *
     * @param userId 用户ID
     * @return 使用情况 Map（key: quotaType, value: {limit, used, remaining}），无配置返回空 Map
     */
    public java.util.Map<String, Object> getQuotaUsage(Long userId) {
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        if (userId == null) {
            return result;
        }
        List<UserTokenQuota> quotas = quotaMapper.selectByUserId(userId);
        if (quotas == null || quotas.isEmpty()) {
            return result;
        }
        for (UserTokenQuota q : quotas) {
            long used = getUsedTokens(userId, q);
            long limit = q.getQuotaLimit() != null ? q.getQuotaLimit() : 0;
            java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("limit", limit);
            item.put("used", used);
            item.put("remaining", Math.max(0, limit - used));
            item.put("status", q.getStatus());
            result.put(q.getQuotaType(), item);
        }
        return result;
    }

    // ========================================================================
    //  额度扣减（LLM 调用完成后调用）
    // ========================================================================

    /**
     * 扣减用户 token 使用量
     * <p>在每次 LLM 调用完成后（TokenStatsService.recordCall 内部）调用。</p>
     *
     * @param userId     用户ID
     * @param tokensTotal 本次消耗的总 token 数（prompt + completion）
     */
    @Transactional
    public void recordUsage(Long userId, long tokensTotal) {
        if (userId == null || tokensTotal <= 0) {
            return;
        }
        String today = SDF.format(new Date());
        // 原子累加当日使用量
        usageMapper.incrementUsage(userId, today, tokensTotal);
    }

    // ========================================================================
    //  管理接口（Controller 调用）
    // ========================================================================

    /**
     * 查询额度配置统计概览
     *
     * @return 统计数据（总用户数、启用数、超限数、禁用数）
     */
    public QuotaStatsVO getStats() {
        return quotaMapper.selectStats();
    }

    /**
     * 分页查询用户额度列表
     */
    public PageInfo<UserQuotaListDTO> listQuotas(int pageNum, int pageSize,
                                                 Long userId, String quotaType) {
        PageHelper.startPage(pageNum, pageSize);
        List<UserTokenQuota> quotas = quotaMapper.selectList(userId, quotaType);

        List<UserQuotaListDTO> dtos = new ArrayList<>();
        for (UserTokenQuota q : quotas) {
            UserQuotaListDTO dto = new UserQuotaListDTO();
            dto.setId(q.getId());
            dto.setUserId(q.getUserId());
            dto.setUserName(getUserName(q.getUserId()));
            dto.setQuotaType(q.getQuotaType());
            dto.setQuotaLimit(q.getQuotaLimit());
            dto.setAlertThreshold(q.getAlertThreshold());
            dto.setIsEnabled(q.getIsEnabled());
            dto.setStatus(q.getStatus());
            dto.setStartDate(q.getStartDate() != null ? SDF.format(q.getStartDate()) : null);
            dto.setEndDate(q.getEndDate() != null ? SDF.format(q.getEndDate()) : null);
            dto.setCreatedAt(q.getCreatedAt() != null ? SDF.format(q.getCreatedAt()) : null);

            // 实时计算已用量
            long used = getUsedTokens(q.getUserId(), q);
            dto.setUsedTokens(used);
            if (q.getQuotaLimit() != null && q.getQuotaLimit() > 0) {
                dto.setUsagePercent(BigDecimal.valueOf(used)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(q.getQuotaLimit()), 1, RoundingMode.HALF_UP));
            } else {
                dto.setUsagePercent(BigDecimal.ZERO);
            }
            dtos.add(dto);
        }
        return new PageInfo<>(dtos);
    }

    /**
     * 新增或编辑用户额度配置
     *
     * @param request 保存请求
     * @param id      编辑时传入（新增时传 null）
     * @return 配额ID
     */
    public Long saveQuota(UserQuotaSaveRequest request, Long id) {
        UserTokenQuota quota = new UserTokenQuota();
        quota.setUserId(request.getUserId());
        quota.setQuotaType(request.getQuotaType());
        quota.setQuotaLimit(request.getQuotaLimit() != null ? request.getQuotaLimit() : 0L);
        quota.setAlertThreshold(request.getAlertThreshold() != null ? request.getAlertThreshold() : 80);
        quota.setIsEnabled(1);
        quota.setStatus("normal");

        // 处理日期
        if (request.getStartDate() != null && !request.getStartDate().isBlank()) {
            quota.setStartDate(parseDate(request.getStartDate()));
        } else {
            quota.setStartDate(new Date());
        }
        if (request.getEndDate() != null && !request.getEndDate().isBlank()) {
            quota.setEndDate(parseDate(request.getEndDate()));
        }

        if (id != null) {
            quota.setId(id);
            quotaMapper.update(quota);
            return id;
        } else {
            // 查重：同一用户+类型已存在则更新，否则新增
            UserTokenQuota existing = quotaMapper.selectByUserType(
                    quota.getUserId(), quota.getQuotaType());
            if (existing != null) {
                quota.setId(existing.getId());
                quotaMapper.update(quota);
                return existing.getId();
            }
            quotaMapper.insert(quota);
            return quota.getId();
        }
    }

    /**
     * 删除额度配置
     */
    public void deleteQuota(Long id) {
        quotaMapper.deleteById(id);
    }

    /**
     * 启用 / 禁用额度控制
     */
    public void toggleQuota(Long id, boolean enable) {
        quotaMapper.updateEnabled(id, enable ? 1 : 0);
    }

    /**
     * 查询当前用户的额度和使用情况
     */
    public List<UserQuotaMyDTO> getMyQuota(Long userId) {
        List<UserTokenQuota> quotas = quotaMapper.selectByUserId(userId);
        List<UserQuotaMyDTO> dtos = new ArrayList<>();
        if (quotas == null) {
            return dtos;
        }
        for (UserTokenQuota q : quotas) {
            UserQuotaMyDTO dto = new UserQuotaMyDTO();
            dto.setQuotaId(q.getId());
            dto.setQuotaType(q.getQuotaType());
            dto.setQuotaLimit(q.getQuotaLimit());
            dto.setAlertThreshold(q.getAlertThreshold());
            dto.setStatus(q.getStatus());
            dto.setStartDate(q.getStartDate() != null ? SDF.format(q.getStartDate()) : null);
            dto.setEndDate(q.getEndDate() != null ? SDF.format(q.getEndDate()) : null);

            long used = getUsedTokens(userId, q);
            dto.setUsedTokens(used);
            if (q.getQuotaLimit() != null && q.getQuotaLimit() > 0) {
                dto.setRemainingTokens(Math.max(0, q.getQuotaLimit() - used));
                dto.setUsagePercent(BigDecimal.valueOf(used)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(q.getQuotaLimit()), 1, RoundingMode.HALF_UP));
            } else {
                dto.setRemainingTokens(0L);
                dto.setUsagePercent(BigDecimal.ZERO);
            }

            // 当日调用次数
            UserTokenUsage todayUsage = getTodayUsage(userId);
            dto.setCallCount(todayUsage != null ? todayUsage.getCallCount() : 0);

            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * 查询当前用户的使用明细（按日/月汇总）
     *
     * @param userId    用户ID
     * @param startDate 起始日期 yyyy-MM-dd
     * @param endDate   结束日期 yyyy-MM-dd
     * @return 使用明细列表
     */
    public List<UserUsageDetailDTO> getMyUsage(Long userId, String startDate, String endDate) {
        List<UserTokenUsage> usages = usageMapper.selectByDateRange(userId, startDate, endDate);
        List<UserUsageDetailDTO> dtos = new ArrayList<>();
        for (UserTokenUsage u : usages) {
            UserUsageDetailDTO dto = new UserUsageDetailDTO();
            dto.setStatDate(u.getUsageDate() != null ? SDF.format(u.getUsageDate()) : "");
            dto.setTokensUsed(u.getTokensUsed());
            dto.setCallCount(u.getCallCount());
            dtos.add(dto);
        }
        return dtos;
    }

    // ========================================================================
    //  定时任务（额度重置）
    // ========================================================================

    /**
     * 每日 00:05 执行：重置 daily 类型配额状态
     * <p>将 daily 配额的 status 重置为 normal，start_date/end_date 更新为当日日期。</p>
     */
    @Scheduled(cron = "0 5 0 * * ?")
    @Transactional
    public void dailyReset() {
        log.info("[额度重置] 开始执行 daily 配额重置");
        String today = SDF.format(new Date());
        // 重置所有 daily 配额状态为 normal
        List<UserTokenQuota> dailyQuotas = quotaMapper.selectList(null, "daily");
        for (UserTokenQuota q : dailyQuotas) {
            quotaMapper.updateStatus(q.getId(), "normal");
            q.setStartDate(parseDate(today));
            q.setEndDate(null);
            quotaMapper.update(q);
        }
        // 清理 90 天前的历史数据
        String cleanBefore = SDF.format(Date.from(
                java.time.LocalDate.now().minusDays(90)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()));
        int deleted = usageMapper.deleteBefore(cleanBefore);
        log.info("[额度重置] daily 配额重置完成，已清理 {} 条历史数据", deleted);
    }

    /**
     * 每月1日 00:10 执行：重置 monthly 类型配额状态
     * <p>将 monthly 配额的 status 重置为 normal，start_date 更新为当月1号，end_date 更新为当月最后一天。</p>
     */
    @Scheduled(cron = "0 10 0 1 * ?")
    @Transactional
    public void monthlyReset() {
        log.info("[额度重置] 开始执行 monthly 配额重置");
        YearMonth currentMonth = YearMonth.now();
        String startDate = currentMonth.atDay(1).format(DATE_FMT);
        String endDate = currentMonth.atEndOfMonth().format(DATE_FMT);

        List<UserTokenQuota> monthlyQuotas = quotaMapper.selectList(null, "monthly");
        for (UserTokenQuota q : monthlyQuotas) {
            quotaMapper.updateStatus(q.getId(), "normal");
            q.setStartDate(parseDate(startDate));
            q.setEndDate(parseDate(endDate));
            quotaMapper.update(q);
        }
        log.info("[额度重置] monthly 配额重置完成");
    }

    // ========================================================================
    //  内部方法
    // ========================================================================

    /**
     * 根据配额类型计算已用 token 数
     *
     * @param userId 用户ID
     * @param quota  配额配置
     * @return 已用 token 数
     */
    private long getUsedTokens(Long userId, UserTokenQuota quota) {
        if (userId == null || quota == null) {
            return 0L;
        }
        switch (quota.getQuotaType()) {
            case "daily": {
                // 当日使用量
                String today = SDF.format(new Date());
                Long sum = usageMapper.sumByDateRange(userId, today, today);
                return sum != null ? sum : 0L;
            }
            case "monthly": {
                // 本月使用量
                YearMonth currentMonth = YearMonth.now();
                String monthStart = currentMonth.atDay(1).format(DATE_FMT);
                String monthEnd = currentMonth.atEndOfMonth().format(DATE_FMT);
                Long sum = usageMapper.sumByDateRange(userId, monthStart, monthEnd);
                return sum != null ? sum : 0L;
            }
            case "total": {
                // 全部使用量（从 usage 表最老记录开始）
                String minDate = "2000-01-01";
                String maxDate = "9999-12-31";
                Long sum = usageMapper.sumByDateRange(userId, minDate, maxDate);
                return sum != null ? sum : 0L;
            }
            default:
                return 0L;
        }
    }

    /**
     * 查询用户当日的使用量记录
     */
    private UserTokenUsage getTodayUsage(Long userId) {
        String today = SDF.format(new Date());
        List<UserTokenUsage> usages = usageMapper.selectByDateRange(userId, today, today);
        return (usages != null && !usages.isEmpty()) ? usages.get(0) : null;
    }

    /**
     * 根据用户ID查询用户名
     */
    private String getUserName(Long userId) {
        if (userId == null) return "未知";
        try {
            org.seaPack.model.system.User user = sysUserMapper.selectUserById(userId);
            return user != null && user.getUserName() != null ? user.getUserName() : String.valueOf(userId);
        } catch (Exception e) {
            return String.valueOf(userId);
        }
    }

    /**
     * 解析日期字符串为 Date
     */
    private Date parseDate(String dateStr) {
        try {
            return SDF.parse(dateStr);
        } catch (Exception e) {
            return new Date();
        }
    }
}