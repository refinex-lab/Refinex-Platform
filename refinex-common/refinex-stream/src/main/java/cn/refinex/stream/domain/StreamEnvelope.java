package cn.refinex.stream.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * 消息信封
 * <p>
 * 用于封装业务消息，提供统一的幂等标识（Identifier）。
 *
 * @author refinex
 */
@Data
@Accessors(chain = true)
public class StreamEnvelope implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识 / 幂等号
     * (通常使用 UUID 或 业务流水号)
     */
    private String identifier;

    /**
     * 业务数据载体 (JSON String)
     */
    private String payload;

    /**
     * 静态构建器 创建一个消息信封
     *
     * @param payload 业务数据载体 (JSON String)
     * @return 消息信封
     */
    public static StreamEnvelope of(String payload) {
        return new StreamEnvelope()
                .setIdentifier(UUID.randomUUID().toString())
                .setPayload(payload);
    }
}
