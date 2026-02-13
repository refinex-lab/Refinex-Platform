package cn.refinex.gateway.auth;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.refinex.api.user.enums.UserPermission;
import cn.refinex.api.user.enums.UserRole;
import cn.refinex.api.user.enums.UserState;
import cn.refinex.api.user.model.vo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限加载接口实现
 * <p>
 * 从 Session (Redis) 中加载用户信息，计算用户的 角色 和 权限列表。
 *
 * @author refinex
 */
@Slf4j
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 返回指定登录的用户 获得的 权限码列表
     *
     * @param loginId   登录id
     * @param loginType 登录类型
     * @return 权限码列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 根据登录id获取用户信息
        UserInfo userInfo = getUserInfo(loginId);
        if (userInfo == null) {
            return Collections.emptyList();
        }

        // 获取用户状态
        String state = userInfo.getState();

        // 管理员、活跃用户、已认证用户 -> 拥有基础权限 + 认证权限
        if (userInfo.getUserRole() == UserRole.ADMIN
                || UserState.ACTIVE.name().equals(state)
                || UserState.AUTH.name().equals(state)) {
            return List.of(UserPermission.BASIC.name(), UserPermission.AUTH.name());
        }

        // 初始用户 -> 仅基础权限
        if (UserState.INIT.name().equals(state)) {
            return List.of(UserPermission.BASIC.name());
        }

        // 冻结用户 -> 仅冻结权限
        if (UserState.FROZEN.name().equals(state)) {
            return List.of(UserPermission.FROZEN.name());
        }

        // 其他状态 (游客) -> 无权限
        return List.of(UserPermission.NONE.name());
    }

    /**
     * 返回指定登录的用户 获得的 角色列表
     *
     * @param loginId   登录id
     * @param loginType 登录类型
     * @return 角色列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 根据登录id获取用户信息
        UserInfo userInfo = getUserInfo(loginId);
        if (userInfo == null) {
            return Collections.emptyList();
        }

        // 管理员 -> 管理员角色
        if (userInfo.getUserRole() == UserRole.ADMIN) {
            return List.of(UserRole.ADMIN.name());
        }

        // 普通用户 -> 普通用户角色
        return List.of(UserRole.CUSTOMER.name());
    }

    /**
     * 从 Sa-Session 获取 UserInfo
     *
     * @param loginId 登录id
     * @return UserInfo
     */
    private UserInfo getUserInfo(Object loginId) {
        try {
            SaSession session = StpUtil.getSessionByLoginId(loginId);
            return (UserInfo) session.get(String.valueOf(loginId));
        } catch (Exception e) {
            log.warn("Failed to get UserInfo from session for loginId: {}", loginId);
            return null;
        }
    }
}
