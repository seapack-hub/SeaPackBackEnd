package org.seaPack.controller;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.IndustrySector;
import org.seaPack.service.IndustrySectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/industrySector")
public class IndustrySectorController {

    @Autowired
    private IndustrySectorService industrySectorService;

    /**
     * 分页查询行业节点列表（支持筛选）
     * @param pageNum 页码（默认1）
     * @param pageSize 每页条数（默认10）
     * @param keyword 关键字（匹配code或label，可选）
     * @param nodeLevel 层级筛选（可选）
     * @param parentId 父节点ID筛选（可选）
     * @return 分页结果
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
     * 获取行业树形结构
     * @return 行业树
     */
    @GetMapping("/tree")
    public ResponseEntity<List<IndustrySector>> tree() {
        return ResponseEntity.ok(industrySectorService.getTree());
    }

    /**
     * 根据主键ID查询节点详情
     * @param id 主键ID
     * @return 节点信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<IndustrySector> detail(@PathVariable Long id) {
        return ResponseEntity.ok(industrySectorService.getById(id));
    }

    /**
     * 查询指定父节点下的直接子节点
     * @param parentId 父节点ID
     * @return 子节点列表
     */
    @GetMapping("/children/{parentId}")
    public ResponseEntity<List<IndustrySector>> children(@PathVariable Long parentId) {
        return ResponseEntity.ok(industrySectorService.getChildren(parentId));
    }

    /**
     * 新增行业节点（ID自增，无需传入；code需唯一）
     * @param sector 节点信息
     * @return 影响行数
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody IndustrySector sector) {
        return ResponseEntity.ok(industrySectorService.insert(sector));
    }

    /**
     * 更新行业节点
     * @param sector 待更新数据（必须含id）
     * @return 影响行数
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody IndustrySector sector) {
        return ResponseEntity.ok(industrySectorService.update(sector));
    }

    /**
     * 删除行业节点（子节点将级联删除）
     * @param id 主键ID
     * @return 影响行数
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(industrySectorService.delete(id));
    }
}
