package cn.refinex.seata.context;

import io.seata.tm.api.GlobalTransaction;
import lombok.experimental.UtilityClass;

/**
 * Seata 全局事务上下文持有者
 * <p>
 * 使用 ThreadLocal 存储当前线程绑定的 GlobalTransaction 对象，
 * 确保 ShardingSphere 在 begin/commit/rollback 过程中操作的是同一个事务对象。
 *
 * @author refinex
 */
@UtilityClass
public class SeataTransactionHolder {

    private static final ThreadLocal<GlobalTransaction> CONTEXT = new ThreadLocal<>();

    /**
     * 设置当前全局事务
     *
     * @param transaction 当前全局事务
     */
    public static void set(GlobalTransaction transaction) {
        CONTEXT.set(transaction);
    }

    /**
     * 获取当前全局事务
     *
     * @return 当前全局事务
     */
    public static GlobalTransaction get() {
        return CONTEXT.get();
    }

    /**
     * 清理上下文
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
