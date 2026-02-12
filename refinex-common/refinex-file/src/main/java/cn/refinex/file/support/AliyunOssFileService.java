package cn.refinex.file.support;

import cn.refinex.base.exception.SystemException;
import cn.refinex.base.exception.code.BizErrorCode;
import cn.refinex.file.api.FileService;
import cn.refinex.file.autoconfigure.FileProperties;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.InputStream;

/**
 * 阿里云 OSS 文件服务实现
 *
 * @author refinex
 */
@Slf4j
@RequiredArgsConstructor
public class AliyunOssFileService implements FileService {

    private final OSS ossClient;
    private final FileProperties properties;

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
        String bucket = properties.getBucket();

        try {
            // 创建 PutObject 请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, path, inputStream);

            // 执行上传
            ossClient.putObject(putObjectRequest);

            // 构建返回 URL
            return getFileUrl(path);
        } catch (Exception e) {
            log.error("OSS Upload failed. bucket={}, path={}", bucket, path, e);
            throw new SystemException("File upload failed", e, BizErrorCode.HTTP_SERVER_ERROR);
        }
    }

    /**
     * 拼接文件访问 URL
     *
     * @param path 文件存储路径 (包含文件名)，例如: "avatar/user_123.jpg"
     * @return 文件访问 URL
     */
    private String getFileUrl(String path) {
        // 如果配置了 CDN 域名，优先使用
        if (StringUtils.hasText(properties.getDomain())) {
            String domain = properties.getDomain();
            return domain.endsWith("/") ? domain + path : domain + "/" + path;
        }

        // 默认格式: https://bucket.endpoint/path
        // 注意处理 endpoint 中的 http/https 前缀
        String endpoint = properties.getEndpoint();
        String protocol = "https://";
        if (endpoint.startsWith("http://")) {
            protocol = "http://";
            endpoint = endpoint.replace("http://", "");
        } else if (endpoint.startsWith("https://")) {
            endpoint = endpoint.replace("https://", "");
        }

        return protocol + properties.getBucket() + "." + endpoint + "/" + path;
    }
}
