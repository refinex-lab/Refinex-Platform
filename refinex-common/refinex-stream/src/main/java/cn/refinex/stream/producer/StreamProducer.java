package cn.refinex.stream.producer;

import cn.refinex.stream.constant.StreamConstant;
import cn.refinex.stream.domain.StreamEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * 消息生产者工具
 * <p>
 * 封装 Spring Cloud Stream 的 StreamBridge，提供便捷的发送方法。
 * 自动将业务数据封装为 {@link StreamEnvelope} 以支持统一幂等处理。
 *
 * @author refinex
 */
@Slf4j
@RequiredArgsConstructor
public class StreamProducer {

    private final StreamBridge streamBridge;

    /**
     * 发送普通消息
     *
     * @param bindingName Binding 名称 (对应配置文件中的 output binding)
     * @param tag         消息 Tag
     * @param payload     业务消息内容 (通常为 JSON 字符串)
     * @return 发送结果 true/false
     */
    public boolean send(String bindingName, String tag, String payload) {
        StreamEnvelope envelope = StreamEnvelope.of(payload);

        if (log.isDebugEnabled()) {
            log.debug("Sending message to [{}]: tag={}, payload={}", bindingName, tag, payload);
        }

        Message<StreamEnvelope> message = MessageBuilder.withPayload(envelope)
                .setHeader(StreamConstant.ROCKET_TAGS, tag)
                .build();

        return doSend(bindingName, message);
    }

    /**
     * 发送延迟消息
     *
     * @param bindingName Binding 名称
     * @param tag         消息 Tag
     * @param payload     业务消息内容
     * @param delayLevel  延迟级别 (1=1s, 2=5s, 3=10s, 4=30s...)
     * @return 发送结果
     */
    public boolean send(String bindingName, String tag, String payload, int delayLevel) {
        StreamEnvelope envelope = StreamEnvelope.of(payload);

        Message<StreamEnvelope> message = MessageBuilder.withPayload(envelope)
                .setHeader(StreamConstant.ROCKET_TAGS, tag)
                .setHeader(MessageConst.PROPERTY_DELAY_TIME_LEVEL, delayLevel)
                .build();

        return doSend(bindingName, message);
    }

    /**
     * 发送带自定义 Header 的消息
     *
     * @param bindingName Binding 名称
     * @param tag         消息 Tag
     * @param payload     业务消息内容
     * @param headerKey   自定义 Header 键
     * @param headerValue 自定义 Header 值
     * @return 发送结果 true/false
     */
    public boolean send(String bindingName, String tag, String payload, String headerKey, String headerValue) {
        StreamEnvelope envelope = StreamEnvelope.of(payload);

        Message<StreamEnvelope> message = MessageBuilder.withPayload(envelope)
                .setHeader(StreamConstant.ROCKET_TAGS, tag)
                .setHeader(headerKey, headerValue)
                .build();

        return doSend(bindingName, message);
    }

    /**
     * 发送消息
     *
     * @param bindingName Binding 名称
     * @param message     消息
     * @return 发送结果 true/false
     */
    private boolean doSend(String bindingName, Message<?> message) {
        try {
            boolean result = streamBridge.send(bindingName, message);
            log.info("Message sent to [{}]: result={}, id={}", bindingName, result, message.getHeaders().getId());
            return result;
        } catch (Exception e) {
            log.error("Failed to send message to [{}]", bindingName, e);
            return false;
        }
    }
}
