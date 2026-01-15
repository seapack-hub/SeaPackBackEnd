package org.seaPack.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.mapper.DictMapper;
import org.seaPack.model.Dict;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DictService {

    @Autowired
    private DictMapper dictMapper;

    public List<Dict> selectDictListByType(String type){
        return dictMapper.selectDictListByType(type);
    }
}
