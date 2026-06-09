package org.seaPack.service.market;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.market.IndustrySectorMapper;
import org.seaPack.model.market.IndustrySector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = false)
public class IndustrySectorService {

    @Autowired
    private IndustrySectorMapper industrySectorMapper;

    /**
     * 分页查询行业板块列表
     */
    public PageInfo<IndustrySector> getList(int pageNum, int pageSize, String keyword, Integer nodeLevel, Long parentId) {
        PageHelper.startPage(pageNum, pageSize);
        List<IndustrySector> list = industrySectorMapper.selectList(keyword, nodeLevel, parentId);
        return new PageInfo<>(list);
    }

    /**
     * 根据 ID 查询行业板块
     */
    public IndustrySector getById(Long id) {
        return industrySectorMapper.selectById(id);
    }

    /**
     * 获取行业板块树（根节点递归构建，按 sortOrder 排序）
     */
    public List<IndustrySector> getTree() {
        List<IndustrySector> all = industrySectorMapper.selectList(null, null, null);
        List<IndustrySector> roots = all.stream()
                .filter(n -> n.getParentId() == null)
                .collect(Collectors.toList());
        for (IndustrySector root : roots) {
            buildChildren(root, all);
        }
        roots.sort(Comparator.comparingInt(IndustrySector::getSortOrder));
        return roots;
    }

    /**
     * 递归构建子节点
     */
    private void buildChildren(IndustrySector parent, List<IndustrySector> all) {
        List<IndustrySector> children = all.stream()
                .filter(n -> parent.getId().equals(n.getParentId()))
                .sorted(Comparator.comparingInt(IndustrySector::getSortOrder))
                .collect(Collectors.toList());
        if (!children.isEmpty()) {
            parent.setChildren(children);
            for (IndustrySector child : children) {
                buildChildren(child, all);
            }
        }
    }

    /**
     * 获取指定父节点下的直接子节点
     */
    public List<IndustrySector> getChildren(Long parentId) {
        return industrySectorMapper.selectByParentId(parentId);
    }

    /**
     * 新增行业板块（编码唯一性校验）
     */
    public int insert(IndustrySector sector) {
        if (sector.getCode() != null && !sector.getCode().isEmpty()) {
            IndustrySector existing = industrySectorMapper.selectByCode(sector.getCode());
            if (existing != null) {
                throw new RuntimeException("行业编码 " + sector.getCode() + " 已存在！");
            }
        }
        return industrySectorMapper.insert(sector);
    }

    /**
     * 修改行业板块
     */
    public int update(IndustrySector sector) {
        IndustrySector existing = industrySectorMapper.selectById(sector.getId());
        if (existing == null) {
            throw new RuntimeException("节点 " + sector.getId() + " 不存在！");
        }
        return industrySectorMapper.update(sector);
    }

    /**
     * 删除行业板块
     */
    public int delete(Long id) {
        IndustrySector existing = industrySectorMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("节点 " + id + " 不存在！");
        }
        return industrySectorMapper.deleteById(id);
    }
}