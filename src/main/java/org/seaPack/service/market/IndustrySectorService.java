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

    public PageInfo<IndustrySector> getList(int pageNum, int pageSize, String keyword, Integer nodeLevel, Long parentId) {
        PageHelper.startPage(pageNum, pageSize);
        List<IndustrySector> list = industrySectorMapper.selectList(keyword, nodeLevel, parentId);
        return new PageInfo<>(list);
    }

    public IndustrySector getById(Long id) {
        return industrySectorMapper.selectById(id);
    }

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

    public List<IndustrySector> getChildren(Long parentId) {
        return industrySectorMapper.selectByParentId(parentId);
    }

    public int insert(IndustrySector sector) {
        if (sector.getCode() != null && !sector.getCode().isEmpty()) {
            IndustrySector existing = industrySectorMapper.selectByCode(sector.getCode());
            if (existing != null) {
                throw new RuntimeException("业务编码 " + sector.getCode() + " 已存在！");
            }
        }
        return industrySectorMapper.insert(sector);
    }

    public int update(IndustrySector sector) {
        IndustrySector existing = industrySectorMapper.selectById(sector.getId());
        if (existing == null) {
            throw new RuntimeException("节点 " + sector.getId() + " 不存在！");
        }
        return industrySectorMapper.update(sector);
    }

    public int delete(Long id) {
        IndustrySector existing = industrySectorMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("节点 " + id + " 不存在！");
        }
        return industrySectorMapper.deleteById(id);
    }
}