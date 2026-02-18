package cn.refinex.api.user.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册结果
 *
 * @author refinex
 */
@Data
public class UserRegisterResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 企业ID
     */
    private Long estabId;
}
