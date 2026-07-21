package org.seaPack.service.finance;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.finance.NavDataMapper;
import org.seaPack.model.finance.NavData;
import org.seaPack.model.finance.NavDataExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 基金净值数据服务
 * <p>提供净值数据的分页查询、新增、修改、删除等功能。</p>
 */
@Slf4j
@Service
@Transactional(readOnly = false)
public class NavDataService {

    @Autowired
    private NavDataMapper navDataMapper;

    /**
     * 分页查询净值列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param example 查询条件
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageInfo<NavData> getNavDataList(int pageNum, int pageSize, NavDataExample example) {
        PageHelper.startPage(pageNum, pageSize);
        List<NavData> list = navDataMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    /**
     * 根据基金代码查询净值列表
     * @param fundCode 基金代码
     * @return 净值列表
     */
    @Transactional(readOnly = true)
    public List<NavData> getNavDataByFundCode(String fundCode) {
        NavDataExample example = new NavDataExample();
        example.createCriteria().andFundCodeEqualTo(fundCode);
        example.setOrderByClause("nav_date DESC");
        return navDataMapper.selectByExample(example);
    }

    /**
     * 根据主键查询净值详情
     * @param id 主键ID
     * @return 净值信息
     */
    @Transactional(readOnly = true)
    public NavData getNavDataById(Integer id) {
        return navDataMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增净值记录
     * @param navData 净值数据
     * @return 影响行数
     */
    @Transactional
    public int insertNavData(NavData navData) {
        navData.setLastUpdated(new Date());
        return navDataMapper.insertSelective(navData);
    }

    /**
     * 批量新增净值记录
     * @param navDataList 净值数据列表
     */
    @Transactional
    public void batchInsertNavData(List<NavData> navDataList) {
        for (NavData navData : navDataList) {
            navData.setLastUpdated(new Date());
            navDataMapper.insertSelective(navData);
        }
        log.info("批量新增净值记录 {} 条", navDataList.size());
    }

    /**
     * 更新净值记录
     * @param navData 净值数据（需含 id）
     * @return 影响行数
     */
    @Transactional
    public int updateNavData(NavData navData) {
        navData.setLastUpdated(new Date());
        return navDataMapper.updateByPrimaryKeySelective(navData);
    }

    /**
     * 根据主键删除净值记录
     * @param id 主键ID
     * @return 影响行数
     */
    @Transactional
    public int deleteNavData(Integer id) {
        return navDataMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据基金代码删除所有净值记录
     * @param fundCode 基金代码
     * @return 影响行数
     */
    @Transactional
    public int deleteNavDataByFundCode(String fundCode) {
        NavDataExample example = new NavDataExample();
        example.createCriteria().andFundCodeEqualTo(fundCode);
        return navDataMapper.deleteByExample(example);
    }
}
