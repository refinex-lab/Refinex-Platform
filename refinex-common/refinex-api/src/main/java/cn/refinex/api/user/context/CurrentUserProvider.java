package cn.refinex.api.user.context;

import cn.refinex.api.user.model.context.LoginUser;

/**
 * 当前登录用户信息提供者（用于基础组件取数）
 *
 * @author refinex
 */
public interface CurrentUserProvider {

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    Long getCurrentUserId();

    /**
     * 获取当前组织ID
     *
     * @return 组织ID
     */
    Long getCurrentEstabId();

    /**
     * 获取当前团队ID
     *
     * @return 团队ID
     */
    Long getCurrentTeamId();

    /**
     * 获取当前登录用户信息
     *
     * @return 登录用户信息
     */
    LoginUser getCurrentUser();
}
