package org.seaPack.tools;

import dev.langchain4j.agent.tool.Tool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.seaPack.dto.AgentContext;
import org.seaPack.service.ProgressService;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class WebScraperUtilTool {

    private final ProgressService progressService;

    public WebScraperUtilTool(ProgressService progressService) {
        this.progressService = progressService;
    }

    /**
     * 抓取指定网页的标题和正文内容
     */
    @Tool("根据提供的URL搜集网页资料，提取标题和核心内容。参数 url 必须是完整的网页链接。")
    public String fetchPageContent(String url) throws IOException {
        // 1. 汇报进度：开始搜索
        AgentContext context = progressService.getContext();

        if (context != null) {
            context.sendProgress("search","开始搜索信息... " + url);
            context.sendProgress("content","🔍 正在抓取网页: " + url);
        }

        // 模拟浏览器请求头，防止被目标网站识别为爬虫而拦截
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8") // 1. 添加 Accept
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8") // 2. 添加语言
                .header("Accept-Encoding", "gzip, deflate") // 3. 添加编码
                .header("Connection", "keep-alive") // 4. 添加连接方式
                .timeout(10000) // 增加超时时间
                .get();


        // 获取网页标题
        String title = doc.title();
        // 获取网页纯文本内容（自动去除了HTML标签）
        String text = doc.body().text();

        // 2. 汇报进度：搜索完成
        if (context != null) {
            context.sendProgress("content", "✅ 抓取成功: " + title);
        }

        return "标题：" + title + "\n内容：" + text.substring(0, Math.min(1000, text.length()));
    }
}
