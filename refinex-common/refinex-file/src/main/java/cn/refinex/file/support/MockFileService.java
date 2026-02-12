package cn.refinex.file.support;

import cn.refinex.file.api.FileService;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * Mock 文件服务 (开发环境/降级兜底)
 * <p>
 * 不进行实际上传，仅打印日志并返回模拟 URL。
 *
 * @author refinex
 */
@Slf4j
public class MockFileService implements FileService {

    /**
     * 文件上传
     *
     * @param path        文件存储路径 (包含文件名)，例如: "avatar/user_123.jpg"
     * @param inputStream 文件内容流
     * @return 文件访问 URL (完整路径)
     * @throws RuntimeException 上传失败时抛出异常
     */
    @Override
    public String upload(String path, InputStream inputStream) {
        log.warn("MockFileService is active. File NOT uploaded to cloud. Path: {}", path);
        // 返回一个假的本地 URL 方便前端调试
        return "http://mock-file-service.local/" + path;
    }
}
