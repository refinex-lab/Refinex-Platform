package cn.refinex.user.application.dto;

import lombok.Data;

/**
 * 注册结果 DTO
 *
 * @author refinex
 */
@Data
public class RegisterUserResultDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 组织ID
     */
    private Long estabId;
}
