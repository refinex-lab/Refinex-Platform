package cn.refinex.user.application.command;

import lombok.Data;

/**
 * 上传用户头像命令
 *
 * @author refinex
 */
@Data
public class UploadUserAvatarCommand {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 文件 MIME 类型
     */
    private String contentType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;
}
