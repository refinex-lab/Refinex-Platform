package cn.refinex.system.application.service;

import cn.refinex.base.exception.BizException;
import cn.refinex.system.application.assembler.SystemDomainAssembler;
import cn.refinex.system.application.command.QueryErrorLogListCommand;
import cn.refinex.system.application.command.QueryLoginLogListCommand;
import cn.refinex.system.application.command.QueryNotifyLogListCommand;
import cn.refinex.system.application.command.QueryOperateLogListCommand;
import cn.refinex.system.application.dto.ErrorLogDTO;
import cn.refinex.system.application.dto.LoginLogDTO;
import cn.refinex.system.application.dto.NotifyLogDTO;
import cn.refinex.system.application.dto.OperateLogDTO;
import cn.refinex.system.domain.error.SystemErrorCode;
import cn.refinex.system.domain.model.entity.ErrorLogEntity;
import cn.refinex.system.domain.model.entity.LoginLogEntity;
import cn.refinex.system.domain.model.entity.NotifyLogEntity;
import cn.refinex.system.domain.model.entity.OperateLogEntity;
import cn.refinex.system.domain.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 日志查询应用服务
 *
 * @author refinex
 */
@Service
@RequiredArgsConstructor
public class LogApplicationService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    private final LogRepository logRepository;
    private final SystemDomainAssembler systemDomainAssembler;

    /**
     * 查询登录日志列表
     *
     * @param command 查询命令
     * @return 登录日志列表
     */
    public List<LoginLogDTO> listLoginLogs(QueryLoginLogListCommand command) {
        List<LoginLogEntity> entities = logRepository.listLoginLogs(
                command == null ? null : command.getUserId(),
                command == null ? null : command.getEstabId(),
                command == null ? null : command.getSuccess(),
                command == null ? null : command.getLoginType(),
                command == null ? null : command.getSourceType(),
                command == null ? null : command.getStartTime(),
                command == null ? null : command.getEndTime(),
                normalizeLimit(command == null ? null : command.getLimit())
        );
        List<LoginLogDTO> result = new ArrayList<>();
        for (LoginLogEntity entity : entities) {
            result.add(systemDomainAssembler.toLoginLogDto(entity));
        }
        return result;
    }

    /**
     * 查询登录日志
     *
     * @param logId 日志ID
     * @return 登录日志
     */
    public LoginLogDTO getLoginLog(Long logId) {
        LoginLogEntity entity = logRepository.findLoginLogById(logId);
        if (entity == null) {
            throw new BizException(SystemErrorCode.LOGIN_LOG_NOT_FOUND);
        }
        return systemDomainAssembler.toLoginLogDto(entity);
    }

    /**
     * 查询操作日志列表
     *
     * @param command 查询命令
     * @return 操作日志列表
     */
    public List<OperateLogDTO> listOperateLogs(QueryOperateLogListCommand command) {
        List<OperateLogEntity> entities = logRepository.listOperateLogs(
                command == null ? null : command.getUserId(),
                command == null ? null : command.getEstabId(),
                command == null ? null : command.getSuccess(),
                command == null ? null : command.getModuleCode(),
                command == null ? null : command.getRequestPath(),
                command == null ? null : command.getStartTime(),
                command == null ? null : command.getEndTime(),
                normalizeLimit(command == null ? null : command.getLimit())
        );
        List<OperateLogDTO> result = new ArrayList<>();
        for (OperateLogEntity entity : entities) {
            result.add(systemDomainAssembler.toOperateLogDto(entity));
        }
        return result;
    }

    /**
     * 查询操作日志
     *
     * @param logId 日志ID
     * @return 操作日志
     */
    public OperateLogDTO getOperateLog(Long logId) {
        OperateLogEntity entity = logRepository.findOperateLogById(logId);
        if (entity == null) {
            throw new BizException(SystemErrorCode.OPERATE_LOG_NOT_FOUND);
        }
        return systemDomainAssembler.toOperateLogDto(entity);
    }

    /**
     * 查询错误日志列表
     *
     * @param command 查询命令
     * @return 错误日志列表
     */
    public List<ErrorLogDTO> listErrorLogs(QueryErrorLogListCommand command) {
        List<ErrorLogEntity> entities = logRepository.listErrorLogs(
                command == null ? null : command.getServiceName(),
                command == null ? null : command.getErrorLevel(),
                command == null ? null : command.getRequestPath(),
                command == null ? null : command.getStartTime(),
                command == null ? null : command.getEndTime(),
                normalizeLimit(command == null ? null : command.getLimit())
        );
        List<ErrorLogDTO> result = new ArrayList<>();
        for (ErrorLogEntity entity : entities) {
            result.add(systemDomainAssembler.toErrorLogDto(entity));
        }
        return result;
    }

    /**
     * 查询错误日志
     *
     * @param logId 日志ID
     * @return 错误日志
     */
    public ErrorLogDTO getErrorLog(Long logId) {
        ErrorLogEntity entity = logRepository.findErrorLogById(logId);
        if (entity == null) {
            throw new BizException(SystemErrorCode.ERROR_LOG_NOT_FOUND);
        }
        return systemDomainAssembler.toErrorLogDto(entity);
    }

    /**
     * 查询通知日志列表
     *
     * @param command 查询命令
     * @return 通知日志列表
     */
    public List<NotifyLogDTO> listNotifyLogs(QueryNotifyLogListCommand command) {
        List<NotifyLogEntity> entities = logRepository.listNotifyLogs(
                command == null ? null : command.getChannelType(),
                command == null ? null : command.getSceneCode(),
                command == null ? null : command.getReceiver(),
                command == null ? null : command.getSendStatus(),
                command == null ? null : command.getStartTime(),
                command == null ? null : command.getEndTime(),
                normalizeLimit(command == null ? null : command.getLimit())
        );
        List<NotifyLogDTO> result = new ArrayList<>();
        for (NotifyLogEntity entity : entities) {
            result.add(systemDomainAssembler.toNotifyLogDto(entity));
        }
        return result;
    }

    /**
     * 查询通知日志
     *
     * @param logId 日志ID
     * @return 通知日志
     */
    public NotifyLogDTO getNotifyLog(Long logId) {
        NotifyLogEntity entity = logRepository.findNotifyLogById(logId);
        if (entity == null) {
            throw new BizException(SystemErrorCode.NOTIFY_LOG_NOT_FOUND);
        }
        return systemDomainAssembler.toNotifyLogDto(entity);
    }

    /**
     * 规范化限制条数
     *
     * @param limit 限制条数
     * @return 规范化后的限制条数
     */
    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
