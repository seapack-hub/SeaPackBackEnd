package org.seaPack.components;

import java.util.List;

/**
 * 通用树节点接口
 * 支持泛型，可适用于各种树形结构
 */
public interface TreeNode<T,K> {
    /**
     * 获取节点ID
     */
    K getId();

    /**
     * 获取父节点ID
     */
    K getParentId();

    /**
     * 获取子节点列表
     */
    List<T> getChildren();

    /**
     * 设置子节点列表
     */
    void setChildren(List<T> children);

    /**
     * 添加子节点
     */
    default void addChild(T child) {
        if (getChildren() == null) {
            setChildren(new java.util.ArrayList<>());
        }
        getChildren().add(child);
    }

    /**
     * 判断是否有子节点
     */
    default boolean hasChildren() {
        return getChildren() != null && !getChildren().isEmpty();
    }

    /**
     * 判断是否为根节点
     */
    default boolean isRoot() {
        return getParentId() == null;
    }
}
