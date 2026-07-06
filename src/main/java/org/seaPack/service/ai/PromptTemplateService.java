package org.seaPack.service.ai;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.AiExecuteResult;
import org.seaPack.dto.ai.PromptExecuteRequest;
import org.seaPack.mapper.ai.PromptTemplateMapper;
import org.seaPack.mapper.ai.TemplateVariableMapper;
import org.seaPack.model.ai.PromptTemplate;
import org.seaPack.model.ai.TemplateVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提示词模板服务
 * <p>提供模板的 CRUD、分页查询、复制、使用统计及启停管理。</p>
 */
@Service
public class PromptTemplateService {

    @Autowired
    private PromptTemplateMapper templateMapper;

    @Autowired
    private TemplateVariableMapper variableMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AIProperties aiProperties;

    /**
     * 分页查询模板列表
     *
     * @param category 分类筛选（可选）
     * @param status   状态筛选（可选，1启用 0禁用）
     * @param keyword  名称/编码关键词（可选）
     */
    public PageInfo<PromptTemplate> getList(int pageNum, int pageSize, String category, Integer status, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<PromptTemplate> list = templateMapper.selectList(category, status, keyword);
        return new PageInfo<>(list);
    }

    /** 全量查询已启用的模板列表（下拉选择用） */
    public List<PromptTemplate> getAll() {
        return templateMapper.selectAll();
    }

    /** 根据 ID 查询模板详情 */
    public PromptTemplate getById(Long id) {
        return templateMapper.selectById(id);
    }

    /** 校验模板编码是否已存在（excludeId 用于更新时排除自身） */
    public boolean isCodeDuplicate(String code, Long excludeId) {
        return templateMapper.countByCode(code, excludeId) > 0;
    }

    /** 新增模板（含变量定义） */
    @Transactional
    public int insert(PromptTemplate template) {
        int rows = templateMapper.insert(template);
        saveVariables(template.getId(), template.getVariables());
        return rows;
    }

    /** 更新模板（先删后插变量） */
    @Transactional
    public int update(PromptTemplate template) {
        int rows = templateMapper.update(template);
        variableMapper.deleteByTemplateId(template.getId());
        saveVariables(template.getId(), template.getVariables());
        return rows;
    }

    /** 删除模板（级联删除变量） */
    @Transactional
    public int deleteById(Long id) {
        variableMapper.deleteByTemplateId(id);
        return templateMapper.deleteById(id);
    }

    /** 复制模板（创建副本，含变量） */
    @Transactional
    public PromptTemplate copy(Long id) {
        PromptTemplate source = templateMapper.selectById(id);
        if (source == null) {
            throw new RuntimeException("模板不存在: " + id);
        }

        PromptTemplate copy = new PromptTemplate();
        copy.setName(source.getName() + "（副本）");
        copy.setCode(source.getCode() + "_copy");
        copy.setCategory(source.getCategory());
        copy.setContent(source.getContent());
        copy.setDescription(source.getDescription());
        copy.setOutputFormat(source.getOutputFormat());
        copy.setVersion(source.getVersion());
        copy.setStatus(source.getStatus());
        copy.setCreatedBy(source.getCreatedBy());

        templateMapper.insert(copy);

        // 复制变量定义
        if (source.getVariables() != null && !source.getVariables().isEmpty()) {
            List<TemplateVariable> copiedVars = source.getVariables().stream().map(v -> {
                TemplateVariable var = new TemplateVariable();
                var.setTemplateId(copy.getId());
                var.setVarName(v.getVarName());
                var.setLabel(v.getLabel());
                var.setVarType(v.getVarType());
                var.setRequired(v.getRequired());
                var.setDefaultValue(v.getDefaultValue());
                var.setOptions(v.getOptions());
                var.setPlaceholder(v.getPlaceholder());
                var.setSortOrder(v.getSortOrder());
                return var;
            }).toList();
            variableMapper.batchInsert(copiedVars);
        }

        return copy;
    }

    /** 增加使用次数 */
    @Transactional
    public int incrementUseCount(Long id) {
        return templateMapper.incrementUseCount(id);
    }

    /** 更新启停状态 */
    @Transactional
    public int updateStatus(Long id, Integer status) {
        return templateMapper.updateStatus(id, status);
    }

    /**
     * 执行提示词模板
     * <p>核心流程：加载模板 → 替换 {{variable}} 占位符 → 调用 LLM API → 增加使用次数。</p>
     *
     * @param request 执行请求（含模板 ID 和变量值）
     * @return 执行结果（含渲染后的 Prompt、输出内容、Token 统计、耗时）
     */
    public AiExecuteResult execute(PromptExecuteRequest request) {
        // 1. 加载模板并校验状态
        PromptTemplate template = templateMapper.selectById(request.getTemplateId());
        if (template == null) {
            throw new RuntimeException("模板不存在: " + request.getTemplateId());
        }
        if (template.getStatus() == null || template.getStatus() != 1) {
            throw new RuntimeException("模板已禁用: " + template.getName());
        }

        // 2. 校验模板正文不为空
        String content = template.getContent();
        if (content == null || content.isBlank()) {
            throw new RuntimeException("模板正文为空: " + template.getName());
        }

        // 3. 替换模板中的 {{variable}} 占位符
        Map<String, Object> params = request.getParams();
        if (params == null) {
            params = new HashMap<>();
        }
        String filledPrompt = AiExecuteHelper.replacePlaceholders(content, params);
        if (request.getUserMessage() != null && !request.getUserMessage().isBlank()) {
            filledPrompt += "\n\n用户补充信息：\n" + request.getUserMessage();
        }

        // 4. 调用 LLM API
        AiExecuteResult result;
        try {
            result = AiExecuteHelper.callLLM(filledPrompt, null, null, restTemplate, aiProperties);
        } catch (Exception e) {
            throw new RuntimeException("模板执行失败: " + e.getMessage(), e);
        }
        return result;
    }

    /**
     * 批量保存模板变量
     * <p>设置 templateId 并过滤空变量后批量插入。</p>
     */
    private void saveVariables(Long templateId, List<TemplateVariable> variables) {
        if (variables == null || variables.isEmpty()) {
            return;
        }
        List<TemplateVariable> toSave = variables.stream().filter(v -> v.getVarName() != null && !v.getVarName().isBlank()).toList();
        if (toSave.isEmpty()) {
            return;
        }
        toSave.forEach(v -> v.setTemplateId(templateId));
        variableMapper.batchInsert(toSave);
    }

}
