package cn.refinex.ai.application.command;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询模型列表命令
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryModelListCommand extends PageRequest {

    /**
     * 供应商ID
     */
    private Long providerId;

    /**
     * 模型类型 1聊天 2嵌入 3图像生成 4语音转文字 5文字转语音 6重排序 7内容审核
     */
    private Integer modelType;

    /**
     * 状态 1启用 0停用
     */
    private Integer status;

    /**
     * 关键词(匹配模型名称/编码)
     */
    private String keyword;
}
