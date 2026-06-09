package org.seaPack.service.market;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.components.GenericTreeBuilder;
import org.seaPack.dto.market.IndustryStats;
import org.seaPack.exception.CyclicDependencyException;
import org.seaPack.mapper.market.DimIndustryMapper;
import org.seaPack.model.market.DimIndustry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndustryTreeService {

    @Autowired
    private DimIndustryMapper dimIndustryMapper;

    /**
     * 获取完整行业树（带缓存，凌晨 2 点自动刷新）
     */
    @Cacheable(value = "industryTree", key = "'fullTree'", unless = "#result == null || #result.isEmpty()")
    public List<DimIndustry> getIndustryTree() {
        long startTime = System.currentTimeMillis();

        try {
            List<DimIndustry> allIndustries = dimIndustryMapper.selectAllEnabledIndustries();
            log.info("数据库查询到 {} 条启用的行业数据", allIndustries.size());

            List<DimIndustry> validIndustries = preprocessIndustries(allIndustries);

            if (validIndustries.isEmpty()) {
                log.warn("经过预处理没有有效的行业数据可用于构建树");
                return Collections.emptyList();
            }

            log.info("开始构建行业树，有效节点数: {}", validIndustries.size());
            GenericTreeBuilder<DimIndustry, String> treeBuilder =
                    GenericTreeBuilder.create(
                            DimIndustry::getIndustryCode,
                            DimIndustry::getParentCode,
                            DimIndustry::setChildren,
                            (a, b) -> a.getIndustryCode().compareTo(b.getIndustryCode())
                    );
            List<DimIndustry> industryTree = treeBuilder.buildTree(allIndustries);

            long endTime = System.currentTimeMillis();
            log.info("行业树构建完成，总耗时: {}ms", (endTime - startTime));

            return industryTree;
        } catch (Exception e) {
            log.error("构建行业树失败", e);
            return handleTreeBuildError(e);
        }
    }

    /**
     * 预处理行业数据：过滤空 ID/空名称的记录
     */
    private List<DimIndustry> preprocessIndustries(List<DimIndustry> industries) {
        if (CollectionUtils.isEmpty(industries)) {
            return Collections.emptyList();
        }

        return industries.stream()
                .filter(Objects::nonNull)
                .filter(industry -> {
                    if (industry.getIndustryCode() == null) {
                        log.warn("跳过行业编码为空的记录: {}", industry);
                        return false;
                    }
                    if (industry.getIndustryName() == null) {
                        log.warn("跳过行业名称为空的记录，行业编码: {}", industry.getIndustryCode());
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据异常类型选择降级策略
     */
    private List<DimIndustry> handleTreeBuildError(Exception e) {
        if (e instanceof IllegalArgumentException) {
            log.error("参数验证失败: {}", e.getMessage());
            return Collections.emptyList();
        } else if (e instanceof CyclicDependencyException) {
            log.error("检测到循环依赖: {}", e.getMessage());
            return buildNonStrictTree();
        } else {
            log.error("未知错误构建行业树: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 非严格模式：跳过循环依赖检测，使用简易父指针构建
     */
    private List<DimIndustry> buildNonStrictTree() {
        try {
            List<DimIndustry> allIndustries = dimIndustryMapper.selectAllEnabledIndustries();
            List<DimIndustry> validIndustries = preprocessIndustries(allIndustries);

            return buildSimpleTree(validIndustries);
        } catch (Exception ex) {
            log.error("非严格模式下构建也失败", ex);
            return Collections.emptyList();
        }
    }

    /**
     * 简易树构建：按父指针映射组装（不检测循环）
     */
    private List<DimIndustry> buildSimpleTree(List<DimIndustry> industries) {
        if (CollectionUtils.isEmpty(industries)) {
            return Collections.emptyList();
        }

        Map<String, DimIndustry> nodeMap = new HashMap<>();
        List<DimIndustry> rootNodes = new ArrayList<>();

        for (DimIndustry industry : industries) {
            nodeMap.put(industry.getIndustryCode(), industry);
        }

        for (DimIndustry industry : industries) {
            if (industry.getParentCode() == null || industry.getParentCode().isEmpty()) {
                rootNodes.add(industry);
            } else {
                DimIndustry parent = nodeMap.get(industry.getParentCode());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(industry);
                }
            }
        }

        rootNodes.sort(Comparator.comparing(DimIndustry::getIndustryCode));
        return rootNodes;
    }

    /**
     * 获取指定行业编码的子树
     */
    public Optional<DimIndustry> getSubTree(String industryCode) {
        List<DimIndustry> fullTree = getIndustryTree();
        return findSubtree(fullTree, industryCode);
    }

    /**
     * 按行业名称关键字搜索
     */
    public List<DimIndustry> searchIndustries(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getIndustryTree();
        }
        return dimIndustryMapper.searchByIndustryName(keyword.trim());
    }

    /**
     * 获取行业统计数据
     */
    public IndustryStats getIndustryStats() {
        Long totalCount = dimIndustryMapper.countEnabledIndustries();
        return IndustryStats.builder()
                .totalCount(totalCount)
                .generatedTime(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 手动刷新行业树缓存
     */
    @CacheEvict(value = "industryTree", allEntries = true)
    public void refreshCache() {
        log.info("行业树缓存已手动刷新");
    }

    /**
     * 定时任务：每天凌晨 2 点自动刷新行业树缓存
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @CacheEvict(value = "industryTree", allEntries = true)
    public void scheduledCacheRefresh() {
        log.info("定时任务: 行业树缓存自动刷新");
    }

    /**
     * 递归查找行业子树
     */
    private Optional<DimIndustry> findSubtree(List<DimIndustry> tree, String industryCode) {
        for (DimIndustry node : tree) {
            if (node.getIndustryCode().equals(industryCode)) {
                return Optional.of(node);
            }
            if (!org.springframework.util.CollectionUtils.isEmpty(node.getChildren())) {
                Optional<DimIndustry> result = findSubtree(node.getChildren(), industryCode);
                if (result.isPresent()) {
                    return result;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 使用 GenericTreeBuilder 构建行业树（可指定排序）
     */
    public List<DimIndustry> buildIndustryTree(List<DimIndustry> allIndustries) {
        GenericTreeBuilder<DimIndustry, String> treeBuilder = GenericTreeBuilder
                .<DimIndustry, String>builder()
                .idGetter(DimIndustry::getIndustryCode)
                .parentIdGetter(DimIndustry::getParentCode)
                .childrenSetter(DimIndustry::setChildren)
                .comparator((a, b) -> a.getIndustryCode().compareTo(b.getIndustryCode()))
                .build();

        List<DimIndustry> enabledIndustries = allIndustries.stream()
                .filter(industry -> industry.getIndustryState() != null && industry.getIndustryState() == 1)
                .collect(java.util.stream.Collectors.toList());

        return treeBuilder.buildTree(enabledIndustries);
    }

    /**
     * 构建以指定行业编码为根的子树
     */
    public List<DimIndustry> buildIndustrySubTree(List<DimIndustry> allIndustries, String industryCode) {
        GenericTreeBuilder<DimIndustry, String> treeBuilder = GenericTreeBuilder
                .<DimIndustry, String>builder()
                .idGetter(DimIndustry::getIndustryCode)
                .parentIdGetter(DimIndustry::getParentCode)
                .childrenSetter(DimIndustry::setChildren)
                .build();

        java.util.Set<String> matchIds = java.util.Collections.singleton(industryCode);
        return treeBuilder.buildSearchTree(allIndustries, matchIds);
    }
}