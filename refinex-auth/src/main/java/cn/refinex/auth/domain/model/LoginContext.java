package cn.refinex.auth.domain.model;

import cn.refinex.auth.domain.enums.LoginSource;
import lombok.Data;

/**
 * 登录上下文
 *
 * @author refinex
 */
@Data
public class LoginContext {

    /**
     * 登录IP
     */
    private String ip;

    /**
     * 登录设备信息
     */
    private String userAgent;

    /**
     * 登录来源
     */
    private LoginSource source;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 设备ID
     */
    private String deviceId;
}
