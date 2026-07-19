package org.seaPack.controller.ai;

import com.github.pagehelper.PageInfo;
import org.seaPack.config.security.SecurityUtils;
import org.seaPack.model.ai.Skill;
import org.seaPack.model.ai.SkillParam;
import org.seaPack.service.ai.SkillParamService;
import org.seaPack.service.ai.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 技能控制器
 * <p>提供技能的完整 CRUD 和参数管理接口。</p>
 */
@RestController
@RequestMapping("/ai/skills")
public class SkillController {

    @Autowired
    private SkillService skillService;

    @Autowired
    private SkillParamService paramService;

    /** 全量技能列表（不分页） */
    @GetMapping("/all")
    public List<Skill> all(@RequestParam(required = false) Integer status) {
        return skillService.getAll(status);
    }

    /** 分页查询技能列表 */
    @GetMapping
    public PageInfo<Skill> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String skillType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return skillService.getList(pageNum, pageSize, categoryId, skillType, status, keyword);
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

    /** 新增技能 */
    @PostMapping
    public ResponseEntity<?> insert(@RequestBody Skill skill) {
        if (skillService.isCodeDuplicate(skill.getCode(), null)) {
            return ResponseEntity.badRequest().body("技能编码已存在: " + skill.getCode());
        }
        skill.setCreatedBy(SecurityUtils.getCurrentUserId());
        skillService.insert(skill);
        return ResponseEntity.ok(skill);
    }

    /** 更新技能 */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Skill skill) {
        if (skill.getCode() != null && skillService.isCodeDuplicate(skill.getCode(), id)) {
            return ResponseEntity.badRequest().body("技能编码已存在: " + skill.getCode());
        }
        skill.setId(id);
        skillService.update(skill);
        return ResponseEntity.ok(skill);
    }

    /** 删除技能（级联删除参数） */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        skillService.deleteById(id);
        return ResponseEntity.ok(id);
    }

    // ===== 参数管理 =====

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
        return ResponseEntity.ok(paramId);
    }

}
