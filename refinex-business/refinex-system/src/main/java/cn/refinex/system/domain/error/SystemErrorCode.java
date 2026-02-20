package cn.refinex.system.domain.error;

import cn.refinex.base.exception.code.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统管理服务错误码
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum SystemErrorCode implements ErrorCode {

    INVALID_PARAM("SYSTEM_400", "参数错误"),
    SYSTEM_NOT_FOUND("SYSTEM_404_SYSTEM", "系统不存在"),
    ROLE_NOT_FOUND("SYSTEM_404_ROLE", "角色不存在"),
    VALUESET_NOT_FOUND("SYSTEM_404_VALUESET", "值集不存在"),
    VALUE_NOT_FOUND("SYSTEM_404_VALUE", "值集明细不存在"),
    DRS_NOT_FOUND("SYSTEM_404_DRS", "数据资源不存在"),
    DRS_INTERFACE_NOT_FOUND("SYSTEM_404_DRS_INTERFACE", "数据资源接口不存在"),
    ESTAB_NOT_FOUND("SYSTEM_404_ESTAB", "企业不存在"),
    ESTAB_ADDRESS_NOT_FOUND("SYSTEM_404_ESTAB_ADDRESS", "企业地址不存在"),
    ESTAB_USER_NOT_FOUND("SYSTEM_404_ESTAB_USER", "企业成员关系不存在"),
    TEAM_NOT_FOUND("SYSTEM_404_TEAM", "团队不存在"),
    TEAM_USER_NOT_FOUND("SYSTEM_404_TEAM_USER", "团队成员关系不存在"),
    LOGIN_LOG_NOT_FOUND("SYSTEM_404_LOGIN_LOG", "登录日志不存在"),
    OPERATE_LOG_NOT_FOUND("SYSTEM_404_OPERATE_LOG", "操作日志不存在"),
    ERROR_LOG_NOT_FOUND("SYSTEM_404_ERROR_LOG", "错误日志不存在"),
    NOTIFY_LOG_NOT_FOUND("SYSTEM_404_NOTIFY_LOG", "通知日志不存在"),
    SYSTEM_CODE_DUPLICATED("SYSTEM_409_SYSTEM_CODE", "系统编码已存在"),
    ROLE_CODE_DUPLICATED("SYSTEM_409_ROLE_CODE", "角色编码已存在"),
    VALUESET_CODE_DUPLICATED("SYSTEM_409_VALUESET_CODE", "值集编码已存在"),
    VALUE_CODE_DUPLICATED("SYSTEM_409_VALUE_CODE", "值编码已存在"),
    MENU_CODE_DUPLICATED("SYSTEM_409_MENU_CODE", "菜单编码已存在"),
    MENU_OP_CODE_DUPLICATED("SYSTEM_409_MENU_OP_CODE", "菜单操作编码已存在"),
    DRS_CODE_DUPLICATED("SYSTEM_409_DRS_CODE", "数据资源编码已存在"),
    DRS_INTERFACE_CODE_DUPLICATED("SYSTEM_409_DRS_INTERFACE_CODE", "数据资源接口编码已存在"),
    ESTAB_CODE_DUPLICATED("SYSTEM_409_ESTAB_CODE", "企业编码已存在"),
    ESTAB_USER_DUPLICATED("SYSTEM_409_ESTAB_USER", "企业成员关系已存在"),
    TEAM_CODE_DUPLICATED("SYSTEM_409_TEAM_CODE", "团队编码已存在"),
    TEAM_USER_DUPLICATED("SYSTEM_409_TEAM_USER", "团队成员关系已存在"),
    MENU_NOT_FOUND("SYSTEM_404_MENU", "菜单不存在"),
    MENU_OP_NOT_FOUND("SYSTEM_404_MENU_OP", "菜单操作不存在"),
    MENU_BUILTIN_FORBIDDEN("SYSTEM_409_MENU_BUILTIN", "内建菜单不允许删除"),
    MENU_HAS_CHILDREN("SYSTEM_409_MENU_CHILDREN", "菜单存在子节点，不允许删除"),
    MENU_HAS_OPS("SYSTEM_409_MENU_OPS", "菜单存在操作项，不允许删除"),
    ESTAB_HAS_USERS("SYSTEM_409_ESTAB_USERS", "企业下存在成员，不允许删除"),
    ESTAB_HAS_TEAMS("SYSTEM_409_ESTAB_TEAMS", "企业下存在团队，不允许删除"),
    TEAM_PARENT_INVALID("SYSTEM_409_TEAM_PARENT", "团队父级不合法"),
    TEAM_HAS_CHILDREN("SYSTEM_409_TEAM_CHILDREN", "团队存在子团队，不允许删除"),
    TEAM_HAS_USERS("SYSTEM_409_TEAM_USERS", "团队存在成员，不允许删除"),
    VALUESET_HAS_VALUES("SYSTEM_409_VALUESET_VALUES", "值集下存在明细数据，不允许删除"),
    DRS_HAS_INTERFACES("SYSTEM_409_DRS_INTERFACES", "数据资源下存在接口，不允许删除");

    private final String code;
    private final String message;
}
