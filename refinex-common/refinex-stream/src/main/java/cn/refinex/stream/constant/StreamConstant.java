package cn.refinex.stream.constant;

import lombok.experimental.UtilityClass;

/**
 * 消息队列常量定义
 *
 * @author refinex
 */
@UtilityClass
public class StreamConstant {

    /**
     * Header Key: RocketMQ 消息 ID
     */
    public static final String ROCKET_MQ_MESSAGE_ID = "ROCKET_MQ_MESSAGE_ID";

    /**
     * Header Key: 消息 Topic
     */
    public static final String ROCKET_MQ_TOPIC = "ROCKET_MQ_TOPIC";

    /**
     * Header Key: 消息 Tags
     */
    public static final String ROCKET_TAGS = "ROCKET_TAGS";

    /**
     * 延迟级别 (RocketMQ 4.x 机制)
     * 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     */
    public static final int DELAY_LEVEL_30_S = 4;
    public static final int DELAY_LEVEL_1_M = 5;
}
