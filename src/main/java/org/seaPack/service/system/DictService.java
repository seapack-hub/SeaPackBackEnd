package org.seaPack.service.system;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.dto.system.DictTypeVO;
import org.seaPack.mapper.system.DictMapper;
import org.seaPack.model.system.Dict;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = false)
public class DictService {

    @Autowired
    private DictMapper dictMapper;

    /**
     * 分页查询字典列表
     */
    @Transactional(readOnly = true)
    public PageInfo<Dict> getList(int pageNum, int pageSize, String dictType, String keyword, String status) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(dictMapper.selectList(dictType, keyword, status));
    }

    /**
     * 根据 ID 查询字典详情
     */
    @Transactional(readOnly = true)
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

    /**
     * 查询所有字典类型及值数量
     */
    @Transactional(readOnly = true)
    public List<DictTypeVO> getTypes(String keyword) {
        return dictMapper.selectTypes(keyword);
    }

    /**
     * 批量逻辑删除某个类型下的所有字典值
     */
    public int deleteByType(String dictType) {
        return dictMapper.deleteByType(dictType);
    }

    /**
     * 查询指定字典类型下的值数量
     */
    @Transactional(readOnly = true)
    public int getCountByType(String dictType) {
        return dictMapper.selectCountByType(dictType);
    }

    /**
     * 按字典类型查询所有启用的字典值（不分页）
     * 用于前端下拉框、格式化器等场景
     */
    @Transactional(readOnly = true)
    public List<Dict> getListByType(String dictType) {
        return dictMapper.selectListByType(dictType);
    }
}