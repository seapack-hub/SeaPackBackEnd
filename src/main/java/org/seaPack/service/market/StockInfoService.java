package org.seaPack.service.market;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.market.IndustrySectorMapper;
import org.seaPack.mapper.market.StockInfoMapper;
import org.seaPack.model.market.IndustrySector;
import org.seaPack.model.market.StockInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = false)
public class StockInfoService {

    @Autowired
    private StockInfoMapper stockInfoMapper;

    @Autowired
    private IndustrySectorMapper industrySectorMapper;

    /**
     * 分页查询股票列表（支持行业递归筛选）
     */
    @Transactional(readOnly = true)
    public PageInfo<StockInfo> getStockList(int pageNum, int pageSize, StockInfo param) {
        expandIndustryIds(param);
        PageHelper.startPage(pageNum, pageSize);
        List<StockInfo> list = stockInfoMapper.selectStockList(param);
        return new PageInfo<>(list);
    }

    /**
     * 递归展开行业 ID：若 industry 为父级，收集所有子级 ID 到 industryIds
     */
    private void expandIndustryIds(StockInfo param) {
        String industry = param.getIndustry();
        if (industry == null || industry.isEmpty()) {
            return;
        }
        List<String> ids = new ArrayList<>();
        collectDescendantIds(Long.valueOf(industry), ids);
        param.setIndustryIds(ids);
    }

    /**
     * 递归收集指定父节点下的所有子孙节点 ID（含自身）
     */
    private void collectDescendantIds(Long parentId, List<String> ids) {
        ids.add(String.valueOf(parentId));
        List<IndustrySector> children = industrySectorMapper.selectByParentId(parentId);
        for (IndustrySector child : children) {
            collectDescendantIds(child.getId(), ids);
        }
    }

    /**
     * 查询全部股票列表（不分页）
     */
    @Transactional(readOnly = true)
    public List<StockInfo> getStockListAll(StockInfo param) {
        expandIndustryIds(param);
        return stockInfoMapper.selectStockList(param);
    }

    /**
     * 根据 ID 查询股票详情
     */
    @Transactional(readOnly = true)
    public StockInfo getStockById(Long stockId) {
        return stockInfoMapper.selectStockById(stockId);
    }

    /**
     * 根据股票代码查询
     */
    @Transactional(readOnly = true)
    public StockInfo getStockByCode(String stockCode) {
        return stockInfoMapper.selectStockByCode(stockCode);
    }

    /**
     * 新增股票（唯一性校验，重复 code 抛异常）
     */
    public int insertStock(StockInfo stockInfo) {
        int count = stockInfoMapper.checkStockCodeExists(stockInfo.getStockCode());
        if (count > 0) {
            throw new RuntimeException("股票代码 " + stockInfo.getStockCode() + " 已存在！");
        }
        return stockInfoMapper.insertStock(stockInfo);
    }

    /**
     * 修改股票信息
     */
    public int updateStock(StockInfo stockInfo) {
        StockInfo existing = stockInfoMapper.selectStockById(stockInfo.getStockId());
        if (existing == null) {
            throw new RuntimeException("股票不存在");
        }
        return stockInfoMapper.updateStock(stockInfo);
    }

    /**
     * 删除股票
     */
    public int deleteStock(Long stockId) {
        StockInfo existing = stockInfoMapper.selectStockById(stockId);
        if (existing == null) {
            throw new RuntimeException("股票不存在");
        }
        return stockInfoMapper.deleteStock(stockId);
    }


}