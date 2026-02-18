package cn.refinex.auth.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 切换当前企业请求
 *
 * @author refinex
 */
@Data
public class SwitchEstabRequest {

    /**
     * 目标企业ID
     */
    @NotNull(message = "目标企业不能为空")
    private Long estabId;
}
