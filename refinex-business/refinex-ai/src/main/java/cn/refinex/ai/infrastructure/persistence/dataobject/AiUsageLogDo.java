package cn.refinex.ai.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * AI调用日志 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_usage_log")
public class AiUsageLogDo extends BaseEntity {

    /**
     * 组织ID
     */
    private Long estabId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话ID(可选)
     */
    private String conversationId;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 请求类型(CHAT/EMBEDDING/IMAGE_GEN/TTS/STT/RERANK)
     */
    private String requestType;

    /**
     * 输入token数
     */
    private Integer inputTokens;

    /**
     * 输出token数
     */
    private Integer outputTokens;

    /**
     * 总token数
     */
    private Integer totalTokens;

    /**
     * 本次调用费用(美元)
     */
    private BigDecimal totalCost;

    /**
     * 耗时(毫秒)
     */
    private Integer durationMs;

    /**
     * 结束原因(stop/length/tool_calls/error)
     */
    private String finishReason;

    /**
     * 是否成功 1成功 0失败
     */
    private Integer success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 扩展信息
     */
    private String extJson;
}
