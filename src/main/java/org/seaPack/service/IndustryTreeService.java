package org.seaPack.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.components.GenericTreeBuilder;
import org.seaPack.dto.IndustryStats;
import org.seaPack.exception.CyclicDependencyException;
import org.seaPack.mapper.DimIndustryMapper;
import org.seaPack.model.DimIndustry;
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
     * 获取完整的行业树形结构（带缓存）
     */
    @Cacheable(value = "industryTree", key = "'fullTree'", unless = "#result == null || #result.isEmpty()")
    public List<DimIndustry> getIndustryTree() {
        long startTime = System.currentTimeMillis();

        try {
            List<DimIndustry> allIndustries = dimIndustryMapper.selectAllEnabledIndustries();
            log.info("数据库查询到 {} 条启用行业数据", allIndustries.size());

            // 数据预处理和验证
            List<DimIndustry> validIndustries = preprocessIndustries(allIndustries);

            if (validIndustries.isEmpty()) {
                log.warn("经过预处理后，没有有效的行业数据可用于构建树");
                return Collections.emptyList();
            }

            log.info("开始构建行业树，有效数据量: {}", validIndustries.size());
            // 创建树构建器
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
            // 不要抛出运行时异常，返回空列表并记录错误
            return handleTreeBuildError(e);
        }
    }

    /**
     * 数据预处理方法
     */
    private List<DimIndustry> preprocessIndustries(List<DimIndustry> industries) {
        if (CollectionUtils.isEmpty(industries)) {
            return Collections.emptyList();
        }

        return industries.stream()
                .filter(Objects::nonNull) // 过滤空对象
                .filter(industry -> {
                    // 验证必要字段
                    if (industry.getIndustryCode() == null) {
                        log.warn("跳过行业代码为空的记录: {}", industry);
                        return false;
                    }
                    if (industry.getIndustryName() == null) {
                        log.warn("跳过行业名称为空的记录，行业代码: {}", industry.getIndustryCode());
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 错误处理方法
     */
    private List<DimIndustry> handleTreeBuildError(Exception e) {
        // 根据异常类型返回不同的错误处理结果
        if (e instanceof IllegalArgumentException) {
            log.error("数据验证失败: {}", e.getMessage());
            return Collections.emptyList();
        } else if (e instanceof CyclicDependencyException) {
            log.error("检测到循环依赖: {}", e.getMessage());
            // 可以尝试构建非严格模式的树
            return buildNonStrictTree();
        } else {
            log.error("未知错误构建行业树: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 非严格模式构建树（跳过循环依赖检测）
     */
    private List<DimIndustry> buildNonStrictTree() {
        try {
            List<DimIndustry> allIndustries = dimIndustryMapper.selectAllEnabledIndustries();
            List<DimIndustry> validIndustries = preprocessIndustries(allIndustries);

            // 使用简化版的树构建逻辑，跳过循环依赖检测
            return buildSimpleTree(validIndustries);
        } catch (Exception ex) {
            log.error("非严格模式构建树也失败", ex);
            return Collections.emptyList();
        }
    }

    /**
     * 简化版树构建逻辑
     */
    private List<DimIndustry> buildSimpleTree(List<DimIndustry> industries) {
        if (CollectionUtils.isEmpty(industries)) {
            return Collections.emptyList();
        }

        Map<String, DimIndustry> nodeMap = new HashMap<>();
        List<DimIndustry> rootNodes = new ArrayList<>();

        // 第一次遍历：建立索引
        for (DimIndustry industry : industries) {
            nodeMap.put(industry.getIndustryCode(), industry);
        }

        // 第二次遍历：建立父子关系
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

        // 排序
        rootNodes.sort(Comparator.comparing(DimIndustry::getIndustryCode));
        return rootNodes;
    }

    /**
     * 根据行业代码获取子树
     */
    public Optional<DimIndustry> getSubTree(String industryCode) {
        List<DimIndustry> fullTree = getIndustryTree();
        return findSubtree(fullTree, industryCode);
    }

    /**
     * 搜索行业（支持名称和代码模糊搜索）
     */
    public List<DimIndustry> searchIndustries(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return dimIndustryMapper.searchByIndustryName(keyword.trim());
    }

    /**
     * 获取行业统计信息
     */
    public IndustryStats getIndustryStats() {
        Long totalCount = dimIndustryMapper.countEnabledIndustries();
        return IndustryStats.builder()
                .totalCount(totalCount)
                .generatedTime(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 手动刷新缓存
     */
    @CacheEvict(value = "industryTree", allEntries = true)
    public void refreshCache() {
        log.info("行业树缓存已刷新");
    }

    /**
     * 定时刷新缓存（每天凌晨2点执行）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @CacheEvict(value = "industryTree", allEntries = true)
    public void scheduledCacheRefresh() {
        log.info("定时任务: 行业树缓存已刷新");
    }

    /**
     * 递归查找子树
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
     * 使用通用树构建器构建行业树
     */
    public List<DimIndustry> buildIndustryTree(List<DimIndustry> allIndustries) {
        // 创建行业树构建器
        GenericTreeBuilder<DimIndustry, String> treeBuilder = GenericTreeBuilder
                .<DimIndustry, String>builder()
                .idGetter(DimIndustry::getIndustryCode)
                .parentIdGetter(DimIndustry::getParentCode)
                .childrenSetter(DimIndustry::setChildren)
                .comparator((a, b) -> a.getIndustryCode().compareTo(b.getIndustryCode()))
                .build();

        // 过滤启用状态的行业
        List<DimIndustry> enabledIndustries = allIndustries.stream()
                .filter(industry -> industry.getIndustryState() != null && industry.getIndustryState() == 1)
                .collect(java.util.stream.Collectors.toList());

        return treeBuilder.buildTree(enabledIndustries);
    }

    /**
     * 构建搜索子树
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

