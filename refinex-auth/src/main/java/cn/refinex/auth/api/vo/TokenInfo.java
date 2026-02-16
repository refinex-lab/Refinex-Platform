package cn.refinex.auth.api.vo;

import lombok.Data;

/**
 * Token 信息
 *
 * @author refinex
 */
@Data
public class TokenInfo {

    /**
     * Token 名称
     */
    private String tokenName;

    /**
     * Token 值
     */
    private String tokenValue;

    /**
     * Token 剩余有效期（秒）
     */
    private long tokenTimeout;

    /**
     * Token 剩余活跃有效期（秒）
     */
    private long activeTimeout;

    /**
     * 登录ID
     */
    private Object loginId;
}
