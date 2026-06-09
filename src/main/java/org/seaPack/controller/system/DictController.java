package org.seaPack.controller.system;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.system.Dict;
import org.seaPack.service.system.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/dict")
public class DictController {

    @Autowired
    private DictService dictService;

    @GetMapping("/list")
    public ResponseEntity<PageInfo<Dict>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String dictType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(dictService.getList(pageNum, pageSize, dictType, keyword, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dict> detail(@PathVariable Long id) {
        return ResponseEntity.ok(dictService.getById(id));
    }

    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody Dict dict) {
        return ResponseEntity.ok(dictService.insert(dict));
    }

    @PostMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody Dict dict) {
        return ResponseEntity.ok(dictService.update(dict));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(dictService.delete(id));
    }
}