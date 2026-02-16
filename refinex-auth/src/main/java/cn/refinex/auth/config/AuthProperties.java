package cn.refinex.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 认证服务配置
 *
 * @author refinex
 */
@Data
@ConfigurationProperties(prefix = "refinex.auth")
public class AuthProperties {

    /**
     * 短信验证码有效期（秒）
     */
    private int smsCodeExpireSeconds = 300;

    /**
     * 短信验证码发送间隔（秒）
     */
    private int smsCodeIntervalSeconds = 60;

    /**
     * 短信验证码长度
     */
    private int smsCodeLength = 6;

    /**
     * 登录失败阈值
     */
    private int loginFailThreshold = 5;

    /**
     * 锁定时长（分钟）
     */
    private int lockMinutes = 30;

    /**
     * Token 超时时间（秒）
     */
    private int tokenTimeoutSeconds = 7200;

    /**
     * Token 活跃超时（秒）
     */
    private int activeTimeoutSeconds = 1800;

    /**
     * 默认角色编码
     */
    private String defaultRoleCode = "TENANT_USER";

    /**
     * 默认系统编码
     */
    private String defaultSystemCode = "tenant";

    /**
     * 是否允许短信登录时自动注册
     */
    private boolean autoRegisterOnSmsLogin = false;

    /**
     * 注册时是否默认创建组织
     */
    private boolean defaultCreateEstab = false;

    /**
     * 短信发送限流：IP 维度
     */
    private int smsIpLimit = 20;

    /**
     * 短信发送限流：手机号维度
     */
    private int smsPhoneLimit = 10;

    /**
     * 短信发送限流：设备维度
     */
    private int smsDeviceLimit = 20;

    /**
     * 短信发送限流窗口（秒）
     */
    private int smsWindowSeconds = 3600;

    /**
     * 注册限流：IP 维度
     */
    private int registerIpLimit = 10;

    /**
     * 注册限流：标识维度（手机号/邮箱/用户名）
     */
    private int registerIdentifierLimit = 5;

    /**
     * 注册限流窗口（秒）
     */
    private int registerWindowSeconds = 3600;

    /**
     * 登录限流：IP 维度
     */
    private int loginIpLimit = 60;

    /**
     * 登录限流：标识维度
     */
    private int loginIdentifierLimit = 30;

    /**
     * 登录限流窗口（秒）
     */
    private int loginWindowSeconds = 60;
}
