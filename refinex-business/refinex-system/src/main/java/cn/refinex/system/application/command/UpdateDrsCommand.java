package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 更新数据资源命令
 *
 * @author refinex
 */
@Data
public class UpdateDrsCommand {

    /**
     * 数据资源ID
     */
    private Long drsId;

    /**
     * 数据资源名称
     */
    private String drsName;

    /**
     * 资源类型 0数据库表 1接口资源 2文件 3其他
     */
    private Integer drsType;

    /**
     * 资源标识(表名/路径/URI)
     */
    private String resourceUri;

    /**
     * 所属组织ID(平台级为0)
     */
    private Long ownerEstabId;

    /**
     * 数据归属 0平台 1租户 2用户
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
