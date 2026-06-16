package org.seaPack.controller.auth;

import org.seaPack.components.RsaUtil;
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
     * <p>支持滑块验证码校验（可在开发环境关闭），密码经 RSA 公钥加密传输，
     * 后端解密后与数据库密码比对。</p>
     * @param token    滑块验证码 token（isVerify=false 时校验）
     * @param sliderX  用户拖拽 x 坐标
     * @param username 用户名
     * @param password RSA 加密后的密码
     * @param isVerify 是否跳过滑块验证（true=跳过，用于开发调试）
     */
    @GetMapping("/login")
    public ResponseEntity<String> login(
            @RequestParam(defaultValue = "" ) String token,
            @RequestParam(defaultValue = "0" ) double sliderX,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(defaultValue = "true" ) boolean isVerify
    ) throws Exception {
        if (!isVerify && !captchaService.verifyCaptcha(token, sliderX)) {
            return ResponseEntity.status(200).body("滑块验证失败");
        }

        if(!username.isEmpty() && !password.isEmpty()){
            User user = userService.selectUserByName(username);
            if (user == null) return ResponseEntity.status(401).body("用户名错误");

            String rawPassword = rsaUtil.decrypt(password);
            if(rawPassword.equals(user.getPassword())){
                return ResponseEntity.ok("登录成功");
            }
        }
        return ResponseEntity.status(401).body("用户名或密码错误");
    }

    /**
     * 获取当前登录用户信息及权限
     * <p>返回用户角色编码列表和权限标识符集合（如 ['user:add', 'role:delete']）。
     * 前端存入 Pinia/Vuex，用于 v-permission 指令判断按钮显隐。</p>
     *
     * @param userId 用户 ID（待接入 JWT 后改为从 token 解析）
     */
    @GetMapping("/user-info")
    public ResponseEntity<UserInfoVO> userInfo(@RequestParam Long userId) {
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
     *
     * @param userId 用户 ID（待接入 JWT 后改为从 token 解析）
     */
    @GetMapping("/menus")
    public ResponseEntity<List<PermissionTreeNode>> menus(@RequestParam Long userId) {
        return ResponseEntity.ok(authService.getUserMenus(userId));
    }
}