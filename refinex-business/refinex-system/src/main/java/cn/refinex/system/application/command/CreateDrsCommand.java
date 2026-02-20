package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 创建数据资源命令
 *
 * @author refinex
 */
@Data
public class CreateDrsCommand {

    /**
     * 数据资源编码
     */
    private String drsCode;

    /**
     * 数据资源名称
     */
    private String drsName;

    /**
     * 所属组织ID(平台级为0)
     */
    private Long ownerEstabId;

    /**
     * 数据归属 0平台 1租户
     */
    private Integer dataOwnerType;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
