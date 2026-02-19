package cn.refinex.user.domain.error;

import cn.refinex.base.exception.code.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户服务错误码
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    INVALID_PARAM("USER_400", "参数错误"),
    USER_NOT_FOUND("USER_404", "用户不存在"),
    IDENTITY_NOT_FOUND("USER_404_ID", "登录身份不存在"),
    IDENTITY_DISABLED("USER_403_ID", "登录身份不可用"),
    USER_DISABLED("USER_403", "用户已停用"),
    USER_LOCKED("USER_423", "账号已锁定"),
    OLD_PASSWORD_INCORRECT("USER_401_PWD", "旧密码错误"),
    DUPLICATE_IDENTITY("USER_409_ID", "账号已存在"),
    USER_CODE_DUPLICATED("USER_409_CODE", "用户编码已存在"),
    USERNAME_DUPLICATED("USER_409_USERNAME", "用户名已存在"),
    LAST_IDENTITY_NOT_ALLOW_DELETE("USER_409_ID_LAST", "至少保留一个登录身份"),
    REGISTER_TYPE_NOT_SUPPORTED("USER_422_REG", "注册方式暂不支持"),
    PASSWORD_RESET_NOT_SUPPORTED("USER_422_PWD_RESET", "该账号暂不支持密码重置"),
    ESTAB_NOT_FOUND("USER_404_ESTAB", "组织不存在");

    private final String code;
    private final String message;
}
