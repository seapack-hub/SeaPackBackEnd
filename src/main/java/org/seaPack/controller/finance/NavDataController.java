package org.seaPack.controller.finance;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.finance.NavData;
import org.seaPack.model.finance.NavDataExample;
import org.seaPack.service.finance.NavDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基金净值数据控制器
 * <p>提供净值数据的分页查询、详情、新增、修改、删除等接口。</p>
 */
@Slf4j
@RestController
@RequestMapping("/navData")
public class NavDataController {

    @Autowired
    private NavDataService navDataService;

    /**
     * 分页查询净值列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param fundCode 基金代码（可选）
     */
    @PostMapping("/page")
    public ResponseEntity<PageInfo<NavData>> getNavDataList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String fundCode) {
        NavDataExample example = new NavDataExample();
        if (fundCode != null && !fundCode.isEmpty()) {
            example.createCriteria().andFundCodeEqualTo(fundCode);
        }
        example.setOrderByClause("nav_date DESC");
        PageInfo<NavData> pageInfo = navDataService.getNavDataList(pageNum, pageSize, example);
        return ResponseEntity.ok(pageInfo);
    }

    /**
     * 根据基金代码查询净值列表
     * @param fundCode 基金代码
     */
    @GetMapping("/list/{fundCode}")
    public ResponseEntity<List<NavData>> getNavDataByFundCode(@PathVariable String fundCode) {
        return ResponseEntity.ok(navDataService.getNavDataByFundCode(fundCode));
    }

    /**
     * 查询净值详情
     * @param id 主键ID
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<NavData> getNavDataDetail(@PathVariable Integer id) {
        NavData navData = navDataService.getNavDataById(id);
        if (navData == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(navData);
    }

    /**
     * 新增净值记录
     * @param navData 净值数据
     */
    @PostMapping("/insert")
    public ResponseEntity<Integer> insertNavData(@RequestBody NavData navData) {
        return ResponseEntity.ok(navDataService.insertNavData(navData));
    }

    /**
     * 更新净值记录
     * @param navData 净值数据
     */
    @PostMapping("/update")
    public ResponseEntity<Integer> updateNavData(@RequestBody NavData navData) {
        if (navData.getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(navDataService.updateNavData(navData));
    }

    /**
     * 删除净值记录
     * @param id 主键ID
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Integer> deleteNavData(@PathVariable Integer id) {
        return ResponseEntity.ok(navDataService.deleteNavData(id));
    }
}
