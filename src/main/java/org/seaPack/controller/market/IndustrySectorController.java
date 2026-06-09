package org.seaPack.controller.market;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.model.market.IndustrySector;
import org.seaPack.service.market.IndustrySectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/industrySector")
public class IndustrySectorController {

    @Autowired
    private IndustrySectorService industrySectorService;

    @GetMapping("/list")
    public ResponseEntity<PageInfo<IndustrySector>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer nodeLevel,
            @RequestParam(required = false) Long parentId) {
        return ResponseEntity.ok(industrySectorService.getList(pageNum, pageSize, keyword, nodeLevel, parentId));
    }

    @GetMapping("/tree")
    public ResponseEntity<List<IndustrySector>> tree() {
        return ResponseEntity.ok(industrySectorService.getTree());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IndustrySector> detail(@PathVariable Long id) {
        return ResponseEntity.ok(industrySectorService.getById(id));
    }

    @GetMapping("/children/{parentId}")
    public ResponseEntity<List<IndustrySector>> children(@PathVariable Long parentId) {
        return ResponseEntity.ok(industrySectorService.getChildren(parentId));
    }

    @PostMapping("/insert")
    public ResponseEntity<Integer> insert(@RequestBody IndustrySector sector) {
        return ResponseEntity.ok(industrySectorService.insert(sector));
    }

    @PostMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody IndustrySector sector) {
        return ResponseEntity.ok(industrySectorService.update(sector));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(industrySectorService.delete(id));
    }
}