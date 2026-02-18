package cn.refinex.auth.interfaces.listener;

import cn.dev33.satoken.listener.SaTokenListenerForSimple;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.refinex.auth.domain.model.LoginLogContextHolder;
import cn.refinex.auth.domain.model.LoginLogEvent;
import cn.refinex.auth.infrastructure.persistence.service.LoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Sa-Token 事件监听器
 *
 * @author refinex
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthSaTokenListener extends SaTokenListenerForSimple {

    private final LoginLogService loginLogService;

    /**
     * 登录时触发
     */
    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginParameter loginParameter) {
        try {
            LoginLogEvent event = LoginLogContextHolder.take();
            if (event != null) {
                loginLogService.recordLoginLog(event);
            }
        } catch (Exception ex) {
            log.warn("Sa-Token login listener error", ex);
        }
    }
}
