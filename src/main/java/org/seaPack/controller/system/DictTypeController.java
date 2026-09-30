package org.seaPack.controller.system;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.system.Dict;
import org.seaPack.model.system.DictType;
import org.seaPack.service.system.DictService;
import org.seaPack.service.system.DictTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字典类型管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/dict/type")
public class DictTypeController {

    @Autowired
    private DictTypeService dictTypeService;

    @Autowired
    private DictService dictService;

    /**
     * 查询所有字典类型列表（可选关键字过滤）
     */
    @GetMapping("/list")
    public ResponseEntity<List<DictType>> list(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(dictTypeService.getTypes(keyword));
    }

    /**
     * 查询所有字典类型列表（含每个类型的字典值数量）
     */
    @GetMapping("/listWithCount")
    public ResponseEntity<List<Map<String, Object>>> listWithCount(
            @RequestParam(required = false) String keyword) {
        List<DictType> types = dictTypeService.getTypes(keyword);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (DictType type : types) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", type.getId());
            item.put("dictType", type.getDictType());
            item.put("dictName", type.getDictName());
            item.put("remark", type.getRemark());
            item.put("orderNum", type.getOrderNum());
            item.put("status", type.getStatus());
            // 查询该类型下的字典值数量
            int count = dictService.getCountByType(type.getDictType());
            item.put("count", count);
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 查询字典类型详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<DictType> detail(@PathVariable Long id) {
        return ResponseEntity.ok(dictTypeService.getById(id));
    }

    /**
     * 新增字典类型
     */
    @PostMapping("/insert")
    public ResponseEntity<DictType> insert(@RequestBody DictType dictType) {
        return ResponseEntity.ok(dictTypeService.add(dictType));
    }

    /**
     * 修改字典类型
     */
    @PostMapping("/update")
    public ResponseEntity<Void> update(@RequestBody DictType dictType) {
        dictTypeService.update(dictType);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除字典类型（同时删除该类型下所有字典值）
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dictTypeService.delete(id);
        return ResponseEntity.ok().build();
    }
}
