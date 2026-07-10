package org.seaPack.controller.workflow;

import com.github.pagehelper.PageInfo;
import org.seaPack.model.workflow.AnnotationRecord;
import org.seaPack.service.workflow.AnnotationRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 标注记录管理控制器
 * <p>提供标注记录的分页查询、新增等接口。</p>
 */
@RestController
@RequestMapping("/workflows/annotations")
public class AnnotationRecordController {

    @Autowired
    private AnnotationRecordService annotationRecordService;

    /** 分页查询标注记录 */
    @GetMapping("/page/list")
    public PageInfo<AnnotationRecord> pageList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Long instanceId,
            @RequestParam(required = false) String contentType) {
        return annotationRecordService.getPageList(pageNum, pageSize, taskId, instanceId, contentType);
    }

    /** 新增标注 */
    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestBody AnnotationRecord record) {
        annotationRecordService.insert(record);
        return ResponseEntity.ok(record);
    }

    /** 删除标注 */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        annotationRecordService.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }
}
