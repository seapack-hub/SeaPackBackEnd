package org.seaPack.controller.system;

import com.github.pagehelper.PageInfo; // MyBatis 分页信息
import lombok.extern.slf4j.Slf4j; // Lombok 日志
import org.seaPack.model.system.Dict; // 字典实体
import org.seaPack.service.system.DictService; // 字典服务
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.http.ResponseEntity; // HTTP 响应实体
import org.springframework.web.bind.annotation.*; // Spring Web MVC 注解

/**
 * 数据字典控制器
 * 提供系统字典的分页查询、详情、新增、修改和删除接口。
 */
@Slf4j // Lombok 日志
@RestController // 标识为 RESTful 控制器
@RequestMapping("/dict") // 请求基础路径
public class DictController {

    @Autowired // 注入字典服务
    private DictService dictService;

    /**
     * 分页查询字典列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param dictType 字典类型
     * @param keyword 关键字
     * @param status 状态
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
     * 查询字典详情
     * @param id 字典 ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Dict> detail(@PathVariable Long id) {
        return ResponseEntity.ok(dictService.getById(id));
    }

    /**
     * 新增字典项
     * @param dict 字典实体
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody Dict dict) {
        return ResponseEntity.ok(dictService.insert(dict));
    }

    /**
     * 修改字典项
     * @param dict 字典实体（需含 id）
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody Dict dict) {
        return ResponseEntity.ok(dictService.update(dict));
    }

    /**
     * 删除字典项
     * @param id 字典 ID
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(dictService.delete(id));
    }
}