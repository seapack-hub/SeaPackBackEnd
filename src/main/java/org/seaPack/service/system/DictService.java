package org.seaPack.service.system;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.system.DictMapper;
import org.seaPack.model.system.Dict;
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
     * 分页查询字典列表
     */
    public PageInfo<Dict> getList(int pageNum, int pageSize, String dictType, String keyword, String status) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(dictMapper.selectList(dictType, keyword, status));
    }

    /**
     * 根据 ID 查询字典详情
     */
    public Dict getById(Long id) {
        return dictMapper.selectById(id);
    }

    /**
     * 新增字典项（校验字典类型+编码唯一性）
     */
    public int insert(Dict dict) {
        Dict existing = dictMapper.selectByTypeAndCode(dict.getDictType(), dict.getDictCode());
        if (existing != null) {
            throw new RuntimeException("字典类型 " + dict.getDictType() + " 下编码 " + dict.getDictCode() + " 已存在！");
        }
        return dictMapper.insert(dict);
    }

    /**
     * 修改字典项
     */
    public int update(Dict dict) {
        Dict existing = dictMapper.selectById(dict.getId());
        if (existing == null) {
            throw new RuntimeException("字典 " + dict.getId() + " 不存在！");
        }
        return dictMapper.update(dict);
    }

    /**
     * 删除字典项
     */
    public int delete(Long id) {
        Dict existing = dictMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("字典 " + id + " 不存在！");
        }
        return dictMapper.deleteById(id);
    }
}