package cn.refinex.system.infrastructure.persistence.repository;

import cn.refinex.system.domain.model.entity.ErrorLogEntity;
import cn.refinex.system.domain.model.entity.LoginLogEntity;
import cn.refinex.system.domain.model.entity.NotifyLogEntity;
import cn.refinex.system.domain.model.entity.OperateLogEntity;
import cn.refinex.system.domain.repository.LogRepository;
import cn.refinex.system.infrastructure.converter.ErrorLogDoConverter;
import cn.refinex.system.infrastructure.converter.LoginLogDoConverter;
import cn.refinex.system.infrastructure.converter.NotifyLogDoConverter;
import cn.refinex.system.infrastructure.converter.OperateLogDoConverter;
import cn.refinex.system.infrastructure.persistence.dataobject.LogErrorDo;
import cn.refinex.system.infrastructure.persistence.dataobject.LogLoginDo;
import cn.refinex.system.infrastructure.persistence.dataobject.LogNotifyDo;
import cn.refinex.system.infrastructure.persistence.dataobject.LogOperateDo;
import cn.refinex.system.infrastructure.persistence.mapper.LogErrorMapper;
import cn.refinex.system.infrastructure.persistence.mapper.LogLoginMapper;
import cn.refinex.system.infrastructure.persistence.mapper.LogNotifyMapper;
import cn.refinex.system.infrastructure.persistence.mapper.LogOperateMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 日志仓储实现
 *
 * @author refinex
 */
@Repository
@RequiredArgsConstructor
public class LogRepositoryImpl implements LogRepository {

    private final LogLoginMapper logLoginMapper;
    private final LogOperateMapper logOperateMapper;
    private final LogErrorMapper logErrorMapper;
    private final LogNotifyMapper logNotifyMapper;
    private final LoginLogDoConverter loginLogDoConverter;
    private final OperateLogDoConverter operateLogDoConverter;
    private final ErrorLogDoConverter errorLogDoConverter;
    private final NotifyLogDoConverter notifyLogDoConverter;

