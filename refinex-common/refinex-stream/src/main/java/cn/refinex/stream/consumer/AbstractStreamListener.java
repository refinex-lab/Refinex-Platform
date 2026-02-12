package cn.refinex.stream.consumer;

import cn.refinex.stream.constant.StreamConstant;
import cn.refinex.stream.domain.StreamEnvelope;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

/**
 * 消息消费者基类
 * <p>
 * 提供从 Message 中提取业务对象及元数据的辅助方法。
 *
 * @author refinex
 */
@Slf4j
public abstract class AbstractStreamListener {

    /**
     * 提取并反序列化消息体
     *
     * @param message 接收到的 Spring Message 对象 (Payload 为 StreamEnvelope)
     * @param clazz   目标类型 Class
     * @param <T>     目标类型泛型
     * @return 反序列化后的业务对象
     */
    protected <T> T extractPayload(Message<StreamEnvelope> message, Class<T> clazz) {
        StreamEnvelope envelope = message.getPayload();
        MessageHeaders headers = message.getHeaders();

        String msgId = headers.get(StreamConstant.ROCKET_MQ_MESSAGE_ID, String.class);
        String topic = headers.get(StreamConstant.ROCKET_MQ_TOPIC, String.class);
        String tag = headers.get(StreamConstant.ROCKET_TAGS, String.class);

        log.info("Received Message: topic={}, tag={}, msgId={}, identifier={}", topic, tag, msgId, envelope.getIdentifier());

        try {
            // 解析内部业务 JSON
            return JSON.parseObject(envelope.getPayload(), clazz);
        } catch (Exception e) {
            log.error("Failed to deserialize message payload. identifier={}", envelope.getIdentifier(), e);
            throw new RuntimeException("Message deserialization failed", e);
        }
    }
}
