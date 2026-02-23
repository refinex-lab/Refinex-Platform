package cn.refinex.ai.interfaces.controller;

import cn.refinex.ai.infrastructure.config.ReactiveLoginUserHolder;
import cn.refinex.file.api.FileService;
import cn.refinex.web.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

/**
 * 文件上传接口
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 上传文件
     *
     * @param filePart 文件
     * @param category 文件分类（默认 chat）
     * @param exchange ServerWebExchange
     * @return 文件访问 URL
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Result<String>> uploadFile(@RequestPart("file") FilePart filePart,
                                           @RequestPart(value = "category", required = false) String category,
                                           ServerWebExchange exchange) {
        String resolvedCategory = (category == null || category.isBlank()) ? "chat" : category;
        String originalFilename = filePart.filename();
        String ext = extractExtension(originalFilename);

        return DataBufferUtils.join(filePart.content())
                .flatMap(dataBuffer -> Mono.fromCallable(() -> {
                    ReactiveLoginUserHolder.initFromExchange(exchange);
                    try {
                        Long estabId = ReactiveLoginUserHolder.getEstabId();
                        String path = String.join("/", "ai-" + resolvedCategory, String.valueOf(estabId), UUID.randomUUID() + ext);
                        String url = fileService.upload(path, dataBuffer.asInputStream(true));
                        return Result.success(url);
                    } finally {
                        ReactiveLoginUserHolder.clear();
                    }
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    /**
     * 提取文件扩展名（含点号）
     *
     * @param filename 原始文件名
     * @return 扩展名（如 ".jpg"），无扩展名时返回空字符串
     */
    private static String extractExtension(String filename) {
        int dotIdx = filename.lastIndexOf('.');
        return dotIdx >= 0 ? filename.substring(dotIdx) : "";
    }
}
