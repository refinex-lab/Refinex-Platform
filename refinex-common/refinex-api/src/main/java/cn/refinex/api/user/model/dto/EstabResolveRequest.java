package cn.refinex.api.user.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 组织解析请求
 *
 * @author refinex
 */
@Data
public class EstabResolveRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 企业代码
     */
    private String estabCode;
}
