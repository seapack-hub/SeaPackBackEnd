package org.seaPack.components;

import org.seaPack.exception.CyclicDependencyException;
import org.seaPack.exception.TreeBuildException;
import org.springframework.util.CollectionUtils;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 高性能通用树形结构构建器
 * 时间复杂度：O(n) 替代传统递归的 O(n²)
 * 支持泛型，适用于各种树形结构业务场景
 */
public class GenericTreeBuilder<T,K> {
    private final Function<T, K> idGetter;
    private final Function<T, K> parentIdGetter;
    private final java.util.function.BiConsumer<T, List<T>> childrenSetter;
    private final Comparator<T> comparator;

    /**
     * 私有构造方法，使用Builder模式创建实例
     */
    private GenericTreeBuilder(Function<T, K> idGetter,
                               Function<T, K> parentIdGetter,
                               java.util.function.BiConsumer<T, List<T>> childrenSetter,
                               Comparator<T> comparator) {
        this.idGetter = Objects.requireNonNull(idGetter, "ID获取器不能为null");
        this.parentIdGetter = Objects.requireNonNull(parentIdGetter, "父ID获取器不能为null");
        this.childrenSetter = Objects.requireNonNull(childrenSetter, "子节点设置器不能为null");
        this.comparator = comparator != null ? comparator : (a, b) -> 0;
    }

    /**
     * 构建完整的树形结构
     */
    public List<T> buildTree(List<T> items) {
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }

        long startTime = System.currentTimeMillis();

