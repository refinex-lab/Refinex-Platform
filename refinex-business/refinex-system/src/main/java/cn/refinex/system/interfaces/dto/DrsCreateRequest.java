package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 创建数据资源请求
 *
 * @author refinex
 */
@Data
public class DrsCreateRequest {

    /**
     * 系统ID
     */
    @NotNull(message = "系统ID不能为空")
    @Positive(message = "系统ID必须大于0")
    private Long systemId;

    /**
     * 数据资源编码
     */
    @NotBlank(message = "数据资源编码不能为空")
    @Size(max = 64, message = "数据资源编码长度不能超过64个字符")
    private String drsCode;

    /**
     * 数据资源名称
     */
    @NotBlank(message = "数据资源名称不能为空")
    @Size(max = 128, message = "数据资源名称长度不能超过128个字符")
    private String drsName;

    /**
     * 资源类型 0数据库表 1接口资源 2文件 3其他
     */
    @Min(value = 0, message = "资源类型取值非法")
    @Max(value = 3, message = "资源类型取值非法")
    private Integer drsType;

    /**
     * 资源标识(表名/路径/URI)
     */
    @Size(max = 255, message = "资源标识长度不能超过255个字符")
    private String resourceUri;

    /**
     * 所属组织ID(平台级为0)
     */
    @PositiveOrZero(message = "所属组织ID不能小于0")
    private Long ownerEstabId;

    /**
     * 数据归属 0平台 1租户 2用户
     */
    @Min(value = 0, message = "数据归属取值非法")
    @Max(value = 2, message = "数据归属取值非法")
    private Integer dataOwnerType;

    /**
     * 状态 1启用 2停用
     */
    @Min(value = 1, message = "状态取值非法")
    @Max(value = 2, message = "状态取值非法")
    private Integer status;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
