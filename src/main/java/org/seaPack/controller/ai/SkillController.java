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
 * <p>提供技能的完整 CRUD 和参数管理接口，仅使用 GET/POST 两种请求方式。</p>
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
    @GetMapping("/page/list")
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
    @GetMapping("/detail/{id}")
    public ResponseEntity<Skill> detail(@PathVariable Long id) {
        Skill skill = skillService.getById(id);
        if (skill == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(skill);
    }

    /** 新增技能 */
    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestBody Skill skill) {
        if (skillService.isCodeDuplicate(skill.getCode(), null)) {
            return ResponseEntity.badRequest().body("技能编码已存在: " + skill.getCode());
        }
        skill.setCreatedBy(SecurityUtils.getCurrentUserId());
        skillService.insert(skill);
        return ResponseEntity.ok(skill);
    }

    /** 更新技能 */
    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody Skill skill) {
        if (skill.getId() == null) {
            return ResponseEntity.badRequest().body("技能 ID 不能为空");
        }
        if (skill.getCode() != null && skillService.isCodeDuplicate(skill.getCode(), skill.getId())) {
            return ResponseEntity.badRequest().body("技能编码已存在: " + skill.getCode());
        }
        skillService.update(skill);
        return ResponseEntity.ok(skill);
    }

    /** 删除技能（级联删除参数） */
    @PostMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        skillService.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }

    // ===== 参数管理 =====

    /** 获取技能的所有参数定义 */
    @GetMapping("/{skillId}/params")
    public List<SkillParam> getParams(@PathVariable Long skillId) {
        return paramService.getBySkillId(skillId);
    }

    /** 为技能新增一个参数定义 */
    @PostMapping("/{skillId}/add-param")
    public ResponseEntity<?> addParam(@PathVariable Long skillId, @RequestBody SkillParam param) {
        param.setSkillId(skillId);
        paramService.insert(param);
        return ResponseEntity.ok(param);
    }

    /** 更新技能的某个参数定义 */
    @PostMapping("/{skillId}/update-param")
    public ResponseEntity<?> updateParam(@PathVariable Long skillId, @RequestBody SkillParam param) {
        if (param.getId() == null) {
            return ResponseEntity.badRequest().body("参数 ID 不能为空");
        }
        param.setSkillId(skillId);
        paramService.update(param);
        return ResponseEntity.ok(param);
    }

    /** 删除技能的某个参数定义 */
    @PostMapping("/{skillId}/delete-param/{paramId}")
    public ResponseEntity<?> deleteParam(@PathVariable Long skillId, @PathVariable Long paramId) {
        paramService.deleteById(paramId);
        return ResponseEntity.ok("删除成功");
    }

}