        try {
            // 1. 数据校验和预处理
            validateItems(items);

            // 2. 建立节点索引映射
            Map<K, T> nodeMap = createNodeMap(items);

            // 3. 循环依赖检测
            detectCyclicDependencies(items, nodeMap);

            // 4. 构建父子关系映射
            Map<K, List<T>> parentChildrenMap = buildParentChildrenMap(items);

            // 5. 设置子节点并构建树结构
            buildTreeStructure(nodeMap, parentChildrenMap);

            // 6. 获取并排序根节点
            List<T> rootNodes = getRootNodes(items, nodeMap);

            long endTime = System.currentTimeMillis();
            System.out.printf("树形结构构建完成: 节点数=%d, 耗时=%dms%n",
                    items.size(), (endTime - startTime));

            return rootNodes;

        } catch (Exception e) {
            throw new TreeBuildException("构建树形结构失败", e);
        }
    }

    /**
     * 构建搜索子树（只包含匹配节点及其路径上的祖先节点）
     */
    public List<T> buildSearchTree(List<T> allItems, Set<K> matchIds) {
        if (CollectionUtils.isEmpty(allItems) || matchIds == null || matchIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查找所有相关节点（匹配节点+路径节点）
        Set<K> relatedIds = findRelatedIds(allItems, matchIds);

        // 过滤出相关节点
        List<T> relatedItems = allItems.stream()
                .filter(item -> relatedIds.contains(idGetter.apply(item)))
                .collect(Collectors.toList());

        return buildTree(relatedItems);
    }

    /**
     * 数据校验
     */
    private void validateItems(List<T> items) {
        if (items == null) {
            throw new IllegalArgumentException("节点列表不能为null");
        }
        // 检查空元素和空ID
        List<T> nullItems = items.stream()
                .filter(Objects::isNull)
                .collect(Collectors.toList());

        if (!nullItems.isEmpty()) {
            throw new IllegalArgumentException("节点列表包含" + nullItems.size() + "个null元素");
        }

        // 检查重复ID
        Map<K, Long> idCounts = items.stream()
                .map(idGetter)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        id -> id,
                        Collectors.counting()
                ));

        List<K> duplicateIds = idCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!duplicateIds.isEmpty()) {
            throw new IllegalArgumentException("发现重复的节点ID: " + duplicateIds);
        }

        // 检查空ID
        List<T> itemsWithNullId = items.stream()
                .filter(item -> idGetter.apply(item) == null)
                .collect(Collectors.toList());

        if (!itemsWithNullId.isEmpty()) {
            throw new IllegalArgumentException("发现" + itemsWithNullId.size() + "个节点的ID为null");
        }
    }

    /**
     * 创建节点ID到节点的映射表
     */
    private Map<K, T> createNodeMap(List<T> items) {
        // 修复：处理可能的重复键和空值
        return items.stream()
                .filter(Objects::nonNull) // 过滤空元素
                .filter(item -> {
                    K id = idGetter.apply(item);
                    if (id == null) {
                        System.err.println("警告: 发现ID为空的节点，已跳过: " + item);
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toMap(
                        idGetter,
                        item -> item,
                        (existing, replacement) -> {
                            // 处理重复ID：选择现有值或记录警告
                            System.err.println("警告: 发现重复ID，使用现有值: " + existing);
                            return existing;
                        },
                        HashMap::new
                ));
    }

    /**
     * 循环依赖检测
     */
    private void detectCyclicDependencies(List<T> items, Map<K, T> nodeMap) {
        if (items == null || items.isEmpty()) {
            return;
        }

        // 创建集合，记录已检测过无循环依赖的节点，避免重复检测
        Set<K> verifiedNodes = new HashSet<>();

        // 使用Stream API安全地构建 id -> parentId 的映射
        Map<K, K> idToParentMap = items.stream()
                .filter(Objects::nonNull) // 过滤掉列表中的null元素，防止空指针异常[6](@ref)
                .filter(item -> {
                    K id = idGetter.apply(item); // 获取当前节点的ID
                    return id != null; // 确保节点ID不为空，无效节点不参与映射构建
                })
                .filter(item -> parentIdGetter.apply(item) != null) // 过滤掉父节点ID为null的节点（即根节点）
                .collect(Collectors.toMap( // 收集器，将流转换为Map
                        // KeyMapper：从节点对象中提取键（节点ID）
                        idGetter,
                        // ValueMapper：从节点对象中提取值（父节点ID），允许为null
                        parentIdGetter,
                        // 合并函数：当遇到重复的键（节点ID）时调用
                        (existing, replacement) -> {
                            // 处理重复键：选择现有值或记录警告
                            System.err.println("警告: 发现重复节点ID，使用第一个值");
                            return existing;
                        },
                        // 指定最终生成的Map类型为HashMap
                        HashMap::new
                ));

        // 遍历列表中的每一个节点
        for (T item : items) {
            // 再次跳过空元素，确保后续操作安全
            if (item == null) continue;

            // 获取当前节点的ID
            K currentId = idGetter.apply(item);
            // 检查该节点是否已在之前的检测中被验证为“安全”
            if (verifiedNodes.contains(currentId)) {
                // 如果已验证，则跳过，避免重复检测
                continue;
            }

            // 使用LinkedHashSet记录当前追踪的路径，保持元素顺序
            Set<K> path = new LinkedHashSet<>();
            // tracingId 用于追踪当前正在检查的节点ID
            K tracingId = currentId;

            // 沿着父节点链向上追踪，直到父节点为null（根节点）或发现循环
            while (tracingId != null) {
                // 尝试将当前tracingId加入路径集合，如果添加失败（说明集合中已存在该ID）
                if (!path.add(tracingId)) {
                    // 则发现循环依赖，抛出异常
                    throw new CyclicDependencyException(buildCyclePath(path, tracingId));
                }

                // 如果当前追踪的节点已经在已验证集合中
                if (verifiedNodes.contains(tracingId)) {
                    // 说明从该节点到根节点的路径已检查过，无循环，可跳出循环
                    break;
                }

                // 从映射中获取当前追踪节点的父节点ID
                K parentId = idToParentMap.get(tracingId);
                // 如果父节点ID为null
                if (parentId == null) {
                    // 说明已到达根节点，此路径无循环，跳出循环
                    break;
                }

                // 直接循环检测: 防止节点指向自身（例如 A 的父节点是 A 自身）
                if (parentId.equals(tracingId)) {
                    throw new CyclicDependencyException("直接循环依赖: " + tracingId);
                }
                // 将追踪ID设置为其父节点ID，继续向上层追踪
                tracingId = parentId;
            }
            // 将当前路径上的所有节点添加到已验证集合中，标记它们为“安全”
            verifiedNodes.addAll(path);
        }
    }

    /**
     * 构建父节点到子节点的映射
     */
    private Map<K, List<T>> buildParentChildrenMap(List<T> items) {
        return items.stream()
                .filter(item -> parentIdGetter.apply(item) != null)
                .collect(Collectors.groupingBy(
                        parentIdGetter,
                        HashMap::new,
                        Collectors.toList()
                ));
    }

    /**
     * 构建树形结构
     */
    private void buildTreeStructure(Map<K, T> nodeMap, Map<K, List<T>> parentChildrenMap) {
        // 为每个节点设置子节点
        nodeMap.forEach((nodeId, node) -> {
            if (node == null) return; // 跳过空节点

            List<T> children = parentChildrenMap.getOrDefault(nodeId, Collections.emptyList())
                    .stream()
                    .filter(Objects::nonNull) // 过滤空子节点
                    .sorted(comparator)
                    .collect(Collectors.toList());

            childrenSetter.accept(node, Collections.unmodifiableList(children));
        });
    }

    /**
     * 获取根节点列表
     */
    private List<T> getRootNodes(List<T> items, Map<K, T> nodeMap) {
        return items.stream()
                .filter(item -> {
                    K parentId = parentIdGetter.apply(item);
                    return parentId == null || !nodeMap.containsKey(parentId);
                })
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * 查找相关ID集合（匹配节点+路径节点）
     */
    private Set<K> findRelatedIds(List<T> allItems, Set<K> matchIds) {
        Map<K, K> idToParentMap = allItems.stream()
                .collect(Collectors.toMap(idGetter, parentIdGetter));

        return matchIds.stream()
                .flatMap(id -> traceAncestors(id, idToParentMap).stream())
                .collect(Collectors.toSet());
    }

    /**
     * 追溯祖先节点链
     */
    private Set<K> traceAncestors(K startId, Map<K, K> idToParentMap) {
        Set<K> ancestors = new LinkedHashSet<>();
        K currentId = startId;

        while (currentId != null && ancestors.add(currentId)) {
            currentId = idToParentMap.get(currentId);
        }

        return ancestors;
    }

    /**
     * 构建循环路径描述信息
     */
    private String buildCyclePath(Set<K> path, K duplicateId) {
        List<K> pathList = new ArrayList<>(path);
        int index = pathList.indexOf(duplicateId);
        List<K> cycle = pathList.subList(index, pathList.size());

        return "检测到循环依赖链: " + cycle.stream()
                .map(Object::toString)
                .collect(Collectors.joining(" → "));
    }

    /**
     * Builder模式创建器
     */
    public static <T, K> Builder<T, K> builder() {
        return new Builder<>();
    }

    /**
     * Builder类
     */
    public static class Builder<T, K> {
        private Function<T, K> idGetter;
        private Function<T, K> parentIdGetter;
        private java.util.function.BiConsumer<T, List<T>> childrenSetter;
        private Comparator<T> comparator;

        public Builder<T, K> idGetter(Function<T, K> idGetter) {
            this.idGetter = idGetter;
            return this;
        }

        public Builder<T, K> parentIdGetter(Function<T, K> parentIdGetter) {
            this.parentIdGetter = parentIdGetter;
            return this;
        }

        public Builder<T, K> childrenSetter(java.util.function.BiConsumer<T, List<T>> childrenSetter) {
            this.childrenSetter = childrenSetter;
            return this;
        }

        public Builder<T, K> comparator(Comparator<T> comparator) {
            this.comparator = comparator;
            return this;
        }

        public GenericTreeBuilder<T, K> build() {
            return new GenericTreeBuilder<>(idGetter, parentIdGetter, childrenSetter, comparator);
        }
    }

    /**
     * 快捷创建方法（使用自然排序）
     */
    public static <T, K extends Comparable<? super K>> GenericTreeBuilder<T, K> createWithNaturalOrder(
            Function<T, K> idGetter,
            Function<T, K> parentIdGetter,
            java.util.function.BiConsumer<T, List<T>> childrenSetter) {
        return new GenericTreeBuilder<>(idGetter, parentIdGetter, childrenSetter,
                Comparator.comparing(idGetter, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    /**
     * 静态工厂方法
     */
    public static <T, K> GenericTreeBuilder<T, K> create(
            Function<T, K> idGetter,
            Function<T, K> parentIdGetter,
            java.util.function.BiConsumer<T, List<T>> childrenSetter,
            Comparator<T> comparator) {
        return new GenericTreeBuilder<>(idGetter, parentIdGetter, childrenSetter, comparator);
    }
}
