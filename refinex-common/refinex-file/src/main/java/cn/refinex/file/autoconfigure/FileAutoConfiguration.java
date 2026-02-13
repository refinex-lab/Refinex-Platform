package cn.refinex.file.autoconfigure;

import cn.refinex.file.api.FileService;
import cn.refinex.file.support.AliyunOssFileService;
import cn.refinex.file.support.MockFileService;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 文件服务自动配置
 *
 * @author refinex
 */
@AutoConfiguration
@EnableConfigurationProperties(FileOSSProperties.class)
public class FileAutoConfiguration {

    /**
     * 1. 当 refinex.file.oss.enable=true 时，初始化 OSS Client
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(prefix = "refinex.file.oss", name = "enable", havingValue = "true")
    public OSS ossClient(FileOSSProperties properties) {
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(
                properties.getAccessKey(),
                properties.getAccessSecret()
        );

        return new OSSClientBuilder().build(
                properties.getEndpoint(),
                credentialsProvider
        );
    }

    /**
     * 2. 当 refinex.file.oss.enable=true 时，初始化 AliyunOssFileService
     */
    @Bean
    @ConditionalOnProperty(prefix = "refinex.file.oss", name = "enable", havingValue = "true")
    public FileService ossFileService(OSS ossClient, FileOSSProperties properties) {
        return new AliyunOssFileService(ossClient, properties);
    }

    /**
     * 3. 兜底策略：如果没有配置 OSS，初始化 MockFileService
     */
    @Bean
    @ConditionalOnMissingBean(FileService.class)
    public FileService mockFileService() {
        return new MockFileService();
    }
}