    /**
     * 查询登录日志列表
     *
     * @param userId     用户ID
     * @param estabId    企业ID
     * @param success    登录是否成功
     * @param loginType  登录类型
     * @param sourceType 登录来源
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param limit      限制条数
     * @return 登录日志列表
     */
    @Override
    public List<LoginLogEntity> listLoginLogs(Long userId, Long estabId, Integer success, Integer loginType, Integer sourceType,
                                              LocalDateTime startTime, LocalDateTime endTime, Integer limit) {
        LambdaQueryWrapper<LogLoginDo> query = Wrappers.lambdaQuery(LogLoginDo.class)
                .eq(LogLoginDo::getDeleted, 0)
                .orderByDesc(LogLoginDo::getId);
        if (userId != null) {
            query.eq(LogLoginDo::getUserId, userId);
        }
        if (estabId != null) {
            query.eq(LogLoginDo::getEstabId, estabId);
        }
        if (success != null) {
            query.eq(LogLoginDo::getSuccess, success);
        }
        if (loginType != null) {
            query.eq(LogLoginDo::getLoginType, loginType);
        }
        if (sourceType != null) {
            query.eq(LogLoginDo::getSourceType, sourceType);
        }
        if (startTime != null) {
            query.ge(LogLoginDo::getGmtCreate, startTime);
        }
        if (endTime != null) {
            query.le(LogLoginDo::getGmtCreate, endTime);
        }
        query.last("LIMIT " + (limit == null ? 50 : limit));
        List<LogLoginDo> rows = logLoginMapper.selectList(query);
        List<LoginLogEntity> result = new ArrayList<>();
        for (LogLoginDo row : rows) {
            result.add(loginLogDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 根据日志ID查询登录日志
     *
     * @param logId 日志ID
     * @return 登录日志
     */
    @Override
    public LoginLogEntity findLoginLogById(Long logId) {
        LogLoginDo row = logLoginMapper.selectById(logId);
        return row == null ? null : loginLogDoConverter.toEntity(row);
    }

    /**
     * 查询操作日志列表
     *
     * @param userId      用户ID
     * @param estabId     企业ID
     * @param success     操作是否成功
     * @param moduleCode  模块编码
     * @param requestPath 请求路径
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param limit       限制条数
     * @return 操作日志列表
     */
    @Override
    public List<OperateLogEntity> listOperateLogs(Long userId, Long estabId, Integer success, String moduleCode, String requestPath,
                                                  LocalDateTime startTime, LocalDateTime endTime, Integer limit) {
        LambdaQueryWrapper<LogOperateDo> query = Wrappers.lambdaQuery(LogOperateDo.class)
                .eq(LogOperateDo::getDeleted, 0)
                .orderByDesc(LogOperateDo::getId);
        if (userId != null) {
            query.eq(LogOperateDo::getUserId, userId);
        }
        if (estabId != null) {
            query.eq(LogOperateDo::getEstabId, estabId);
        }
        if (success != null) {
            query.eq(LogOperateDo::getSuccess, success);
        }
        if (moduleCode != null && !moduleCode.isBlank()) {
            query.eq(LogOperateDo::getModuleCode, moduleCode.trim());
        }
        if (requestPath != null && !requestPath.isBlank()) {
            query.like(LogOperateDo::getRequestPath, requestPath.trim());
        }
        if (startTime != null) {
            query.ge(LogOperateDo::getGmtCreate, startTime);
        }
        if (endTime != null) {
            query.le(LogOperateDo::getGmtCreate, endTime);
        }
        query.last("LIMIT " + (limit == null ? 50 : limit));
        List<LogOperateDo> rows = logOperateMapper.selectList(query);
        List<OperateLogEntity> result = new ArrayList<>();
        for (LogOperateDo row : rows) {
            result.add(operateLogDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 根据日志ID查询操作日志
     *
     * @param logId 日志ID
     * @return 操作日志
     */
    @Override
    public OperateLogEntity findOperateLogById(Long logId) {
        LogOperateDo row = logOperateMapper.selectById(logId);
        return row == null ? null : operateLogDoConverter.toEntity(row);
    }

    /**
     * 查询错误日志列表
     *
     * @param serviceName 服务名
     * @param errorLevel  错误级别
     * @param requestPath 请求路径
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param limit       限制条数
     * @return 错误日志列表
     */
    @Override
    public List<ErrorLogEntity> listErrorLogs(String serviceName, Integer errorLevel, String requestPath,
                                              LocalDateTime startTime, LocalDateTime endTime, Integer limit) {
        LambdaQueryWrapper<LogErrorDo> query = Wrappers.lambdaQuery(LogErrorDo.class)
                .eq(LogErrorDo::getDeleted, 0)
                .orderByDesc(LogErrorDo::getId);
        if (serviceName != null && !serviceName.isBlank()) {
            query.eq(LogErrorDo::getServiceName, serviceName.trim());
        }
        if (errorLevel != null) {
            query.eq(LogErrorDo::getErrorLevel, errorLevel);
        }
        if (requestPath != null && !requestPath.isBlank()) {
            query.like(LogErrorDo::getRequestPath, requestPath.trim());
        }
        if (startTime != null) {
            query.ge(LogErrorDo::getGmtCreate, startTime);
        }
        if (endTime != null) {
            query.le(LogErrorDo::getGmtCreate, endTime);
        }
        query.last("LIMIT " + (limit == null ? 50 : limit));
        List<LogErrorDo> rows = logErrorMapper.selectList(query);
        List<ErrorLogEntity> result = new ArrayList<>();
        for (LogErrorDo row : rows) {
            result.add(errorLogDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 根据日志ID查询错误日志
     *
     * @param logId 日志ID
     * @return 错误日志
     */
    @Override
    public ErrorLogEntity findErrorLogById(Long logId) {
        LogErrorDo row = logErrorMapper.selectById(logId);
        return row == null ? null : errorLogDoConverter.toEntity(row);
    }

    /**
     * 查询通知日志列表
     *
     * @param channelType 通知通道
     * @param sceneCode   业务场景编码
     * @param receiver    接收目标
     * @param sendStatus  发送状态
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param limit       限制条数
     * @return 通知日志列表
     */
    @Override
    public List<NotifyLogEntity> listNotifyLogs(Integer channelType, String sceneCode, String receiver, Integer sendStatus,
                                                LocalDateTime startTime, LocalDateTime endTime, Integer limit) {
        LambdaQueryWrapper<LogNotifyDo> query = Wrappers.lambdaQuery(LogNotifyDo.class)
                .eq(LogNotifyDo::getDeleted, 0)
                .orderByDesc(LogNotifyDo::getId);
        if (channelType != null) {
            query.eq(LogNotifyDo::getChannelType, channelType);
        }
        if (sceneCode != null && !sceneCode.isBlank()) {
            query.eq(LogNotifyDo::getSceneCode, sceneCode.trim());
        }
        if (receiver != null && !receiver.isBlank()) {
            query.like(LogNotifyDo::getReceiver, receiver.trim());
        }
        if (sendStatus != null) {
            query.eq(LogNotifyDo::getSendStatus, sendStatus);
        }
        if (startTime != null) {
            query.ge(LogNotifyDo::getGmtCreate, startTime);
        }
        if (endTime != null) {
            query.le(LogNotifyDo::getGmtCreate, endTime);
        }
        query.last("LIMIT " + (limit == null ? 50 : limit));
        List<LogNotifyDo> rows = logNotifyMapper.selectList(query);
        List<NotifyLogEntity> result = new ArrayList<>();
        for (LogNotifyDo row : rows) {
            result.add(notifyLogDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 根据日志ID查询通知日志
     *
     * @param logId 日志ID
     * @return 通知日志
     */
    @Override
    public NotifyLogEntity findNotifyLogById(Long logId) {
        LogNotifyDo row = logNotifyMapper.selectById(logId);
        return row == null ? null : notifyLogDoConverter.toEntity(row);
    }
}
