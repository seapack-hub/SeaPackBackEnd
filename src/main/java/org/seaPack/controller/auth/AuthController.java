package org.seaPack.controller.auth;

import org.seaPack.components.RsaUtil;
import org.seaPack.config.security.SecurityUtils;
import org.seaPack.dto.auth.LoginResponse;
import org.seaPack.dto.system.PermissionTreeNode;
import org.seaPack.dto.system.UserInfoVO;
import org.seaPack.model.system.User;
import org.seaPack.service.auth.AuthService;
import org.seaPack.service.auth.CaptchaService;
import org.seaPack.service.system.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证控制器
 * <p>提供用户登录认证接口，支持滑块验证码校验与 RSA 解密密码传输。</p>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private RsaUtil rsaUtil;

    /**
     * 用户登录
     * @param username 用户名
     * @param password RSA 加密后的密码
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestParam String username,
            @RequestParam String password
    ) {

        if(username.isEmpty() || password.isEmpty()){
            return ResponseEntity.status(401).body(null);
        }

        try {
            String rawPassword = rsaUtil.decrypt(password);
            LoginResponse response = authService.login(username, rawPassword);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    /**
     * 获取当前登录用户信息及权限
     * <p>返回用户角色编码列表和权限标识符集合（如 ['user:add', 'role:delete']）。
     * 前端存入 Pinia/Vuex，用于 v-permission 指令判断按钮显隐。</p>
     * <p>userId 从 JWT token 中自动解析，无需前端传参。</p>
     */
    @GetMapping("/user-info")
    public ResponseEntity<UserInfoVO> userInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        UserInfoVO vo = authService.getUserInfo(userId);
        if (vo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vo);
    }

    /**
     * 获取当前用户的动态菜单树
     * <p>仅返回 type=1(目录) 和 type=2(菜单) 的节点，且根据用户角色过滤。
     * 前端拿到后通过 router.addRoute() 动态注册路由并渲染侧边栏。</p>
     * <p>userId 从 JWT token 中自动解析，无需前端传参。</p>
     */
    @GetMapping("/menus")
    public ResponseEntity<List<PermissionTreeNode>> menus() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authService.getUserMenus(userId));
    }
}