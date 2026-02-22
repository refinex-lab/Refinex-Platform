package cn.refinex.ai.application.command;

import lombok.Data;

/**
 * 更新目录命令
 *
 * @author refinex
 */
@Data
public class UpdateFolderCommand {

    /**
     * 主键ID
     */
    private Long folderId;

    /**
     * 目录名称
     */
    private String folderName;

    /**
     * 扩展信息
     */
    private String extJson;
}
