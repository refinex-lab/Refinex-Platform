package cn.refinex.api.user.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录主体查询条件
 *
 * @author refinex
 */
@Data
public class UserAuthSubjectQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 身份类型
     */
    private Integer identityType;

    /**
     * 标识符
     */
    private String identifier;

    /**
     * 企业ID
     */
    private Long estabId;
}
