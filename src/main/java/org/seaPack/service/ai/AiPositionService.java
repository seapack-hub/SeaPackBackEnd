package org.seaPack.service.ai;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.mapper.ai.AiPositionMapper;
import org.seaPack.model.ai.AiPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * AI 功能位置服务
 * <p>提供前端模块/位置的注册、查询和启停管理。</p>
 */
@Service
public class AiPositionService {

    @Autowired
    private AiPositionMapper positionMapper;

    /**
     * 全量查询位置（可按状态筛选）
     *
     * @param status 状态筛选（可选）
     * @return 位置列表
     */
    public List<AiPosition> getAll(Integer status) {
        return positionMapper.selectList(status, null);
    }

    /**
     * 分页查询位置列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param status   状态筛选（可选）
     * @param keyword  关键字搜索（可选，匹配 label/moduleKey/positionKey）
     * @return 分页结果
     */
    public PageInfo<AiPosition> getPage(int pageNum, int pageSize, Integer status, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<AiPosition> list = positionMapper.selectList(status, keyword);
        return new PageInfo<>(list);
    }

    /**
     * 根据 ID 查询位置
     *
     * @param id 位置ID
     * @return 位置实体，不存在返回 null
     */
    public AiPosition getById(Long id) {
        return positionMapper.selectById(id);
    }

    /**
     * 校验唯一约束
     *
     * @param moduleKey  模块标识
     * @param positionKey 位置标识
     * @param excludeId  排除的ID（更新时用）
     * @return true-存在 false-不存在
     */
    public boolean isDuplicate(String moduleKey, String positionKey, Long excludeId) {
        return positionMapper.countByUnique(moduleKey, positionKey, excludeId) > 0;
    }

    /**
     * 新增位置
     *
     * @param position 位置实体
     * @return 影响行数
     */
    @Transactional
    public int insert(AiPosition position) {
        return positionMapper.insert(position);
    }

    /**
     * 更新位置
     *
     * @param position 位置实体（仅更新非空字段）
     * @return 影响行数
     */
    @Transactional
    public int update(AiPosition position) {
        return positionMapper.update(position);
    }

    /**
     * 删除位置
     *
     * @param id 位置ID
     * @return 影响行数
     */
    @Transactional
    public int deleteById(Long id) {
        return positionMapper.deleteById(id);
    }

    /**
     * 更新启停状态
     *
     * @param id     位置ID
     * @param status 状态（1-启用 0-禁用）
     * @return 影响行数
     */
    @Transactional
    public int updateStatus(Long id, Integer status) {
        AiPosition position = new AiPosition();
        position.setId(id);
        position.setStatus(status);
        return positionMapper.update(position);
    }

    /**
     * 获取已启用的模块列表（去重）
     *
     * @return 模块列表 [{moduleKey, label}]
     */
    public List<Map<String, Object>> getModules() {
        return positionMapper.selectModules();
    }
}
