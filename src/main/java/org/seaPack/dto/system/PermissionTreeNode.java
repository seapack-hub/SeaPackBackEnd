package org.seaPack.dto.system;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限树节点 DTO
 * <p>用于构建前端的递归树形结构，包含节点基本信息及子节点列表。</p>
 */
@Data
public class PermissionTreeNode {
    private Long id;
    private Long parentId;
    private String name;
    private String permKey;
    private Integer type;
    private String path;
    private String component;
    private Integer sortOrder;
    private Integer status;
    private List<PermissionTreeNode> children = new ArrayList<>();
}
