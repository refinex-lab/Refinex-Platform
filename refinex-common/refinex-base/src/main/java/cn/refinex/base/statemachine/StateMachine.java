package cn.refinex.base.statemachine;

/**
 * 状态机（Finite State Machine, FSM）接口定义
 * <p>
 * 用于处理状态流转逻辑，通过输入当前状态和触发事件，计算出下一个目标状态。
 *
 * @param <S> 状态类型（State）
 * @param <E> 事件类型（Event）
 * @author refinex
 */
public interface StateMachine<S, E> {

    /**
     * 执行状态转移
     *
     * @param state 当前状态
     * @param event 触发事件
     * @return 转移后的目标状态
     * @throws cn.refinex.base.exception.BizException 如果转移规则不存在则抛出异常
     */
    S transition(S state, E event);
}
