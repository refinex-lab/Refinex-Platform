package cn.refinex.datasource.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 字段自动填充处理器
 * <p>
 * 在插入或更新时，自动为 gmtCreate, gmtModified 等字段赋值。
 *
 * @author refinex
 */
@Slf4j
public class RefinexMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时填充字段
     *
     * @param metaObject metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 起始版本号默认为 0
        this.strictInsertFill(metaObject, "lockVersion", Integer.class, 0);
        // 逻辑删除默认为 0
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);

        // 时间填充
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "gmtCreate", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "gmtModified", LocalDateTime.class, now);
    }

    /**
     * 更新时填充字段
     *
     * @param metaObject metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时强制修改 gmtModified
        this.strictUpdateFill(metaObject, "gmtModified", LocalDateTime.class, LocalDateTime.now());
    }
}
