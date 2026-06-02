package org.seaPack.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.IndustrySectorMapper;
import org.seaPack.model.IndustrySector;
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
     * 分页查询行业节点列表（可选筛选条件）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keyword 关键字（匹配code或label，可选）
     * @param nodeLevel 层级筛选（可选）
     * @param parentId 父节点筛选（可选）
     * @return 分页结果
     */
    public PageInfo<IndustrySector> getList(int pageNum, int pageSize, String keyword, Integer nodeLevel, Long parentId) {
        PageHelper.startPage(pageNum, pageSize);
        List<IndustrySector> list = industrySectorMapper.selectList(keyword, nodeLevel, parentId);
        return new PageInfo<>(list);
    }

    /**
     * 根据主键ID查询行业节点
     * @param id 主键ID
     * @return 节点信息
     */
    public IndustrySector getById(Long id) {
        return industrySectorMapper.selectById(id);
    }

    /**
     * 获取行业树形结构（从根节点开始递归构建）
     * @return 树形结构（根节点列表）
     */
    public List<IndustrySector> getTree() {
        List<IndustrySector> all = industrySectorMapper.selectList(null, null, null); // 树结构需要全量数据，不分页
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
     * 递归构建子树
     * @param parent 父节点
     * @param all 全量节点列表
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
     * 查询指定父节点下的直接子节点
     * @param parentId 父节点ID
     * @return 子节点列表
     */
    public List<IndustrySector> getChildren(Long parentId) {
        return industrySectorMapper.selectByParentId(parentId);
    }

    /**
     * 新增行业节点（ID自增无需传入，code需唯一）
     * @param sector 节点信息
     * @return 影响行数
     */
    public int insert(IndustrySector sector) {
        if (sector.getCode() != null && !sector.getCode().isEmpty()) {
            IndustrySector existing = industrySectorMapper.selectByCode(sector.getCode());
            if (existing != null) {
                throw new RuntimeException("业务编码 " + sector.getCode() + " 已存在！");
            }
        }
        return industrySectorMapper.insert(sector);
    }

    /**
     * 更新行业节点
     * @param sector 待更新数据（必须含id）
     * @return 影响行数
     */
    public int update(IndustrySector sector) {
        IndustrySector existing = industrySectorMapper.selectById(sector.getId());
        if (existing == null) {
            throw new RuntimeException("节点 " + sector.getId() + " 不存在！");
        }
        return industrySectorMapper.update(sector);
    }

    /**
     * 删除行业节点（数据库已设ON DELETE CASCADE，子节点将级联删除）
     * @param id 主键ID
     * @return 影响行数
     */
    public int delete(Long id) {
        IndustrySector existing = industrySectorMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("节点 " + id + " 不存在！");
        }
        return industrySectorMapper.deleteById(id);
    }
}
