package cn.refinex.auth.domain.error;

import cn.refinex.base.exception.code.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 认证服务错误码
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_PARAM("AUTH_400", "参数错误"),
    USER_NOT_FOUND("AUTH_404", "用户不存在"),
    IDENTITY_NOT_FOUND("AUTH_404_ID", "登录身份不存在"),
    PASSWORD_ERROR("AUTH_401", "账号或密码错误"),
    CODE_ERROR("AUTH_401_CODE", "验证码错误或已过期"),
    USER_DISABLED("AUTH_403", "用户已被停用或锁定"),
    USER_LOCKED("AUTH_423", "账号已锁定，请稍后再试"),
    IDENTITY_DISABLED("AUTH_403_ID", "登录身份不可用"),
    DUPLICATE_IDENTITY("AUTH_409", "账号已存在"),
    CODE_SEND_TOO_FREQUENT("AUTH_429_CODE", "验证码发送过于频繁"),
    SMS_SEND_TOO_FREQUENT("AUTH_429_SMS", "验证码发送过于频繁"),
    REGISTER_TOO_FREQUENT("AUTH_429_REG", "注册过于频繁"),
    LOGIN_TOO_FREQUENT("AUTH_429_LOGIN", "登录请求过于频繁"),
    LOGIN_TYPE_NOT_SUPPORTED("AUTH_422", "登录方式暂不支持"),
    REGISTER_TYPE_NOT_SUPPORTED("AUTH_422_REG", "注册方式暂不支持"),
    ESTAB_NOT_FOUND("AUTH_404_ESTAB", "组织不存在"),
    ESTAB_LOGIN_FORBIDDEN("AUTH_403_ESTAB", "组织禁止该登录方式"),
    SYSTEM_ERROR("AUTH_500", "系统错误");

    private final String code;
    private final String message;
}
