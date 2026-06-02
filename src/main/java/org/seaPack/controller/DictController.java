package org.seaPack.controller;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.Dict;
import org.seaPack.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/dict")
public class DictController {

    @Autowired
    private DictService dictService;

    /**
     * 分页查询字典列表（支持按类型/关键字/状态筛选）
     * @param pageNum 页码（默认1）
     * @param pageSize 每页条数（默认10）
     * @param dictType 字典类型（精确匹配，可选）
     * @param keyword 关键字（模糊匹配dict_code或dict_name，可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    @GetMapping("/list")
    public ResponseEntity<PageInfo<Dict>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String dictType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(dictService.getList(pageNum, pageSize, dictType, keyword, status));
    }

    /**
     * 根据主键ID查询字典详情
     * @param id 主键ID
     * @return 字典信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Dict> detail(@PathVariable Long id) {
        return ResponseEntity.ok(dictService.getById(id));
    }

    /**
     * 新增字典（同一类型下编码需唯一）
     * @param dict 字典信息（id自增无需传入）
     * @return 影响行数
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody Dict dict) {
        return ResponseEntity.ok(dictService.insert(dict));
    }

    /**
     * 更新字典
     * @param dict 待更新数据（必须含id）
     * @return 影响行数
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody Dict dict) {
        return ResponseEntity.ok(dictService.update(dict));
    }

    /**
     * 逻辑删除字典
     * @param id 主键ID
     * @return 影响行数
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(dictService.delete(id));
    }
}
