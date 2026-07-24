package org.seaPack.controller.ai;

import com.github.pagehelper.PageInfo;
import org.seaPack.dto.ai.SceneBindingInfo;
import org.seaPack.model.ai.*;
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
 * <p>提供场景的增删改查、复制、启停管理、关联管理（Agent、知识库）、部署管理和 Agent 配置管理接口。</p>
 */
@RestController
@RequestMapping("/ai/scenes")
public class SceneController {

    @Autowired
    private SceneService sceneService;

    // ===== 场景主体 CRUD =====

    /**
     * 分页查询场景列表
     *
     * @param pageNum  页码（默认1）
     * @param pageSize 每页条数（默认10）
     * @param status   状态筛选（可选：1-启用 0-禁用）
     * @param keyword  关键字模糊搜索（可选，匹配名称和编码）
     * @return 分页结果
     */
    @GetMapping("/page/list")
    public PageInfo<Scene> pageList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return sceneService.getList(pageNum, pageSize, status, keyword);
    }

    /**
     * 查询全量已启用的场景列表（下拉选择用）
     *
     * @return 已启用的场景列表
     */
    @GetMapping("/all")
    public List<Scene> all() {
        return sceneService.getAll();
    }

    /**
     * 查询场景详情
     *
     * @param id 场景ID
     * @return 场景实体，不存在返回 404
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<Scene> detail(@PathVariable Long id) {
        Scene scene = sceneService.getById(id);
        if (scene == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(scene);
    }

    /**
     * 新增场景
     *
     * @param scene 场景实体（编码必填，需唯一）
     * @return 新增后的场景（含自增ID），编码重复返回 400
     */
    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestBody Scene scene) {
        if (sceneService.isCodeDuplicate(scene.getCode(), null)) {
            return ResponseEntity.badRequest().body("场景编码已存在: " + scene.getCode());
        }
        scene.setCreatedBy(getCurrentUserId());
        sceneService.insert(scene);
        return ResponseEntity.ok(scene);
    }

    /**
     * 编辑场景
     *
     * @param scene 场景实体（ID必填，编码修改需唯一）
     * @return 更新后的场景，校验失败返回 400
     */
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

    /**
     * 删除场景（级联删除关联关系、部署、Agent 配置）
     *
     * @param id 场景ID
     * @return 操作结果
     */
    @PostMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        sceneService.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }

    /**
     * 复制场景（创建副本，含关联关系、部署、Agent 配置）
     *
     * @param id 源场景ID
     * @return 复制后的场景
     */
    @PostMapping("/copy/{id}")
    public ResponseEntity<?> copy(@PathVariable Long id) {
        try {
            Scene copy = sceneService.copy(id);
            return ResponseEntity.ok(copy);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 启停切换
     *
     * @param id   场景ID
     * @param body 请求体，包含 status 字段（1-启用 0-禁用）
     * @return 操作结果
     */
    @PostMapping("/updateStatus/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return ResponseEntity.badRequest().body("状态值无效，仅支持 0（禁用）或 1（启用）");
        }
        sceneService.updateStatus(id, status);
        return ResponseEntity.ok("操作成功");
    }

    // ===== 关联管理：Agent =====

    /**
     * 查询场景关联的 Agent 列表
     *
     * @param sceneId 场景ID
     * @return 关联的 Agent 列表（含名称和编码）
     */
    @GetMapping("/{sceneId}/agents")
    public List<SceneAgent> getAgents(@PathVariable Long sceneId) {
        return sceneService.getAgents(sceneId);
    }

    /**
     * 添加关联 Agent
     *
     * @param sceneId    场景ID
     * @param sceneAgent 关联关系（agent_id / is_default / sort_order）
     * @return 新增的关联关系（含自增ID），重复关联返回 400
     */
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

    /**
     * 更新关联 Agent
     *
     * @param sceneId    场景ID
     * @param id         关联关系ID
     * @param sceneAgent 要更新的字段（agent_id / is_default / sort_order）
     * @return 更新后的关联关系
     */
    @PostMapping("/{sceneId}/agents/{id}/update")
    public ResponseEntity<?> updateAgent(@PathVariable Long sceneId, @PathVariable Long id, @RequestBody SceneAgent sceneAgent) {
        sceneAgent.setId(id);
        sceneAgent.setSceneId(sceneId);
        sceneService.updateAgent(sceneAgent);
        return ResponseEntity.ok(sceneAgent);
    }

    /**
     * 删除关联 Agent
     *
     * @param sceneId 场景ID
     * @param id      关联关系ID
     * @return 操作结果
     */
    @PostMapping("/{sceneId}/agents/{id}/delete")
    public ResponseEntity<?> deleteAgent(@PathVariable Long sceneId, @PathVariable Long id) {
        sceneService.deleteAgent(id);
        return ResponseEntity.ok().build();
    }

    // ===== 关联管理：知识库 =====

    /**
     * 查询场景关联的知识库列表
     *
     * @param sceneId 场景ID
     * @return 关联的知识库列表（含名称）
     */
    @GetMapping("/{sceneId}/knowledge")
    public List<SceneKnowledge> getKnowledgeList(@PathVariable Long sceneId) {
        return sceneService.getKnowledgeList(sceneId);
    }

    /**
     * 添加关联知识库
     *
     * @param sceneId        场景ID
     * @param sceneKnowledge 关联关系（knowledge_id / enabled）
     * @return 新增的关联关系（含自增ID），重复关联返回 400
     */
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

    /**
     * 更新关联知识库
     *
     * @param sceneId        场景ID
     * @param id             关联关系ID
     * @param sceneKnowledge 要更新的字段（knowledge_id / enabled）
     * @return 更新后的关联关系
     */
    @PostMapping("/{sceneId}/knowledge/{id}/update")
    public ResponseEntity<?> updateKnowledge(@PathVariable Long sceneId, @PathVariable Long id, @RequestBody SceneKnowledge sceneKnowledge) {
        sceneKnowledge.setId(id);
        sceneKnowledge.setSceneId(sceneId);
        sceneService.updateKnowledge(sceneKnowledge);
        return ResponseEntity.ok(sceneKnowledge);
    }

    /**
     * 删除关联知识库
     *
     * @param sceneId 场景ID
     * @param id      关联关系ID
     * @return 操作结果
     */
    @PostMapping("/{sceneId}/knowledge/{id}/delete")
    public ResponseEntity<?> deleteKnowledge(@PathVariable Long sceneId, @PathVariable Long id) {
        sceneService.deleteKnowledge(id);
        return ResponseEntity.ok().build();
    }

    // ===== 场景部署管理 =====

    /**
     * 查询场景的所有部署配置
     *
     * @param sceneId 场景ID
     * @return 部署配置列表
     */
    @GetMapping("/{sceneId}/deployments")
    public List<SceneDeployment> getDeployments(@PathVariable Long sceneId) {
        return sceneService.getDeployments(sceneId);
    }

    /**
     * 新增部署
     *
     * @param sceneId    场景ID
     * @param deployment 部署配置（module_key / position_key / config / is_default / sort_order）
     * @return 新增的部署（含自增ID），重复部署返回 400
     */
    @PostMapping("/{sceneId}/deployments")
    public ResponseEntity<?> addDeployment(@PathVariable Long sceneId, @RequestBody SceneDeployment deployment) {
        try {
            deployment.setSceneId(sceneId);
            sceneService.addDeployment(deployment);
            return ResponseEntity.ok(deployment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新部署
     *
     * @param sceneId    场景ID
     * @param id         部署ID
     * @param deployment 要更新的字段（config / is_default / sort_order / status）
     * @return 更新后的部署
     */
    @PostMapping("/{sceneId}/deployments/{id}/update")
    public ResponseEntity<?> updateDeployment(@PathVariable Long sceneId, @PathVariable Long id, @RequestBody SceneDeployment deployment) {
        deployment.setId(id);
        deployment.setSceneId(sceneId);
        sceneService.updateDeployment(deployment);
        return ResponseEntity.ok(deployment);
    }

    /**
     * 删除部署
     *
     * @param sceneId 场景ID
     * @param id      部署ID
     * @return 操作结果
     */
    @PostMapping("/{sceneId}/deployments/{id}/delete")
    public ResponseEntity<?> deleteDeployment(@PathVariable Long sceneId, @PathVariable Long id) {
        sceneService.deleteDeployment(id);
        return ResponseEntity.ok("删除成功");
    }

    /**
     * 启停切换部署
     *
     * @param sceneId 场景ID
     * @param id      部署ID
     * @param body    请求体，包含 status 字段（1-启用 0-禁用）
     * @return 操作结果
     */
    @PostMapping("/{sceneId}/deployments/{id}/status")
    public ResponseEntity<?> updateDeploymentStatus(@PathVariable Long sceneId, @PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return ResponseEntity.badRequest().body("状态值无效，仅支持 0（禁用）或 1（启用）");
        }
        SceneDeployment d = new SceneDeployment();
        d.setId(id);
        d.setStatus(status);
        sceneService.updateDeployment(d);
        return ResponseEntity.ok("操作成功");
    }

    // ===== 场景级 Agent 配置管理 =====

    /**
     * 查询场景的所有 Agent 运行配置
     *
     * @param sceneId 场景ID
     * @return Agent 运行配置列表
     */
    @GetMapping("/{sceneId}/agent-config")
    public List<SceneAgentConfig> getAgentConfigs(@PathVariable Long sceneId) {
        return sceneService.getAgentConfigs(sceneId);
    }

    /**
     * 新增场景级 Agent 配置
     *
     * @param sceneId 场景ID
     * @param config  Agent 运行配置（agent_id / model / temperature / max_tokens / system_prompt / output_format）
     * @return 新增的配置（含自增ID），重复配置返回 400
     */
    @PostMapping("/{sceneId}/agent-config")
    public ResponseEntity<?> addAgentConfig(@PathVariable Long sceneId, @RequestBody SceneAgentConfig config) {
        try {
            config.setSceneId(sceneId);
            sceneService.addAgentConfig(config);
            return ResponseEntity.ok(config);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新场景级 Agent 配置
     *
     * @param sceneId 场景ID
     * @param id      配置ID
     * @param config  要更新的字段
     * @return 更新后的配置
     */
    @PostMapping("/{sceneId}/agent-config/{id}/update")
    public ResponseEntity<?> updateAgentConfig(@PathVariable Long sceneId, @PathVariable Long id, @RequestBody SceneAgentConfig config) {
        config.setId(id);
        config.setSceneId(sceneId);
        sceneService.updateAgentConfig(config);
        return ResponseEntity.ok(config);
    }

    /**
     * 删除场景级 Agent 配置
     *
     * @param sceneId 场景ID
     * @param id      配置ID
     * @return 操作结果
     */
    @PostMapping("/{sceneId}/agent-config/{id}/delete")
    public ResponseEntity<?> deleteAgentConfig(@PathVariable Long sceneId, @PathVariable Long id) {
        sceneService.deleteAgentConfig(id);
        return ResponseEntity.ok("删除成功");
    }

    // ===== 全量绑定查询 =====

    /**
     * 全量绑定查询
     * <p>JOIN 多表查询所有已启用的场景部署、默认 Agent、场景级配置和知识库绑定信息，
     * 用于前端动态渲染各模块位置的 AI 功能入口和卡片展示。</p>
     *
     * @return 全量绑定信息列表
     */
    @GetMapping("/bindings/all")
    public List<SceneBindingInfo> getAllBindings() {
        return sceneService.getAllBindings();
    }

    /**
     * 从 SecurityContext 中获取当前登录用户 ID
     *
     * @return 用户 ID，未登录返回 null
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}
