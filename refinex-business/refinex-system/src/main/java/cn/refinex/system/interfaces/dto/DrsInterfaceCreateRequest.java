package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建数据资源接口请求
 *
 * @author refinex
 */
@Data
public class DrsInterfaceCreateRequest {

    /**
     * 接口编码（可选，留空自动生成）
     */
    @Size(max = 64, message = "接口编码长度不能超过64个字符")
    private String interfaceCode;

    /**
     * 接口名称
     */
    @NotBlank(message = "接口名称不能为空")
    @Size(max = 128, message = "接口名称长度不能超过128个字符")
    private String interfaceName;

    /**
     * 数据资源SQL
     */
    @Size(max = 5000, message = "数据资源SQL长度不能超过5000个字符")
    private String interfaceSql;

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
