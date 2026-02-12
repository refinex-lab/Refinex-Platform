package cn.refinex.datasource.config;

import cn.refinex.datasource.handler.RefinexMetaObjectHandler;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据源及 MyBatis Plus 核心配置
 *
 * 1. 配置 MP 插件（分页、乐观锁、防全表更新）。
 * 2. 配置自动填充处理器。
 * 3. 扫描 Mapper 接口。
 *
 * @author refinex
 */
@Configuration
@MapperScan("cn.refinex.**.infrastructure.mapper") // 扫描规则：cn.refinex.任意模块.infrastructure.mapper
public class DatasourceConfiguration {

    /**
     * 注册元对象字段填充控制器
     * 用于处理 createTime, updateTime 等字段的自动填充
     */
    @Bean
    public RefinexMetaObjectHandler metaObjectHandler() {
        return new RefinexMetaObjectHandler();
    }

    /**
     * MyBatis Plus 插件拦截器配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 乐观锁插件 (需配合 @Version 注解)
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 2. 防全表更新与删除插件 (生产环境安全保障)
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        // 3. 分页插件 (指定数据库类型为 MySQL)
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return interceptor;
    }
}
