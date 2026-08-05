package org.seaPack.controller.ai;

import com.github.pagehelper.PageInfo;
import org.seaPack.config.Result;
import org.seaPack.dto.ai.QuotaStatsVO;
import org.seaPack.dto.ai.UserQuotaListDTO;
import org.seaPack.dto.ai.UserQuotaMyDTO;
import org.seaPack.dto.ai.UserQuotaSaveRequest;
import org.seaPack.dto.ai.UserUsageDetailDTO;
import org.seaPack.service.ai.TokenQuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 用户 Token 额度管理控制器
 * <p>提供额度配置的增删改查及用户额度使用情况查询。</p>
 */
@RestController
@RequestMapping("/ai/user-quota")
public class UserQuotaController {

    @Autowired
    private TokenQuotaService tokenQuotaService;

    /**
     * 分页查询用户额度列表
     *
     * @param pageNum   页码（默认1）
     * @param pageSize  每页数量（默认10）
     * @param userId    用户ID（可选筛选）
     * @param quotaType 配额类型（可选筛选）
     * @return 分页列表
     */
    @PostMapping("/list")
    public Result<PageInfo<UserQuotaListDTO>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String quotaType) {
        PageInfo<UserQuotaListDTO> page = tokenQuotaService.listQuotas(pageNum, pageSize, userId, quotaType);
        return Result.success(page);
    }

    /**
     * 查询额度配置统计概览
     *
     * @return 统计数据（总用户数、启用数、超限数、禁用数）
     */
    @PostMapping("/stats")
    public Result<QuotaStatsVO> stats() {
        return Result.success(tokenQuotaService.getStats());
    }

    /**
     * 新增或编辑用户额度配置
     *
     * @param request 额度配置数据
     * @param id      编辑时传入（新增不传）
     * @return 操作结果
     */
    @PostMapping("/save")
    public Result<?> save(@RequestBody UserQuotaSaveRequest request,
                          @RequestParam(required = false) Long id) {
        try {
            if (request.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }
            if (request.getQuotaType() == null || request.getQuotaType().isBlank()) {
                return Result.error("配额类型不能为空");
            }
            // daily/monthly 类型未传 startDate 时自动填充为当天日期
            if (request.getStartDate() == null || request.getStartDate().isBlank()) {
                String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                request.setStartDate(today);
            }
            Long quotaId = tokenQuotaService.saveQuota(request, id);
            return Result.success(quotaId);
        } catch (Exception e) {
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 删除额度配置
     *
     * @param id 配额ID
     * @return 操作结果
     */
    @PostMapping("/delete")
    public Result<?> delete(@RequestParam Long id) {
        try {
            tokenQuotaService.deleteQuota(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 启用 / 禁用额度控制
     *
     * @param id     配额ID
     * @param enable true=启用，false=禁用
     * @return 操作结果
     */
    @PostMapping("/toggle")
    public Result<?> toggle(@RequestParam Long id,
                            @RequestParam boolean enable) {
        try {
            tokenQuotaService.toggleQuota(id, enable);
            return Result.success();
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 查询当前登录用户的额度和使用情况
     *
     * @return 当前用户的所有配额及使用情况
     */
    @GetMapping("/my")
    public Result<List<UserQuotaMyDTO>> my() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        List<UserQuotaMyDTO> list = tokenQuotaService.getMyQuota(userId);
        return Result.success(list);
    }

    /**
     * 查询当前用户的使用明细（按日汇总）
     *
     * @param startDate 起始日期 yyyy-MM-dd（必填）
     * @param endDate   结束日期 yyyy-MM-dd（必填）
     * @return 使用明细列表
     */
    @GetMapping("/my-usage")
    public Result<List<UserUsageDetailDTO>> myUsage(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        List<UserUsageDetailDTO> list = tokenQuotaService.getMyUsage(userId, startDate, endDate);
        return Result.success(list);
    }

    // ========================================================================
    //  辅助方法
    // ========================================================================

    /**
     * 从 SecurityContext 中获取当前登录用户 ID
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}
