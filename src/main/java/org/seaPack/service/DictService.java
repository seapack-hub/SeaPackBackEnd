package org.seaPack.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.DictMapper;
import org.seaPack.model.Dict;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = false)
public class DictService {

    @Autowired
    private DictMapper dictMapper;

    /**
     * 分页查询字典列表（支持按类型/关键字/状态筛选）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param dictType 字典类型（精确匹配，可选）
     * @param keyword 关键字（模糊匹配dict_code或dict_name，可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    public PageInfo<Dict> getList(int pageNum, int pageSize, String dictType, String keyword, String status) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(dictMapper.selectList(dictType, keyword, status));
    }

    /**
     * 根据主键ID查询字典
     * @param id 主键ID
     * @return 字典信息
     */
    public Dict getById(Long id) {
        return dictMapper.selectById(id);
    }

    /**
     * 新增字典（同一类型下编码需唯一）
     * @param dict 字典信息
     * @return 影响行数
     */
    public int insert(Dict dict) {
        Dict existing = dictMapper.selectByTypeAndCode(dict.getDictType(), dict.getDictCode());
        if (existing != null) {
            throw new RuntimeException("字典类型 " + dict.getDictType() + " 下编码 " + dict.getDictCode() + " 已存在！");
        }
        return dictMapper.insert(dict);
    }

    /**
     * 更新字典
     * @param dict 待更新数据（必须含id）
     * @return 影响行数
     */
    public int update(Dict dict) {
        Dict existing = dictMapper.selectById(dict.getId());
        if (existing == null) {
            throw new RuntimeException("字典 " + dict.getId() + " 不存在！");
        }
        return dictMapper.update(dict);
    }

    /**
     * 逻辑删除字典
     * @param id 主键ID
     * @return 影响行数
     */
    public int delete(Long id) {
        Dict existing = dictMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("字典 " + id + " 不存在！");
        }
        return dictMapper.deleteById(id);
    }
}
