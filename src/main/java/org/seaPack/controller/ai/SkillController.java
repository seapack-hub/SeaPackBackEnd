package org.seaPack.controller.ai;

import com.github.pagehelper.PageInfo;
import org.seaPack.dto.ai.AiExecuteResult;
import org.seaPack.dto.ai.SkillBindingVO;
import org.seaPack.dto.ai.SkillExecuteRequest;
import org.seaPack.model.ai.Skill;
import org.seaPack.model.ai.SkillExecutionLog;
import org.seaPack.model.ai.SkillModuleBinding;
import org.seaPack.model.ai.SkillParam;
import org.seaPack.service.ai.SkillModuleBindingService;
import org.seaPack.service.ai.SkillParamService;
import org.seaPack.service.ai.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 技能控制器
 * <p>提供技能的完整 CRUD、参数管理、模块绑定管理、AI 执行及日志查询接口。</p>
 */
@RestController
@RequestMapping("/ai/skills")
public class SkillController {

    @Autowired
    private SkillService skillService;

    @Autowired
    private SkillParamService paramService;

    @Autowired
    private SkillModuleBindingService bindingService;

    /**
     * 分页查询技能列表
     *
     * @param categoryId 分类 ID（可选）
     * @param moduleKey  模块标识（可选）
     * @param status     状态（可选，1启用 0禁用）
     * @param keyword    名称/编码关键词（可选）
     */
    @GetMapping
    public PageInfo<Skill> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String moduleKey,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return skillService.getList(pageNum, pageSize, categoryId, moduleKey, status, keyword);
    }

    /** 查询技能详情 */
    @GetMapping("/{id}")
    public ResponseEntity<Skill> detail(@PathVariable Long id) {
        Skill skill = skillService.getById(id);
        if (skill == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(skill);
    }

    /**
     * 新增技能
     * <p>新增前校验 code 是否唯一。</p>
     */
    @PostMapping
    public ResponseEntity<?> insert(@RequestBody Skill skill) {
        if (skillService.isCodeDuplicate(skill.getCode(), null)) {
            return ResponseEntity.badRequest().body("技能编码已存在: " + skill.getCode());
        }
        skill.setCreatedBy(getCurrentUserId());
        skillService.insert(skill);
        return ResponseEntity.ok(skill);
    }

    /**
     * 更新技能
     * <p>更新时校验 code 是否被其他技能占用。</p>
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Skill skill) {
        if (skill.getCode() != null && skillService.isCodeDuplicate(skill.getCode(), id)) {
            return ResponseEntity.badRequest().body("技能编码已存在: " + skill.getCode());
        }
        skill.setId(id);
        skillService.update(skill);
        return ResponseEntity.ok(skill);
    }

    /** 删除技能（级联删除参数和绑定） */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        skillService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 执行 AI 技能
     * <p>接收参数和用户补充消息，调用 LLM API 并返回执行结果。</p>
     */
    @PostMapping("/{id}/execute")
    public ResponseEntity<?> execute(@PathVariable Long id, @RequestBody SkillExecuteRequest request) {
        try {
            AiExecuteResult result = skillService.execute(id, request, getCurrentUserId());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** 获取技能的所有参数定义 */
    @GetMapping("/{id}/params")
    public List<SkillParam> getParams(@PathVariable Long id) {
        return paramService.getBySkillId(id);
    }

    /** 为技能新增一个参数定义 */
    @PostMapping("/{id}/params")
    public ResponseEntity<?> addParam(@PathVariable Long id, @RequestBody SkillParam param) {
        param.setSkillId(id);
        paramService.insert(param);
        return ResponseEntity.ok(param);
    }

    /** 更新技能的某个参数定义 */
    @PutMapping("/{skillId}/params/{paramId}")
    public ResponseEntity<?> updateParam(@PathVariable Long skillId, @PathVariable Long paramId, @RequestBody SkillParam param) {
        param.setId(paramId);
        param.setSkillId(skillId);
        paramService.update(param);
        return ResponseEntity.ok(param);
    }

    /** 删除技能的某个参数定义 */
    @DeleteMapping("/{skillId}/params/{paramId}")
    public ResponseEntity<?> deleteParam(@PathVariable Long skillId, @PathVariable Long paramId) {
        paramService.deleteById(paramId);
        return ResponseEntity.ok().build();
    }

    /** 分页查询当前用户的技能执行日志 */
    @GetMapping("/logs")
    public PageInfo<SkillExecutionLog> listLogs(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long skillId,
            @RequestParam(required = false) String skillCode,
            @RequestParam(required = false) String moduleKey,
            @RequestParam(required = false) String status) {
        return skillService.getLogList(pageNum, pageSize, skillId, skillCode, moduleKey, status, getCurrentUserId());
    }

    /** 查询某条执行日志的详情 */
    @GetMapping("/logs/{id}")
    public ResponseEntity<SkillExecutionLog> logDetail(@PathVariable Long id) {
        SkillExecutionLog log = skillService.getLogById(id);
        if (log == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(log);
    }

    /**
     * 查询模块绑定的技能列表（含技能详情和参数定义）
     * <p>前端根据返回数据动态渲染模块按钮和参数表单。</p>
     */
    @GetMapping("/bindings")
    public List<SkillBindingVO> getBindingsWithDetails(@RequestParam(required = false) String moduleKey) {
        return bindingService.getBindingsWithDetails(moduleKey);
    }

    /** 获取技能的所有模块绑定关系 */
    @GetMapping("/{id}/bindings")
    public List<SkillModuleBinding> getBindings(@PathVariable Long id) {
        return bindingService.getBySkillId(id);
    }

    /** 为技能新增一条模块绑定 */
    @PostMapping("/{id}/bindings")
    public ResponseEntity<?> addBinding(@PathVariable Long id, @RequestBody SkillModuleBinding binding) {
        binding.setSkillId(id);
        bindingService.insert(binding);
        return ResponseEntity.ok(binding);
    }

    /** 更新技能的某条模块绑定 */
    @PutMapping("/{skillId}/bindings/{bindingId}")
    public ResponseEntity<?> updateBinding(@PathVariable Long skillId, @PathVariable Long bindingId, @RequestBody SkillModuleBinding binding) {
        binding.setId(bindingId);
        binding.setSkillId(skillId);
        bindingService.update(binding);
        return ResponseEntity.ok(binding);
    }

    /** 删除技能的某条模块绑定 */
    @DeleteMapping("/{skillId}/bindings/{bindingId}")
    public ResponseEntity<?> deleteBinding(@PathVariable Long skillId, @PathVariable Long bindingId) {
        bindingService.deleteById(bindingId);
        return ResponseEntity.ok().build();
    }

    /**
     * 从 SecurityContext 中获取当前登录用户 ID
     * <p>由 JwtAuthenticationFilter 在请求拦截时写入。</p>
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}
