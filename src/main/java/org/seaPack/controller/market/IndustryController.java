package org.seaPack.controller.market;

import lombok.extern.slf4j.Slf4j; // Lombok 日志
import org.seaPack.model.market.DimIndustry; // 行业实体（国家标准行业分类）
import org.seaPack.dto.market.IndustryStats; // 行业统计数据 DTO
import org.seaPack.service.market.IndustryTreeService; // 行业树服务
import org.springframework.beans.factory.annotation.Autowired; // Spring 依赖注入
import org.springframework.http.ResponseEntity; // HTTP 响应实体
import org.springframework.web.bind.annotation.*; // Spring Web MVC 注解

import java.util.List; // List 集合
import java.util.Optional; // Optional 容器

/**
 * 行业分类控制器
 * 提供国家标准行业分类（GB/T 4754）的树形结构查询、搜索和统计接口。
 */
@Slf4j // Lombok 日志
@RestController // 标识为 RESTful 控制器
@RequestMapping("/industry") // 请求基础路径
public class IndustryController {

    @Autowired // 注入行业树服务
    private IndustryTreeService industryTreeService;

    /**
     * 获取完整行业树
     */
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

    /**
     * 获取指定行业子树
     * @param industryCode 行业代码
     */
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

    /**
     * 搜索行业（按名称模糊匹配）
     * @param keyword 搜索关键字
     */
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

    /**
     * 获取行业统计数据
     */
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