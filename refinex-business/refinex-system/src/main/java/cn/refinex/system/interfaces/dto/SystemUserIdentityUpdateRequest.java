package cn.refinex.system.interfaces.dto;

import lombok.Data;

/**
 * 系统用户身份更新请求
 *
 * @author refinex
 */
@Data
public class SystemUserIdentityUpdateRequest {

    /**
     * 身份标识
     */
    private String identifier;

    /**
     * 发行方
     */
    private String issuer;

    /**
     * 凭证（加密后）
     */
    private String credential;

    /**
     * 凭证算法
     */
    private String credentialAlg;

    /**
     * 是否主身份 1是 0否
     */
    private Integer isPrimary;

    /**
     * 是否已验证 1是 0否
     */
    private Integer verified;

    /**
     * 状态 1启用 0停用
     */
    private Integer status;
}
