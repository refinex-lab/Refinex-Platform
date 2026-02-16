package cn.refinex.satoken.helper;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.refinex.api.user.model.context.LoginUser;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 当前登录用户工具类
 * <p>
 * 统一封装登录用户上下文读取，便于业务模块快速获取常用信息。
 *
 * @author refinex
 */
@UtilityClass
public class LoginUserHelper {

    /**
     * 当前是否已登录
     *
     * @return 是否登录
     */
    public static boolean isLogin() {
        return StpUtil.isLogin();
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (NotLoginException e) {
            return null;
        }
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 登录用户信息
     */
    public static LoginUser getLoginUser() {
        try {
            SaSession session = StpUtil.getSession();
            return (LoginUser) session.get(LoginUser.SESSION_KEY);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 通过登录ID获取登录用户信息
     *
     * @param loginId 登录ID
     * @return 登录用户信息
     */
    public static LoginUser getLoginUser(Object loginId) {
        if (loginId == null) {
            return null;
        }
        try {
            SaSession session = StpUtil.getSessionByLoginId(loginId);
            LoginUser loginUser = (LoginUser) session.get(LoginUser.SESSION_KEY);
            if (loginUser != null) {
                return loginUser;
            }
            return (LoginUser) session.get(String.valueOf(loginId));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 设置登录用户信息到 Session
     *
     * @param loginUser 登录用户
     */
    public static void setLoginUser(LoginUser loginUser) {
        if (loginUser == null) {
            return;
        }
        SaSession session = StpUtil.getSession();
        session.set(LoginUser.SESSION_KEY, loginUser);
        try {
            Object loginId = StpUtil.getLoginId();
            if (loginId != null) {
                session.set(String.valueOf(loginId), loginUser);
            }
        } catch (NotLoginException ignored) {
            // ignore
        }
    }

    /**
     * 获取当前组织ID
     *
     * @return 组织ID
     */
    public static Long getEstabId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getEstabId();
    }

    /**
     * 获取当前团队ID
     *
     * @return 团队ID
     */
    public static Long getTeamId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getTeamId();
    }

    /**
     * 获取当前角色列表
     *
     * @return 角色编码列表
     */
    public static List<String> getRoleCodes() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null || loginUser.getRoleCodes() == null
                ? Collections.emptyList()
                : loginUser.getRoleCodes();
    }

    /**
     * 获取当前权限列表
     *
     * @return 权限编码列表
     */
    public static List<String> getPermissionCodes() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null || loginUser.getPermissionCodes() == null
                ? Collections.emptyList()
                : loginUser.getPermissionCodes();
    }
}
