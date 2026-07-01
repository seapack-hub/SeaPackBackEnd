package org.seaPack.service.ai;

import org.seaPack.dto.ai.SkillBindingVO;
import org.seaPack.mapper.ai.SkillModuleBindingMapper;
import org.seaPack.model.ai.SkillModuleBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 技能-模块绑定服务
 * <p>管理技能在前端各模块中的展示位置绑定关系。</p>
 */
@Service
public class SkillModuleBindingService {

    @Autowired
    private SkillModuleBindingMapper bindingMapper;

    /** 根据技能 ID 查询所有绑定关系 */
    public List<SkillModuleBinding> getBySkillId(Long skillId) {
        return bindingMapper.selectBySkillId(skillId);
    }

    /**
     * 根据模块标识查询绑定的技能列表
     * <p>关联 ai_skill 表获取技能名称、图标等信息，用于前端模块渲染。</p>
     */
    public List<SkillModuleBinding> getByModuleKey(String moduleKey, Integer status) {
        return bindingMapper.selectByModuleKey(moduleKey, status);
    }

    /** 根据绑定 ID 查询详情 */
    public SkillModuleBinding getById(Long id) {
        return bindingMapper.selectById(id);
    }

    /** 新增绑定关系 */
    @Transactional
    public int insert(SkillModuleBinding binding) {
        return bindingMapper.insert(binding);
    }

    /** 更新绑定关系 */
    @Transactional
    public int update(SkillModuleBinding binding) {
        return bindingMapper.update(binding);
    }

    /** 删除单个绑定 */
    @Transactional
    public int deleteById(Long id) {
        return bindingMapper.deleteById(id);
    }

    /**
     * 查询模块绑定的技能列表（含技能详情和参数定义）
     * <p>供前端动态渲染模块按钮和表单使用。</p>
     */
    public List<SkillBindingVO> getBindingsWithDetails(String moduleKey) {
        return bindingMapper.selectBindingsWithDetails(moduleKey);
    }

    /** 删除指定技能的所有绑定（级联删除时使用） */
    @Transactional
    public int deleteBySkillId(Long skillId) {
        return bindingMapper.deleteBySkillId(skillId);
    }
}
