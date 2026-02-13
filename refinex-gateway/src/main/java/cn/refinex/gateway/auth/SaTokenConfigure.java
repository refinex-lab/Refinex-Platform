package cn.refinex.gateway.auth;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.refinex.api.user.enums.UserPermission;
import cn.refinex.api.user.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 全局过滤器配置 (WebFlux 版)
 *
 * @author refinex
 */
@Slf4j
@Configuration
public class SaTokenConfigure {

    /**
     * 配置 Sa-Token 全局过滤器
     */
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 1. 拦截所有请求
                .addInclude("/**")
                // 2. 开放静态资源与端点: 图标、Actuator
                .addExclude("/favicon.ico", "/actuator/**")
                // 3. 鉴权逻辑
                .setAuth(obj -> {
                    // A. 登录校验
                    SaRouter
                            // 拦截所有
                            .match("/**")
                            // 排除白名单
                            .notMatch(
                                    // 放行登陆接口
                                    "/auth/login",
                                    // 放行注册接口
                                    "/auth/register",
                                    // 放行支付回调接口
                                    "/wxPay/notify"
                            )
                            // 其他请求都需要登录校验
                            .check(r -> StpUtil.checkLogin());

                    // B. 角色/权限校验
                    // 管理员模块需要管理员角色才能访问
                    SaRouter.match("/admin/**", r -> StpUtil.checkRole(UserRole.ADMIN.name()));

                    // 交易模块需要需实名认证权限才能访问
                    SaRouter.match("/trade/**", r -> StpUtil.checkPermission(UserPermission.AUTH.name()));

                    // 用户中心/订单中心 (基础权限 or 冻结状态限制)
                    // 注意：这里的业务逻辑可能需要根据实际需求调整，冻结用户是否允许查看订单？
                    SaRouter.match("/user/**", "/order/**", r ->
                            StpUtil.checkPermissionOr(UserPermission.BASIC.name(), UserPermission.FROZEN.name())
                    );
                })
                // 4. 异常处理 (返回 JSON 给前端)
                .setError(this::processError);
    }

    /**
     * 统一异常封装
     *
     * @param e 错误异常
     * @return 处理结果
     */
    private SaResult processError(Throwable e) {
        if (e instanceof NotLoginException) {
            // 401 未登录
            return SaResult.error("Token 无效或已过期，请重新登录").setCode(401);
        } else if (e instanceof NotRoleException) {
            // 403 无权限
            return SaResult.error("无权操作").setCode(403);
        } else if (e instanceof NotPermissionException notPermissionException) {
            // 403 无权限
            String msg = "无权操作";
            if (UserPermission.AUTH.name().equals(notPermissionException.getPermission())) {
                msg = "请先完成实名认证";
            }
            return SaResult.error(msg).setCode(403);
        }

        // 其他未知异常
        log.error("Gateway Auth Error", e);
        return SaResult.error("网关鉴权异常: " + e.getMessage());
    }
}
