package org.seaPack.mapper.macro;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seaPack.model.macro.SysMacroIndicatorMeta;

import java.util.List;

@Mapper
public interface SysMacroIndicatorMetaMapper {

    List<SysMacroIndicatorMeta> selectAll();

    List<SysMacroIndicatorMeta> selectByFrequency(@Param("frequency") String frequency);

    SysMacroIndicatorMeta selectByCode(@Param("indicatorCode") String indicatorCode);

    int insertOrUpdate(SysMacroIndicatorMeta meta);

    int deleteByCode(@Param("indicatorCode") String indicatorCode);
}
