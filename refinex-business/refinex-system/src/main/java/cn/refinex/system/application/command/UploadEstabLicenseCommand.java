package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 上传企业营业执照命令
 *
 * @author refinex
 */
@Data
public class UploadEstabLicenseCommand {

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 文件类型
     */
    private String contentType;

    /**
     * 文件大小
     */
    private Long fileSize;
}
