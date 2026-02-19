package cn.refinex.system.domain.repository;

import cn.refinex.system.domain.model.entity.ErrorLogEntity;
import cn.refinex.system.domain.model.entity.LoginLogEntity;
import cn.refinex.system.domain.model.entity.NotifyLogEntity;
import cn.refinex.system.domain.model.entity.OperateLogEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日志仓储
 *
 * @author refinex
 */
public interface LogRepository {

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
    List<LoginLogEntity> listLoginLogs(Long userId, Long estabId, Integer success, Integer loginType, Integer sourceType,
                                       LocalDateTime startTime, LocalDateTime endTime, Integer limit);

    /**
     * 根据日志ID查询登录日志
     *
     * @param logId 日志ID
     * @return 登录日志
     */
    LoginLogEntity findLoginLogById(Long logId);

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
    List<OperateLogEntity> listOperateLogs(Long userId, Long estabId, Integer success, String moduleCode, String requestPath,
                                           LocalDateTime startTime, LocalDateTime endTime, Integer limit);

    /**
     * 根据日志ID查询操作日志
     *
     * @param logId 日志ID
     * @return 操作日志
     */
    OperateLogEntity findOperateLogById(Long logId);

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
    List<ErrorLogEntity> listErrorLogs(String serviceName, Integer errorLevel, String requestPath,
                                       LocalDateTime startTime, LocalDateTime endTime, Integer limit);

    /**
     * 根据日志ID查询错误日志
     *
     * @param logId 日志ID
     * @return 错误日志
     */
    ErrorLogEntity findErrorLogById(Long logId);

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
    List<NotifyLogEntity> listNotifyLogs(Integer channelType, String sceneCode, String receiver, Integer sendStatus,
                                         LocalDateTime startTime, LocalDateTime endTime, Integer limit);

    /**
     * 根据日志ID查询通知日志
     *
     * @param logId 日志ID
     * @return 通知日志
     */
    NotifyLogEntity findNotifyLogById(Long logId);
}
