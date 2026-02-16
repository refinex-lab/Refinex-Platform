package cn.refinex.gateway.auth;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.refinex.api.user.enums.UserStatus;
import cn.refinex.api.user.model.context.LoginUser;
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
        // 根据登录id获取登录用户信息
        LoginUser loginUser = getLoginUser(loginId);
        if (loginUser == null) {
            return Collections.emptyList();
        }

        // 非启用用户不下发权限
        if (loginUser.getStatus() != null && loginUser.getStatus() != UserStatus.ENABLED) {
            return Collections.emptyList();
        }

        // 直接返回预聚合权限列表
        List<String> permissionCodes = loginUser.getPermissionCodes();
        return permissionCodes == null ? Collections.emptyList() : permissionCodes;
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
        // 根据登录id获取登录用户信息
        LoginUser loginUser = getLoginUser(loginId);
        if (loginUser == null) {
            return Collections.emptyList();
        }

        // 直接返回预聚合角色列表
        List<String> roleCodes = loginUser.getRoleCodes();
        return roleCodes == null ? Collections.emptyList() : roleCodes;
    }

    /**
     * 从 Sa-Session 获取 LoginUser
     *
     * @param loginId 登录id
     * @return LoginUser
     */
    private LoginUser getLoginUser(Object loginId) {
        try {
            SaSession session = StpUtil.getSessionByLoginId(loginId);
            LoginUser loginUser = (LoginUser) session.get(LoginUser.SESSION_KEY);
            if (loginUser != null) {
                return loginUser;
            }
            return (LoginUser) session.get(String.valueOf(loginId));
        } catch (Exception e) {
            log.warn("Failed to get LoginUser from session for loginId: {}", loginId);
            return null;
        }
    }
}
