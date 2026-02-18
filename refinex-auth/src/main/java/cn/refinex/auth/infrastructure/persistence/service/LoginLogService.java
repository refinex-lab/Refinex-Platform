package cn.refinex.auth.infrastructure.persistence.service;

import cn.refinex.auth.domain.entity.LogLogin;
import cn.refinex.auth.domain.enums.LoginSource;
import cn.refinex.auth.domain.model.LoginContext;
import cn.refinex.auth.domain.model.LoginLogEvent;
import cn.refinex.auth.infrastructure.mapper.LogLoginMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 登录日志服务
 *
 * @author refinex
 */
@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LogLoginMapper logLoginMapper;

    /**
     * 记录登录日志
     *
     * @param event 登录日志事件
     */
    public void recordLoginLog(LoginLogEvent event) {
        if (event == null) {
            return;
        }

        LogLogin logLogin = new LogLogin();
        logLogin.setUserId(event.getUserId());
        logLogin.setEstabId(event.getEstabId());
        logLogin.setIdentityId(event.getIdentityId());
        logLogin.setLoginType(event.getLoginType() == null ? null : event.getLoginType().getCode());

        LoginSource source = event.getSource();
        LoginContext context = event.getContext();
        if (source == null && context != null) {
            source = context.getSource();
        }

        logLogin.setSourceType(source == null ? null : source.getCode());
        logLogin.setSuccess(event.isSuccess() ? 1 : 0);
        logLogin.setFailureReason(event.getFailureReason());
        logLogin.setRequestId(event.getRequestId());

        if (context != null) {
            logLogin.setIp(context.getIp());
            logLogin.setUserAgent(context.getUserAgent());
            logLogin.setDeviceId(context.getDeviceId());
            logLogin.setClientId(context.getClientId());
        }

        logLoginMapper.insert(logLogin);
    }
}
