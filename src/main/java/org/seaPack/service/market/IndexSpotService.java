package org.seaPack.service.market;

import org.seaPack.mapper.market.IndexSpotMapper;
import org.seaPack.model.market.IndexSpot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 大盘指数服务
 */
@Service
public class IndexSpotService {

    @Autowired
    private IndexSpotMapper indexSpotMapper;

    /**
     * 查询所有大盘指数（按 sort_order 排序）
     */
    public List<IndexSpot> listAll() {
        return indexSpotMapper.selectAll();
    }
}
