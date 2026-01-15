package org.seaPack.controller;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.Dict;
import org.seaPack.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dict")
public class DictController {

    @Autowired
    private DictService dictService;

    @GetMapping("/list")
    public ResponseEntity<List<Dict>> getDictListByType(@RequestParam String dictType) {
        List<Dict> dictList = dictService.selectDictListByType(dictType);
        return ResponseEntity.ok(dictList);
    }
}
