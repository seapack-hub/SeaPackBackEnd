package org.seaPack.controller.system;

import com.github.pagehelper.PageInfo; // MyBatis 分页信息
import org.seaPack.model.system.User; // 用户实体
import org.seaPack.service.system.UserService; // 用户服务
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.web.bind.annotation.GetMapping; // GET 请求映射
import org.springframework.web.bind.annotation.RequestMapping; // 请求路径映射
import org.springframework.web.bind.annotation.RestController; // REST 控制器

/**
 * 用户控制器
 * 提供用户管理相关接口。
 */
@RestController // 标识为 RESTful 控制器
@RequestMapping("/user") // 请求基础路径
public class UserController {

    @Autowired // 注入用户服务
    private UserService userService;

    /**
     * 分页查询用户列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keywords 关键字搜索
     * @param status 状态筛选
     * @param deptId 部门 ID
     * @param startTime 起始时间
     * @param endTime 截止时间
     */
    @GetMapping("/page/list")
    public PageInfo<User> getUserList(
            int pageNum,
            int pageSize,
            String keywords,
            String status,
            Long deptId,
            String startTime,
            String endTime) {
        return userService.getUserList(pageNum, pageSize, keywords, status, deptId, startTime, endTime);
    }

}