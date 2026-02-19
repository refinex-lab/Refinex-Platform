package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 更新系统命令
 *
 * @author refinex
 */
@Data
public class UpdateSystemCommand {

    /**
     * 系统ID
     */
    private Long systemId;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 系统类型 0平台 1租户 2业务子系统
     */
    private Integer systemType;

    /**
     * 系统基础URL
     */
    private String baseUrl;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;
}
