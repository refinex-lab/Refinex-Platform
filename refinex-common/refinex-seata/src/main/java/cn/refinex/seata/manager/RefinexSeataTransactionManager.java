package cn.refinex.seata.manager;

import cn.refinex.seata.context.SeataTransactionHolder;
import com.google.common.base.Preconditions;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.rm.datasource.DataSourceProxy;
import io.seata.tm.api.GlobalTransaction;
import io.seata.tm.api.GlobalTransactionContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.transaction.core.ResourceDataSource;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.spi.ShardingSphereTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Refinex 自定义 Seata AT 事务管理器
 * <p>
 * 适配 ShardingSphere 5.x + Seata 2.x。
 * 核心职责：
 * 1. 拦截 ShardingSphere 数据源，包装为 Seata {@link DataSourceProxy} 以生成 Undo Log。
 * 2. 对接 Seata TM API 控制全局事务生命周期 (Begin/Commit/Rollback)。
 *
 * @author refinex
 */
@Slf4j
public class RefinexSeataTransactionManager implements ShardingSphereTransactionManager {

    /**
     * 数据源缓存
     */
    private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

    /**
     * 默认全局事务超时时间 (秒)
     */
    private static final int DEFAULT_TX_TIMEOUT = 60;

    /**
     * 初始化事务管理器
     *
     * @param databaseType 数据库类型
     * @param resourceDataSources 数据源集合
     * @param providerType 事务管理器提供者类型
     */
    @Override
    public void init(final DatabaseType databaseType, final Collection<ResourceDataSource> resourceDataSources, final String providerType) {
        // 在这里将 ShardingSphere 加载的物理数据源包装为 Seata 的 DataSourceProxy
        // 这样 SQL 执行时才能被 Seata 拦截并生成 Undo Log
        for (ResourceDataSource each : resourceDataSources) {
            dataSourceMap.put(each.getOriginalName(), new DataSourceProxy(each.getDataSource()));
        }
        log.info("Refinex Seata Transaction Manager initialized. Proxy data sources count: {}", dataSourceMap.size());
    }

    /**
     * 获取事务类型
     *
     * @return 事务类型
     */
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.BASE;
    }

    /**
     * 判断当前线程是否处于全局事务中
     *
     * @return 是否处于全局事务中
     */
    @Override
    public boolean isInTransaction() {
        // 判断当前线程是否处于 Seata 全局事务中
        return SeataTransactionHolder.get() != null || RootContext.inGlobalTransaction();
    }

    /**
     * 获取数据源连接
     *
     * @param databaseName 数据库名称
     * @param dataSourceName 数据源名称
     * @return 数据源连接
     * @throws SQLException 连接异常
     */
    @Override
    public Connection getConnection(final String databaseName, final String dataSourceName) throws SQLException {
        // 获取被代理后的数据源连接
        DataSource dataSource = dataSourceMap.get(databaseName + "." + dataSourceName);
        if (dataSource == null) {
            // 兜底：如果没找到代理数据源（理论上不应发生），尝试查找原始数据源或抛错
            throw new SQLException("Seata DataSourceProxy not found for: " + databaseName + "." + dataSourceName);
        }
        return dataSource.getConnection();
    }

    /**
     * 开始全局事务
     */
    @Override
    public void begin() {
        begin(DEFAULT_TX_TIMEOUT);
    }

    /**
     * 开始全局事务
     *
     * @param timeout 超时时间（秒）
     */
    @Override
    @SneakyThrows(TransactionException.class)
    public void begin(final int timeout) {
        if (timeout < 0) {
            throw new TransactionException("Timeout should be more than 0s");
        }

        // 创建或获取当前全局事务
        GlobalTransaction globalTransaction = GlobalTransactionContext.getCurrentOrCreate();
        globalTransaction.begin(timeout * 1000);

        // 绑定到本地上下文
        SeataTransactionHolder.set(globalTransaction);

        log.debug("Seata global transaction begun. XID: {}", globalTransaction.getXid());
    }

    /**
     * 提交全局事务
     *
     * @param rollbackOnly 标记是否处于只读状态
     */
    @Override
    @SneakyThrows(TransactionException.class)
    public void commit(final boolean rollbackOnly) {
        GlobalTransaction transaction = SeataTransactionHolder.get();
        Preconditions.checkNotNull(transaction, "Seata global transaction is null during commit");

        try {
            transaction.commit();
            log.debug("Seata global transaction committed. XID: {}", transaction.getXid());
        } finally {
            cleanup();
        }
    }

    /**
     * 回滚全局事务
     */
    @Override
    @SneakyThrows(TransactionException.class)
    public void rollback() {
        GlobalTransaction transaction = SeataTransactionHolder.get();
        Preconditions.checkNotNull(transaction, "Seata global transaction is null during rollback");

        try {
            transaction.rollback();
            log.debug("Seata global transaction rolled back. XID: {}", transaction.getXid());
        } finally {
            cleanup();
        }
    }

    /**
     * 关闭事务管理器
     */
    @Override
    public void close() {
        dataSourceMap.clear();
        SeataTransactionHolder.clear();
    }

    /**
     * 清理当前线程的全局事务上下文
     */
    private void cleanup() {
        SeataTransactionHolder.clear();
        // 如果是手动控制事务，可能需要手动 unbind。但在 Spring 整合模式下，RootContext 通常由 Seata 拦截器管理。
        // 为防万一，清理 RootContext 防止污染
        RootContext.unbind();
    }
}
