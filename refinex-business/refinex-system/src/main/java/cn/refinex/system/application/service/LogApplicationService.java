package cn.refinex.system.application.service;

import cn.refinex.api.user.model.dto.UserManageDTO;
import cn.refinex.api.user.model.dto.UserManageListQuery;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.PageResponse;
import cn.refinex.base.utils.PageUtils;
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
import cn.refinex.system.infrastructure.client.user.UserManageRemoteGateway;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 日志查询应用服务
 *
 * @author refinex
 */
@Service
@RequiredArgsConstructor
public class LogApplicationService {

    private final LogRepository logRepository;
    private final SystemDomainAssembler systemDomainAssembler;
    private final UserManageRemoteGateway userManageRemoteGateway;

    /**
     * 查询登录日志列表
     *
     * @param command 查询命令
     * @return 登录日志列表
     */
    public PageResponse<LoginLogDTO> listLoginLogs(QueryLoginLogListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(), PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);

        PageResponse<LoginLogEntity> entities = logRepository.listLoginLogs(
                command == null ? null : command.getUserId(),
                command == null ? null : command.getEstabId(),
                command == null ? null : command.getSuccess(),
                command == null ? null : command.getLoginType(),
                command == null ? null : command.getSourceType(),
                command == null ? null : command.getStartTime(),
                command == null ? null : command.getEndTime(),
                currentPage,
                pageSize
        );

        List<LoginLogEntity> entitiesData = entities.getData();
        if (CollectionUtils.isEmpty(entitiesData)) {
            return PageResponse.of(Collections.emptyList(), 0, pageSize, currentPage);
        }

        List<LoginLogDTO> result = new ArrayList<>();
        for (LoginLogEntity entity : entities.getData()) {
            result.add(systemDomainAssembler.toLoginLogDto(entity));
        }
        enrichLoginLogs(result);

        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
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
        LoginLogDTO dto = systemDomainAssembler.toLoginLogDto(entity);
        enrichLoginLog(dto);
        return dto;
    }

    /**
     * 查询操作日志列表
     *
     * @param command 查询命令
     * @return 操作日志列表
     */
    public PageResponse<OperateLogDTO> listOperateLogs(QueryOperateLogListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);
        PageResponse<OperateLogEntity> entities = logRepository.listOperateLogs(
                command == null ? null : command.getUserId(),
                command == null ? null : command.getEstabId(),
                command == null ? null : command.getSuccess(),
                command == null ? null : command.getModuleCode(),
                command == null ? null : command.getRequestPath(),
                command == null ? null : command.getStartTime(),
                command == null ? null : command.getEndTime(),
                currentPage,
                pageSize
        );
        List<OperateLogDTO> result = new ArrayList<>();
        for (OperateLogEntity entity : entities.getData()) {
            result.add(systemDomainAssembler.toOperateLogDto(entity));
        }
        enrichOperateLogs(result);
        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
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
        OperateLogDTO dto = systemDomainAssembler.toOperateLogDto(entity);
        enrichOperateLog(dto);
        return dto;
    }

    /**
     * 查询错误日志列表
     *
     * @param command 查询命令
     * @return 错误日志列表
     */
    public PageResponse<ErrorLogDTO> listErrorLogs(QueryErrorLogListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);
        PageResponse<ErrorLogEntity> entities = logRepository.listErrorLogs(
                command == null ? null : command.getServiceName(),
                command == null ? null : command.getErrorLevel(),
                command == null ? null : command.getRequestPath(),
                command == null ? null : command.getStartTime(),
                command == null ? null : command.getEndTime(),
                currentPage,
                pageSize
        );
        List<ErrorLogDTO> result = new ArrayList<>();
        for (ErrorLogEntity entity : entities.getData()) {
            result.add(systemDomainAssembler.toErrorLogDto(entity));
        }
        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
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
    public PageResponse<NotifyLogDTO> listNotifyLogs(QueryNotifyLogListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);
        PageResponse<NotifyLogEntity> entities = logRepository.listNotifyLogs(
                command == null ? null : command.getChannelType(),
                command == null ? null : command.getSceneCode(),
                command == null ? null : command.getReceiver(),
                command == null ? null : command.getSendStatus(),
                command == null ? null : command.getStartTime(),
                command == null ? null : command.getEndTime(),
                currentPage,
                pageSize
        );
        List<NotifyLogDTO> result = new ArrayList<>();
        for (NotifyLogEntity entity : entities.getData()) {
            result.add(systemDomainAssembler.toNotifyLogDto(entity));
        }
        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
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
     * 批量补充登录日志用户名
     *
     * @param logs 登录日志
     */
    private void enrichLoginLogs(List<LoginLogDTO> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }
        List<Long> userIds = logs.stream()
                .map(LoginLogDTO::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, UserManageDTO> userMap = loadUserMapByIds(userIds);
        for (LoginLogDTO log : logs) {
            fillLoginLogUser(log, userMap.get(log.getUserId()));
        }
    }

    /**
     * 补充单条登录日志用户名
     *
     * @param log 登录日志
     */
    private void enrichLoginLog(LoginLogDTO log) {
        if (log == null || log.getUserId() == null) {
            fillLoginLogUser(log, null);
            return;
        }
        Map<Long, UserManageDTO> userMap = loadUserMapByIds(List.of(log.getUserId()));
        fillLoginLogUser(log, userMap.get(log.getUserId()));
    }

    /**
     * 批量补充操作日志用户名
     *
     * @param logs 操作日志
     */
    private void enrichOperateLogs(List<OperateLogDTO> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }
        List<Long> userIds = logs.stream()
                .map(OperateLogDTO::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, UserManageDTO> userMap = loadUserMapByIds(userIds);
        for (OperateLogDTO log : logs) {
            fillOperateLogUser(log, userMap.get(log.getUserId()));
        }
    }

    /**
     * 补充单条操作日志用户名
     *
     * @param log 操作日志
     */
    private void enrichOperateLog(OperateLogDTO log) {
        if (log == null || log.getUserId() == null) {
            fillOperateLogUser(log, null);
            return;
        }
        Map<Long, UserManageDTO> userMap = loadUserMapByIds(List.of(log.getUserId()));
        fillOperateLogUser(log, userMap.get(log.getUserId()));
    }

    /**
     * 根据用户ID批量加载用户信息
     *
     * @param userIds 用户ID
     * @return 用户映射
     */
    private Map<Long, UserManageDTO> loadUserMapByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        UserManageListQuery query = new UserManageListQuery();
        query.setUserIds(userIds);
        query.setCurrentPage(1);
        query.setPageSize(userIds.size());
        PageResponse<UserManageDTO> users = userManageRemoteGateway.listUsers(query);
        List<UserManageDTO> rows = users.getData() == null ? Collections.emptyList() : users.getData();
        Map<Long, UserManageDTO> userMap = new HashMap<>();
        for (UserManageDTO row : rows) {
            if (row != null && row.getUserId() != null) {
                userMap.put(row.getUserId(), row);
            }
        }
        return userMap;
    }

    /**
     * 填充登录日志用户名
     *
     * @param log  日志DTO
     * @param user 用户信息
     */
    private void fillLoginLogUser(LoginLogDTO log, UserManageDTO user) {
        if (log == null) {
            return;
        }
        log.setUsername(user == null ? null : user.getUsername());
    }

    /**
     * 填充操作日志用户名
     *
     * @param log  日志DTO
     * @param user 用户信息
     */
    private void fillOperateLogUser(OperateLogDTO log, UserManageDTO user) {
        if (log == null) {
            return;
        }
        log.setUsername(user == null ? null : user.getUsername());
    }

}
