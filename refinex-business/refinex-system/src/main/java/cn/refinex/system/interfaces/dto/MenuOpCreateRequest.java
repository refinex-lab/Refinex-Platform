package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建菜单操作请求
 *
 * @author refinex
 */
@Data
public class MenuOpCreateRequest {

    /**
     * 操作编码（可选，留空自动生成）
     */
    @Size(max = 64, message = "操作编码长度不能超过64个字符")
    private String opCode;

    /**
     * 操作名称
     */
    @NotBlank(message = "操作名称不能为空")
    @Size(max = 64, message = "操作名称长度不能超过64个字符")
    private String opName;

    /**
     * HTTP方法
     */
    @Size(max = 16, message = "HTTP方法长度不能超过16个字符")
    private String httpMethod;

    /**
     * 接口路径模式
     */
    @Size(max = 255, message = "路径模式长度不能超过255个字符")
    private String pathPattern;

    /**
     * 权限标识
     */
    @Size(max = 128, message = "权限标识长度不能超过128个字符")
    private String permissionKey;

    /**
     * 状态 1启用 2停用
     */
    @Min(value = 1, message = "状态取值非法")
    @Max(value = 2, message = "状态取值非法")
    private Integer status;

    /**
     * 排序
     */
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;
}
