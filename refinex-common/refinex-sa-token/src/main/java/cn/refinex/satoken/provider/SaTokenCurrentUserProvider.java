package cn.refinex.satoken.provider;

import cn.refinex.api.user.context.CurrentUserProvider;
import cn.refinex.api.user.model.context.LoginUser;
import cn.refinex.satoken.helper.LoginUserHelper;
import org.springframework.stereotype.Component;

/**
 * Sa-Token 当前用户提供者实现
 *
 * @author refinex
 */
@Component
public class SaTokenCurrentUserProvider implements CurrentUserProvider {

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    @Override
    public Long getCurrentUserId() {
        return LoginUserHelper.getUserId();
    }

    /**
     * 获取当前组织ID
     *
     * @return 组织ID
     */
    @Override
    public Long getCurrentEstabId() {
        return LoginUserHelper.getEstabId();
    }

    /**
     * 获取当前团队ID
     *
     * @return 团队ID
     */
    @Override
    public Long getCurrentTeamId() {
        return LoginUserHelper.getTeamId();
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 登录用户信息
     */
    @Override
    public LoginUser getCurrentUser() {
        return LoginUserHelper.getLoginUser();
    }
}
