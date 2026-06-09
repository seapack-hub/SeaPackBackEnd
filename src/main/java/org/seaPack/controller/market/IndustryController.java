package org.seaPack.controller.market;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.Result;
import org.seaPack.model.market.DimIndustry;
import org.seaPack.dto.market.IndustryStats;
import org.seaPack.service.market.IndustryTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/industry")
public class IndustryController {

    @Autowired
    private IndustryTreeService industryTreeService;

    @GetMapping("/tree")
    public ResponseEntity<List<DimIndustry>> getIndustryTree(){
        try {
            List<DimIndustry> industryTree = industryTreeService.getIndustryTree();
            log.info("成功获取行业树，包含 {} 个根节点", industryTree.size());
            return ResponseEntity.ok(industryTree);
        }catch (Exception e){
            log.error("获取行业树失败", e);
            return ResponseEntity.internalServerError()
                    .body(null);
        }
    }

    @GetMapping("/tree/{industryCode}")
    public ResponseEntity<Optional<DimIndustry>> getSubTree(@PathVariable String industryCode){
        try{
            Optional<DimIndustry> subTree = industryTreeService.getSubTree(industryCode);
            if(subTree.isPresent()){
                return ResponseEntity.ok(subTree);
            }else {
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error("获取子树失败: {}", industryCode, e);
            return ResponseEntity.internalServerError()
                    .body(Optional.empty());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<DimIndustry>> searchIndustry(@RequestParam(required = false) String keyword){
        try{
            List<DimIndustry> results = industryTreeService.searchIndustries(keyword);
            return ResponseEntity.ok(results);
        }catch (Exception e){
            log.error("搜索行业失败: keyword={}", keyword, e);
            return ResponseEntity.internalServerError()
                    .body(null);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<IndustryStats> getIndustryStats() {
        try {
            IndustryStats stats = industryTreeService.getIndustryStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取行业统计失败", e);
            return ResponseEntity.internalServerError()
                    .body(null);
        }
    }
}