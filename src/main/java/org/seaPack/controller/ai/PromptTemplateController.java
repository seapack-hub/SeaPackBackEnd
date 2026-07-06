package org.seaPack.controller.ai;

import com.github.pagehelper.PageInfo;
import org.seaPack.dto.ai.AiExecuteResult;
import org.seaPack.dto.ai.PromptExecuteRequest;
import org.seaPack.model.ai.PromptTemplate;
import org.seaPack.service.ai.PromptTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 提示词模板控制器
 * <p>提供提示词模板的增删改查、复制、使用统计及启停管理接口。</p>
 */
@RestController
@RequestMapping("/ai/prompt-templates")
public class PromptTemplateController {

    @Autowired
    private PromptTemplateService templateService;

    /**
     * 分页查询模板列表
     *
     * @param category 分类筛选（可选）
     * @param status   状态筛选（可选，1启用 0禁用）
     * @param keyword  名称/编码关键词（可选）
     */
    @GetMapping("/page/list")
    public PageInfo<PromptTemplate> pageList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return templateService.getList(pageNum, pageSize, category, status, keyword);
    }

    /**
     * 全量模板列表（下拉选择用）
     * <p>仅返回已启用的模板，不带分页。</p>
     */
    @GetMapping("/all")
    public List<PromptTemplate> all() {
        return templateService.getAll();
    }

    /**
     * 查询模板详情（含变量定义）
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<PromptTemplate> detail(@PathVariable Long id) {
        PromptTemplate template = templateService.getById(id);
        if (template == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(template);
    }

    /**
     * 新增模板
     * <p>新增前校验 code 是否唯一。</p>
     */
    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestBody PromptTemplate template) {
        if (templateService.isCodeDuplicate(template.getCode(), null)) {
            return ResponseEntity.badRequest().body("模板编码已存在: " + template.getCode());
        }
        template.setCreatedBy(getCurrentUserId());
        templateService.insert(template);
        return ResponseEntity.ok(template);
    }

    /**
     * 编辑模板
     * <p>更新时校验 code 是否被其他模板占用。</p>
     */
    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody PromptTemplate template) {
        if (template.getId() == null) {
            return ResponseEntity.badRequest().body("模板 ID 不能为空");
        }
        if (template.getCode() != null && templateService.isCodeDuplicate(template.getCode(), template.getId())) {
            return ResponseEntity.badRequest().body("模板编码已存在: " + template.getCode());
        }
        templateService.update(template);
        return ResponseEntity.ok(template);
    }

    /** 删除模板 */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        templateService.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }

    /**
     * 复制模板（创建副本）
     * <p>复制原模板内容，名称后追加"（副本）"，编码追加"_copy"。</p>
     */
    @PostMapping("/copy/{id}")
    public ResponseEntity<?> copy(@PathVariable Long id) {
        try {
            PromptTemplate copy = templateService.copy(id);
            return ResponseEntity.ok(copy);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 增加使用次数 */
    @PutMapping("/incrementUse/{id}")
    public ResponseEntity<?> incrementUse(@PathVariable Long id) {
        templateService.incrementUseCount(id);
        return ResponseEntity.ok("操作成功");
    }

    /**
     * 执行提示词模板
     * <p>接收模板 ID 和变量值，渲染 Prompt 后调用 LLM 并返回结果。</p>
     */
    @PostMapping("/execute")
    public ResponseEntity<?> execute(@RequestBody PromptExecuteRequest request) {
        try {
            AiExecuteResult result = templateService.execute(request);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 启停切换
     * <p>请求体格式：{ "status": 0 } 或 { "status": 1 }</p>
     */
    @PutMapping("/updateStatus/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return ResponseEntity.badRequest().body("状态值无效，仅支持 0（禁用）或 1（启用）");
        }
        templateService.updateStatus(id, status);
        return ResponseEntity.ok("操作成功");
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
