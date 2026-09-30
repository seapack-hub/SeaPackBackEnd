package org.seaPack.service.system;

import org.seaPack.mapper.system.DictTypeMapper;
import org.seaPack.model.system.DictType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DictTypeService {

    @Autowired
    private DictTypeMapper dictTypeMapper;

    /** 查询所有类型列表 */
    public List<DictType> getTypes(String keyword) {
        return dictTypeMapper.selectList(keyword);
    }

    /** 根据ID查询 */
    public DictType getById(Long id) {
        return dictTypeMapper.selectById(id);
    }

    /** 根据类型编码查询 */
    public DictType getByType(String dictType) {
        return dictTypeMapper.selectByType(dictType);
    }

    /** 新增类型 */
    @Transactional
    public DictType add(DictType dictType) {
        // 检查编码是否已存在
        DictType existing = dictTypeMapper.selectByType(dictType.getDictType());
        if (existing != null) {
            throw new RuntimeException("字典类型编码已存在：" + dictType.getDictType());
        }
        dictType.setStatus("1");
        dictTypeMapper.insert(dictType);
        return dictType;
    }

    /** 更新类型 */
    @Transactional
    public void update(DictType dictType) {
        dictTypeMapper.update(dictType);
    }

    /** 删除类型（同时删除该类型下所有字典值） */
    @Transactional
    public void delete(Long id) {
        DictType dictType = dictTypeMapper.selectById(id);
        if (dictType == null) {
            throw new RuntimeException("字典类型不存在");
        }
        // 先删除类型
        dictTypeMapper.deleteById(id);
        // 再删除该类型下所有字典值
        // 注意：这里需要注入 DictMapper 来批量删除，通过 Service 调用
    }
}
