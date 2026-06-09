package org.seaPack.controller.market;

import com.github.pagehelper.PageInfo; // MyBatis 分页信息
import lombok.extern.slf4j.Slf4j; // Lombok 日志
import org.seaPack.model.market.IndustrySector; // 行业板块实体
import org.seaPack.service.market.IndustrySectorService; // 行业板块服务
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.http.ResponseEntity; // HTTP 响应实体
import org.springframework.web.bind.annotation.*; // Spring Web MVC 注解

import java.util.List; // List 集合

/**
 * 行业板块控制器
 * 提供行业板块的分页查询、树形结构、节点详情、新增、修改和删除接口。
 */
@Slf4j // Lombok 日志
@RestController // 标识为 RESTful 控制器
@RequestMapping("/industrySector") // 请求基础路径
public class IndustrySectorController {

    @Autowired // 注入行业板块服务
    private IndustrySectorService industrySectorService;

    /**
     * 分页查询行业板块列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keyword 关键字（匹配 code 或 label）
     * @param nodeLevel 层级筛选
     * @param parentId 父节点 ID
     */
    @GetMapping("/list")
    public ResponseEntity<PageInfo<IndustrySector>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer nodeLevel,
            @RequestParam(required = false) Long parentId) {
        return ResponseEntity.ok(industrySectorService.getList(pageNum, pageSize, keyword, nodeLevel, parentId));
    }

    /**
     * 获取行业板块树形结构
     */
    @GetMapping("/tree")
    public ResponseEntity<List<IndustrySector>> tree() {
        return ResponseEntity.ok(industrySectorService.getTree());
    }

    /**
     * 查询行业板块详情
     * @param id 节点 ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<IndustrySector> detail(@PathVariable Long id) {
        return ResponseEntity.ok(industrySectorService.getById(id));
    }

    /**
     * 查询指定父节点下的直接子节点
     * @param parentId 父节点 ID
     */
    @GetMapping("/children/{parentId}")
    public ResponseEntity<List<IndustrySector>> children(@PathVariable Long parentId) {
        return ResponseEntity.ok(industrySectorService.getChildren(parentId));
    }

    /**
     * 新增行业节点
     * @param sector 行业节点实体
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody IndustrySector sector) {
        return ResponseEntity.ok(industrySectorService.insert(sector));
    }

    /**
     * 修改行业节点
     * @param sector 行业节点实体（需含 id）
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody IndustrySector sector) {
        return ResponseEntity.ok(industrySectorService.update(sector));
    }

    /**
     * 删除行业节点
     * @param id 节点 ID
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(industrySectorService.delete(id));
    }
}