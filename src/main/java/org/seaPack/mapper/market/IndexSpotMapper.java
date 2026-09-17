package org.seaPack.mapper.market;

import org.apache.ibatis.annotations.Mapper;
import org.seaPack.model.market.IndexSpot;

import java.util.List;

@Mapper
public interface IndexSpotMapper {

    /**
     * 查询所有大盘指数（按 sort_order 升序）
     */
    List<IndexSpot> selectAll();
}
