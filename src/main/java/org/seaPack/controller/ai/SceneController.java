package org.seaPack.controller.ai;

import com.github.pagehelper.PageInfo;
import org.seaPack.model.ai.Scene;
import org.seaPack.model.ai.SceneAgent;
import org.seaPack.model.ai.SceneKnowledge;
import org.seaPack.service.ai.SceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 场景管理控制器
 * <p>提供场景的增删改查、复制、启停管理、关联管理（Agent、知识库）接口。</p>
 */
@RestController
@RequestMapping("/ai/scenes")
public class SceneController {

    @Autowired
    private SceneService sceneService;

    // ===== 场景主体 CRUD =====

    /** 分页查询场景列表 */
    @GetMapping("/page/list")
    public PageInfo<Scene> pageList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return sceneService.getList(pageNum, pageSize, status, keyword);
    }

    /** 全量场景列表（下拉选择用） */
    @GetMapping("/all")
    public List<Scene> all() {
        return sceneService.getAll();
    }

    /** 查询场景详情 */
    @GetMapping("/detail/{id}")
    public ResponseEntity<Scene> detail(@PathVariable Long id) {
        Scene scene = sceneService.getById(id);
        if (scene == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(scene);
    }

    /** 新增场景 */
    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestBody Scene scene) {
        if (sceneService.isCodeDuplicate(scene.getCode(), null)) {
            return ResponseEntity.badRequest().body("场景编码已存在: " + scene.getCode());
        }
        scene.setCreatedBy(getCurrentUserId());
        sceneService.insert(scene);
        return ResponseEntity.ok(scene);
    }

    /** 编辑场景 */
    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody Scene scene) {
        if (scene.getId() == null) {
            return ResponseEntity.badRequest().body("场景 ID 不能为空");
        }
        if (scene.getCode() != null && sceneService.isCodeDuplicate(scene.getCode(), scene.getId())) {
            return ResponseEntity.badRequest().body("场景编码已存在: " + scene.getCode());
        }
        sceneService.update(scene);
        return ResponseEntity.ok(scene);
    }

    /** 删除场景 */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        sceneService.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }

    /** 复制场景 */
    @PostMapping("/copy/{id}")
    public ResponseEntity<?> copy(@PathVariable Long id) {
        try {
            Scene copy = sceneService.copy(id);
            return ResponseEntity.ok(copy);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 启停切换 */
    @PutMapping("/updateStatus/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return ResponseEntity.badRequest().body("状态值无效，仅支持 0（禁用）或 1（启用）");
        }
        sceneService.updateStatus(id, status);
        return ResponseEntity.ok("操作成功");
    }

    // ===== 关联管理：Agent =====

    /** 获取场景关联的 Agent 列表 */
    @GetMapping("/{sceneId}/agents")
    public List<SceneAgent> getAgents(@PathVariable Long sceneId) {
        return sceneService.getAgents(sceneId);
    }

    /** 添加关联 Agent */
    @PostMapping("/{sceneId}/agents")
    public ResponseEntity<?> addAgent(@PathVariable Long sceneId, @RequestBody SceneAgent sceneAgent) {
        try {
            sceneAgent.setSceneId(sceneId);
            sceneService.addAgent(sceneAgent);
            return ResponseEntity.ok(sceneAgent);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 更新关联 Agent */
    @PutMapping("/{sceneId}/agents/{id}")
    public ResponseEntity<?> updateAgent(@PathVariable Long sceneId, @PathVariable Long id, @RequestBody SceneAgent sceneAgent) {
        sceneAgent.setId(id);
        sceneAgent.setSceneId(sceneId);
        sceneService.updateAgent(sceneAgent);
        return ResponseEntity.ok(sceneAgent);
    }

    /** 删除关联 Agent */
    @DeleteMapping("/{sceneId}/agents/{id}")
    public ResponseEntity<?> deleteAgent(@PathVariable Long sceneId, @PathVariable Long id) {
        sceneService.deleteAgent(id);
        return ResponseEntity.ok().build();
    }

    // ===== 关联管理：知识库 =====

    /** 获取场景关联的知识库列表 */
    @GetMapping("/{sceneId}/knowledge")
    public List<SceneKnowledge> getKnowledgeList(@PathVariable Long sceneId) {
        return sceneService.getKnowledgeList(sceneId);
    }

    /** 添加关联知识库 */
    @PostMapping("/{sceneId}/knowledge")
    public ResponseEntity<?> addKnowledge(@PathVariable Long sceneId, @RequestBody SceneKnowledge sceneKnowledge) {
        try {
            sceneKnowledge.setSceneId(sceneId);
            sceneService.addKnowledge(sceneKnowledge);
            return ResponseEntity.ok(sceneKnowledge);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 更新关联知识库 */
    @PutMapping("/{sceneId}/knowledge/{id}")
    public ResponseEntity<?> updateKnowledge(@PathVariable Long sceneId, @PathVariable Long id, @RequestBody SceneKnowledge sceneKnowledge) {
        sceneKnowledge.setId(id);
        sceneKnowledge.setSceneId(sceneId);
        sceneService.updateKnowledge(sceneKnowledge);
        return ResponseEntity.ok(sceneKnowledge);
    }

    /** 删除关联知识库 */
    @DeleteMapping("/{sceneId}/knowledge/{id}")
    public ResponseEntity<?> deleteKnowledge(@PathVariable Long sceneId, @PathVariable Long id) {
        sceneService.deleteKnowledge(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 从 SecurityContext 中获取当前登录用户 ID
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}
