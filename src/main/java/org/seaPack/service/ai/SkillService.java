package org.seaPack.service.ai;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.ai.SkillMapper;
import org.seaPack.mapper.ai.SkillParamMapper;
import org.seaPack.model.ai.Skill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI 技能服务
 * <p>提供技能的 CRUD、查询等基础操作。</p>
 */
@Service
public class SkillService {

    @Autowired
    private SkillMapper skillMapper;

    @Autowired
    private SkillParamMapper paramMapper;

    /** 查询全量技能列表（不分页） */
    public List<Skill> getAll(Integer status) {
        return skillMapper.selectAll(status);
    }

    /** 分页查询技能列表 */
    public PageInfo<Skill> getList(int pageNum, int pageSize, Long categoryId, String skillType, Integer status, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<Skill> list = skillMapper.selectList(categoryId, skillType, status, keyword);
        return new PageInfo<>(list);
    }

    /** 根据 ID 查询技能详情 */
    public Skill getById(Long id) {
        return skillMapper.selectById(id);
    }

    /** 校验技能编码是否已存在（excludeId 用于更新时排除自身） */
    public boolean isCodeDuplicate(String code, Long excludeId) {
        return skillMapper.countByCode(code, excludeId) > 0;
    }

    /** 新增技能 */
    @Transactional
    public int insert(Skill skill) {
        if (skill.getInputSchema() != null && skill.getInputSchema().isEmpty()) {
            skill.setInputSchema(null);
        }
        return skillMapper.insert(skill);
    }

    /** 更新技能 */
    @Transactional
    public int update(Skill skill) {
        if (skill.getInputSchema() != null && skill.getInputSchema().isEmpty()) {
            skill.setInputSchema(null);
        }
        return skillMapper.update(skill);
    }

    /** 删除技能（级联删除参数） */
    @Transactional
    public int deleteById(Long id) {
        paramMapper.deleteBySkillId(id);
        return skillMapper.deleteById(id);
    }

}
