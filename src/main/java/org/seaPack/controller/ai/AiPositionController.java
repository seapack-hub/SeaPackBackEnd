package org.seaPack.controller.ai;

import com.github.pagehelper.PageInfo;
import org.seaPack.model.ai.AiPosition;
import org.seaPack.service.ai.AiPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 功能位置控制器
 * <p>提供前端模块/位置的注册、查询、启停管理等接口。</p>
 */
@RestController
@RequestMapping("/ai/positions")
public class AiPositionController {

    @Autowired
    private AiPositionService positionService;

    /**
     * 全量已启用位置（下拉选择用）
     *
     * @param status 状态筛选（可选）
     * @return 位置列表
     */
    @GetMapping("/all")
    public List<AiPosition> all(@RequestParam(required = false) Integer status) {
        return positionService.getAll(status);
    }

    /**
     * 分页查询位置列表（管理用）
     *
     * @param pageNum  页码（默认1）
     * @param pageSize 每页条数（默认10）
     * @param status   状态筛选（可选）
     * @param keyword  关键字搜索（可选）
     * @return 分页结果
     */
    @GetMapping("/page/list")
    public PageInfo<AiPosition> pageList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return positionService.getPage(pageNum, pageSize, status, keyword);
    }

    /**
     * 查询位置详情
     *
     * @param id 位置ID
     * @return 位置实体，不存在返回 404
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<AiPosition> detail(@PathVariable Long id) {
        AiPosition position = positionService.getById(id);
        if (position == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(position);
    }

    /**
     * 新增位置
     *
     * @param position 位置实体（moduleKey + positionKey 联合唯一）
     * @return 新增后的位置（含自增ID），重复返回 400
     */
    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestBody AiPosition position) {
        if (positionService.isDuplicate(position.getModuleKey(), position.getPositionKey(), null)) {
            return ResponseEntity.badRequest().body("该模块下的位置标识已存在: "
                    + position.getModuleKey() + " / " + position.getPositionKey());
        }
        positionService.insert(position);
        return ResponseEntity.ok(position);
    }

    /**
     * 编辑位置
     *
     * @param position 位置实体（ID必填）
     * @return 更新后的位置，校验失败返回 400
     */
    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody AiPosition position) {
        if (position.getId() == null) {
            return ResponseEntity.badRequest().body("位置 ID 不能为空");
        }
        if (position.getModuleKey() != null && position.getPositionKey() != null
                && positionService.isDuplicate(position.getModuleKey(), position.getPositionKey(), position.getId())) {
            return ResponseEntity.badRequest().body("该模块下的位置标识已存在: "
                    + position.getModuleKey() + " / " + position.getPositionKey());
        }
        positionService.update(position);
        return ResponseEntity.ok(position);
    }

    /**
     * 删除位置
     *
     * @param id 位置ID
     * @return 操作结果
     */
    @PostMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        positionService.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }

    /**
     * 启停切换
     *
     * @param id   位置ID
     * @param body 请求体，包含 status 字段（1-启用 0-禁用）
     * @return 操作结果
     */
    @PostMapping("/updateStatus/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return ResponseEntity.badRequest().body("状态值无效，仅支持 0（禁用）或 1（启用）");
        }
        positionService.updateStatus(id, status);
        return ResponseEntity.ok("操作成功");
    }

    /**
     * 获取所有已启用的模块列表（去重）
     *
     * @return 模块列表 [{moduleKey, label}]
     */
    @GetMapping("/modules")
    public List<Map<String, Object>> getModules() {
        return positionService.getModules();
    }
}
