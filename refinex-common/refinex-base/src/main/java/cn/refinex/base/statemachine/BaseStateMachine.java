package cn.refinex.base.statemachine;

import cn.refinex.base.exception.BizException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static cn.refinex.base.exception.code.BizErrorCode.STATE_MACHINE_TRANSITION_FAILED;

/**
 * 状态机基础实现类
 * <p>
 * 提供基于内存映射的状态转移表，支持子类通过 {@link #registerTransition} 注册流转规则。
 *
 * @param <S> 状态类型
 * @param <E> 事件类型
 * @author refinex
 */
@Slf4j
public abstract class BaseStateMachine<S, E> implements StateMachine<S, E> {

    /**
     * 状态转移映射表
     * Key 结构: "源状态_事件"
     */
    private final Map<String, S> transitionTable = new ConcurrentHashMap<>();

    /**
     * 注册状态转移规则
     *
     * @param source 初始状态
     * @param event  触发事件
     * @param target 目标状态
     */
    protected void registerTransition(S source, E event, S target) {
        String key = buildTransitionKey(source, event);
        if (transitionTable.containsKey(key)) {
            log.warn("Duplicate transition registered: {} + {} -> {}", source, event, target);
        }
        transitionTable.put(key, target);
    }

    /**
     * 执行状态转移
     *
     * @param state 当前状态
     * @param event 触发事件
     * @return 转移后的目标状态
     * @throws cn.refinex.base.exception.BizException 如果转移规则不存在则抛出异常
     */
    @Override
    public S transition(S state, E event) {
        String key = buildTransitionKey(state, event);
        S target = transitionTable.get(key);

        if (target == null) {
            log.error("Invalid state transition: current state [{}], event [{}]", state, event);
            throw new BizException(
                    String.format("State transition failed: %s with event %s is not allowed", state, event),
                    STATE_MACHINE_TRANSITION_FAILED
            );
        }

        return target;
    }

    /**
     * 构建转移表的唯一标识 Key
     *
     * @param state 状态
     * @param event 事件
     * @return 组合 Key
     */
    protected String buildTransitionKey(S state, E event) {
        // 使用 Objects.toString 确保 null 安全，并使用特有分隔符
        return Objects.toString(state) + "@@@" + Objects.toString(event);
    }
}
