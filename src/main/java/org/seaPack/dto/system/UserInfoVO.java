package org.seaPack.dto.system;

import lombok.Data;
import org.seaPack.model.system.permission.SysRole;
import org.seaPack.model.system.permission.SysPermission;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户认证信息 VO
 * <p>登录成功后返回给前端，包含角色编码列表和权限标识符列表。</p>
 */
@Data
public class UserInfoVO {
    private Long userId;
    private String userName;
    private List<String> roles;
    private List<String> permissions;

    /**
     * 工厂方法：从用户角色和权限列表构建 UserInfoVO
     *
     * @param roleList       用户角色列表
     * @param permissionList 用户拥有的所有权限（已去重）
     */
    public static UserInfoVO of(Long userId, String userName,
                                List<SysRole> roleList,
                                List<SysPermission> permissionList) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(userId);
        vo.setUserName(userName);
        vo.setRoles(roleList.stream().map(SysRole::getRoleCode).collect(Collectors.toList()));
        vo.setPermissions(permissionList.stream()
                .filter(p -> p.getPermKey() != null && !p.getPermKey().isEmpty())
                .map(SysPermission::getPermKey)
                .collect(Collectors.toList()));
        return vo;
    }
}
