package cn.refinex.file.api;

import java.io.InputStream;

/**
 * 文件存储服务接口
 * <p>
 * 定义统一的文件上传能力，屏蔽底层实现（OSS/S3/Local）。
 *
 * @author refinex
 */
public interface FileService {

    /**
     * 文件上传
     *
     * @param path        文件存储路径 (包含文件名)，例如: "avatar/user_123.jpg"
     * @param inputStream 文件内容流
     * @return 文件访问 URL (完整路径)
     * @throws RuntimeException 上传失败时抛出异常
     */
    String upload(String path, InputStream inputStream);
}
