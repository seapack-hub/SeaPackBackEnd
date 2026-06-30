package org.seaPack.service.ai;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.ai.SkillCategoryMapper;
import org.seaPack.mapper.ai.SkillMapper;
import org.seaPack.model.ai.SkillCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI 技能分类服务
 * <p>提供分类的分页查询、唯一性校验、CRUD 及删除前引用检查。</p>
 */
@Service
public class SkillCategoryService {

    @Autowired
    private SkillCategoryMapper categoryMapper;

    @Autowired
    private SkillMapper skillMapper;

    /**
     * 分页查询分类列表
     *
     * @param keyword 名称/编码关键词（可选）
     * @param status  状态筛选（可选，1启用 0禁用）
     */
    public PageInfo<SkillCategory> getList(int pageNum, int pageSize, String keyword, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        List<SkillCategory> list = categoryMapper.selectList(keyword, status);
        return new PageInfo<>(list);
    }

    /** 根据 ID 查询分类详情 */
    public SkillCategory getById(Long id) {
        return categoryMapper.selectById(id);
    }

    /** 校验分类编码是否已存在（excludeId 用于更新时排除自身） */
    public boolean isCodeDuplicate(String code, Long excludeId) {
        return categoryMapper.countByCode(code, excludeId) > 0;
    }

    /** 新增分类 */
    @Transactional
    public int insert(SkillCategory category) {
        return categoryMapper.insert(category);
    }

    /** 更新分类 */
    @Transactional
    public int update(SkillCategory category) {
        return categoryMapper.update(category);
    }

    /**
     * 删除分类
     * <p>删除前检查该分类下是否有关联技能，有则抛出异常阻止删除。</p>
     */
    @Transactional
    public int deleteById(Long id) {
        int skillCount = skillMapper.countByCategoryId(id);
        if (skillCount > 0) {
            throw new RuntimeException("该分类下存在 " + skillCount + " 个技能，无法删除");
        }
        return categoryMapper.deleteById(id);
    }
}
