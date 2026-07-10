package org.seaPack.controller.workflow;

import com.github.pagehelper.PageInfo;
import org.seaPack.model.workflow.FeedbackRecord;
import org.seaPack.service.workflow.FeedbackRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 反馈记录管理控制器
 * <p>提供反馈记录的分页查询、新增等接口。</p>
 */
@RestController
@RequestMapping("/workflows/feedbacks")
public class FeedbackRecordController {

    @Autowired
    private FeedbackRecordService feedbackRecordService;

    /** 分页查询反馈记录 */
    @GetMapping("/page/list")
    public PageInfo<FeedbackRecord> pageList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String feedbackType,
            @RequestParam(required = false) Long skillId) {
        return feedbackRecordService.getPageList(pageNum, pageSize, feedbackType, skillId);
    }

    /** 新增反馈 */
    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestBody FeedbackRecord record) {
        feedbackRecordService.insert(record);
        return ResponseEntity.ok(record);
    }

    /** 删除反馈 */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        feedbackRecordService.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }
}
