package org.seaPack.service.ai;

import org.seaPack.mapper.ai.SkillParamMapper;
import org.seaPack.model.ai.SkillParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 技能参数服务
 * <p>提供技能输入参数的查询与 CRUD 操作。</p>
 */
@Service
public class SkillParamService {

    @Autowired
    private SkillParamMapper paramMapper;

    /** 根据技能 ID 获取所有参数定义 */
    public List<SkillParam> getBySkillId(Long skillId) {
        return paramMapper.selectBySkillId(skillId);
    }

    /** 根据参数 ID 查询详情 */
    public SkillParam getById(Long id) {
        return paramMapper.selectById(id);
    }

    /** 新增参数 */
    @Transactional
    public int insert(SkillParam param) {
        return paramMapper.insert(param);
    }

    /** 更新参数 */
    @Transactional
    public int update(SkillParam param) {
        return paramMapper.update(param);
    }

    /** 删除单个参数 */
    @Transactional
    public int deleteById(Long id) {
        return paramMapper.deleteById(id);
    }

    /** 删除指定技能下的所有参数（级联删除时使用） */
    @Transactional
    public int deleteBySkillId(Long skillId) {
        return paramMapper.deleteBySkillId(skillId);
    }
}
