package org.seaPack.controller;

import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.Result;
import org.seaPack.model.DimIndustry;
import org.seaPack.dto.IndustryStats;
import org.seaPack.service.IndustryTreeService;
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

    /**
     * 获取完整的行业树形结构
     * GET /industries/tree
     */
    @GetMapping("/tree")
    public ResponseEntity<Result<List<DimIndustry>>> getIndustryTree(){
        try {
            List<DimIndustry> industryTree = industryTreeService.getIndustryTree();
            log.info("成功获取行业树，包含 {} 个根节点", industryTree.size());
            return ResponseEntity.ok(Result.success(industryTree));
        }catch (Exception e){
            log.error("获取行业树失败", e);
            return ResponseEntity.internalServerError()
                    .body(Result.error("获取行业树失败: " + e.getMessage()));
        }
    }

    /**
     * 根据行业代码获取子树
     * GET /industries/tree/{industryCode}
     */
    @GetMapping("/tree/{industryCode}")
    public ResponseEntity<Result<Optional<DimIndustry>>> getSubTree(@PathVariable String industryCode){
        try{
            Optional<DimIndustry> subTree = industryTreeService.getSubTree(industryCode);
            if(subTree.isPresent()){
                return ResponseEntity.ok(Result.success(subTree));
            }else {
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            log.error("获取子树失败: {}", industryCode, e);
            return ResponseEntity.internalServerError()
                    .body(Result.error("获取子树失败: " + e.getMessage()));
        }
    }

    /**
     * 搜索行业
     * GET /industries/search?keyword=?
     */
    @GetMapping("/search")
    public ResponseEntity<Result<List<DimIndustry>>> searchIndustry(@RequestParam String keyword){
        try{
            List<DimIndustry> results = industryTreeService.searchIndustries(keyword);
            return ResponseEntity.ok(Result.success(results));
        }catch (Exception e){
            log.error("搜索行业失败: keyword={}", keyword, e);
            return ResponseEntity.internalServerError()
                    .body(Result.error("搜索失败: " + e.getMessage()));
        }
    }

    /**
     * 获取行业统计信息
     * GET /industries/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Result<IndustryStats>> getIndustryStats() {
        try {
            IndustryStats stats = industryTreeService.getIndustryStats();
            return ResponseEntity.ok(Result.success(stats));
        } catch (Exception e) {
            log.error("获取行业统计失败", e);
            return ResponseEntity.internalServerError()
                    .body(Result.error("获取统计失败: " + e.getMessage()));
        }
    }
}
