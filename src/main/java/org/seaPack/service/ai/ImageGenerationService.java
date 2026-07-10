package org.seaPack.service.ai;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.seaPack.config.AIProperties;
import org.seaPack.dto.ai.ImageGenRequest;
import org.seaPack.dto.ai.ImageGenResponse;
import org.seaPack.dto.ai.ImageResult;
import org.seaPack.dto.ai.ImageTaskVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * AI 图片生成服务
 * <p>
 * 调用 OpenAI DALL-E 3 兼容的图片生成 API，将生成的图片下载到本地存储，
 * 并提供同步和异步两种调用模式。异步模式下通过 taskId 轮询任务状态。
 * </p>
 *
 * <h3>工作流程</h3>
 * <ol>
 *   <li>接收前端请求参数（prompt, n, size, style）</li>
 *   <li>调用 AI 图片生成 API，获取临时图片 URL</li>
 *   <li>从临时 URL 下载图片到本地 uploads/images 目录</li>
 *   <li>返回本地可访问的图片 URL</li>
 * </ol>
 */
@Slf4j
@Service
public class ImageGenerationService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AIProperties aiProperties;

    @Value("${ai.image-generation.api-url:https://api.openai.com/v1/images/generations}")
    private String apiUrl;

    @Value("${ai.image-generation.model:dall-e-3}")
    private String model;

    @Value("${ai.image-generation.storage-dir:uploads/images}")
    private String storageDir;

    @Value("${server.address:localhost}")
    private String serverHost;

    @Value("${server.port:8090}")
    private int serverPort;

    @Value("${ai.image-generation.base-url:}")
    private String baseUrl;

    /** 异步任务存储 map（key=taskId, value=任务状态） */
    private final ConcurrentHashMap<String, ImageTaskVO> taskStore = new ConcurrentHashMap<>();

    /** 异步任务线程池 */
    private ExecutorService executor;

    /** 线程计数器 */
    private static final AtomicInteger imageGenThreadCounter = new AtomicInteger(0);

    /** 图片存储的绝对路径 */
    private Path storagePath;

    @PostConstruct
    public void init() {
        storagePath = Paths.get(storageDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storagePath);
            log.info("图片存储目录已创建: {}", storagePath);
        } catch (IOException e) {
            log.error("创建图片存储目录失败: {}", storagePath, e);
        }
        executor = new ThreadPoolExecutor(
                2, 8, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                r -> {
                    Thread t = new Thread(r, "img-gen-" + imageGenThreadCounter.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @PreDestroy
    public void destroy() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    /**
     * 同步模式：调用 AI 图片生成 API 并等待结果
     *
     * @param request 图片生成请求
     * @return 包含本地可访问 URL 的响应
     */
    public ImageGenResponse generateSync(ImageGenRequest request) {
        // 1. 调用 AI API 获取临时图片 URL
        List<String> remoteUrls = callImageApi(request);

        // 2. 下载每张图片到本地，构建响应
        List<ImageResult> images = remoteUrls.parallelStream()
                .map(this::downloadAndSave)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ImageGenResponse.builder().images(images).build();
    }

    /**
     * 异步模式：创建后台任务，立即返回 taskId
     *
     * @param request 图片生成请求
     * @return 任务状态（processing）
     */
    public ImageTaskVO generateAsync(ImageGenRequest request) {
        String taskId = UUID.randomUUID().toString().replace("-", "");

        ImageTaskVO task = ImageTaskVO.builder()
                .taskId(taskId)
                .status("processing")
                .build();
        taskStore.put(taskId, task);

        // 后台异步执行图片生成
        CompletableFuture.runAsync(() -> {
            try {
                ImageGenResponse response = generateSync(request);
                ImageTaskVO updated = ImageTaskVO.builder()
                        .taskId(taskId)
                        .status("completed")
                        .images(response.getImages())
                        .build();
                taskStore.put(taskId, updated);
                log.info("异步图片任务 {} 完成，生成 {} 张图片", taskId,
                        response.getImages() != null ? response.getImages().size() : 0);
            } catch (Exception e) {
                log.error("异步图片任务 {} 失败", taskId, e);
                ImageTaskVO failed = ImageTaskVO.builder()
                        .taskId(taskId)
                        .status("failed")
                        .error(e.getMessage())
                        .build();
                taskStore.put(taskId, failed);
            }
        }, executor);

        return task;
    }

    /**
     * 查询异步任务状态
     *
     * @param taskId 任务 ID
     * @return 任务状态，不存在时返回 null
     */
    public ImageTaskVO getTaskStatus(String taskId) {
        return taskStore.get(taskId);
    }

    /**
     * 调用 AI 图片生成 API（OpenAI DALL-E 3 兼容格式）
     *
     * @param request 生成参数
     * @return AI API 返回的临时图片 URL 列表
     */
    @SuppressWarnings("unchecked")
    private List<String> callImageApi(ImageGenRequest request) {
        // 构造请求体 —— 兼容 OpenAI DALL-E 3 格式
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("prompt", request.getPrompt());
        body.put("n", request.getN() != null ? request.getN() : 1);
        body.put("size", request.getSize() != null ? request.getSize() : "1024x1024");
        body.put("style", request.getStyle() != null ? request.getStyle() : "vivid");
        body.put("response_format", "url");

        // 构造请求头 —— 使用图片生成专用的 API Key
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String imageApiKey = System.getenv("IMAGE_GEN_API_KEY");
        if (imageApiKey == null || imageApiKey.isEmpty()) {
            // 回退到 deepseek 配置
            AIProperties.ProviderConfig deepseekConfig = aiProperties.getProviders().get("deepseek");
            imageApiKey = (deepseekConfig != null) ? deepseekConfig.getApiKey() : "";
        }
        if (!imageApiKey.isEmpty()) {
            headers.setBearerAuth(imageApiKey);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        log.debug("调用图片生成 API: {} | prompt={}", apiUrl, request.getPrompt());

        // 发起 HTTP 请求
        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl, HttpMethod.POST, entity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("图片生成 API 调用失败，状态码: " + response.getStatusCode());
        }

        // 解析响应 —— OpenAI 格式: { data: [ { url: "...", revised_prompt: "..." } ] }
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.getBody().get("data");
        if (dataList == null || dataList.isEmpty()) {
            throw new RuntimeException("图片生成 API 返回数据为空");
        }

        return dataList.stream()
                .map(d -> (String) d.get("url"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 从远程 URL 下载图片，保存到本地存储目录
     *
     * @param remoteUrl AI API 返回的临时图片 URL
     * @return 本地可访问的图片信息（URL、宽、高）
     */
    private ImageResult downloadAndSave(String remoteUrl) {
        if (remoteUrl == null || remoteUrl.isEmpty()) {
            return null;
        }

        // 生成唯一文件名
        String ext = "png";
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path targetPath = storagePath.resolve(filename);

        try {
            // 下载图片到本地文件
            byte[] imageBytes = restTemplate.getForObject(URI.create(remoteUrl), byte[].class);
            if (imageBytes == null || imageBytes.length == 0) {
                log.warn("下载图片内容为空: {}", remoteUrl);
                return null;
            }
            Files.write(targetPath, imageBytes);

            // 读取图片实际尺寸
            int width = 0, height = 0;
            try (InputStream is = new ByteArrayInputStream(imageBytes)) {
                BufferedImage img = ImageIO.read(is);
                if (img != null) {
                    width = img.getWidth();
                    height = img.getHeight();
                }
            }

            // 构造本地可访问的 URL
            String localUrl = buildImageUrl(filename);

            log.debug("图片已保存: {} ({}x{})", localUrl, width, height);
            return ImageResult.builder()
                    .url(localUrl)
                    .width(width)
                    .height(height)
                    .build();

        } catch (Exception e) {
            log.error("下载/保存图片失败: {}", remoteUrl, e);
            return null;
        }
    }

    /**
     * 构建图片的可访问 URL
     * <p>格式: http://host:port/images/filename.png</p>
     */
    private String buildImageUrl(String filename) {
        String host = (baseUrl != null && !baseUrl.isEmpty()) ? baseUrl :
                "http://" + serverHost + ":" + serverPort;
        return host + "/images/" + filename;
    }
}
