package cn.refinex.datasource.handler;

import cn.refinex.api.user.context.CurrentUserProvider;
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
     * 当前登录用户信息提供者（用于基础组件取数）
     */
    private final CurrentUserProvider currentUserProvider;

    /**
     * 构造函数
     *
     * @param currentUserProvider 当前登录用户信息提供者（用于基础组件取数）
     */
    public RefinexMetaObjectHandler(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

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

        // 审计人字段填充
        Long userId = getCurrentUserIdSafely();
        if (userId != null) {
            this.strictInsertFill(metaObject, "createBy", Long.class, userId);
            this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
        }
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

        Long userId = getCurrentUserIdSafely();
        if (userId != null) {
            this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
        }

        // 逻辑删除时记录删除人
        Object deleted = this.getFieldValByName("deleted", metaObject);
        boolean isDeleted = false;
        if (deleted instanceof Number number) {
            isDeleted = number.intValue() == 1;
        } else if (deleted instanceof Boolean bool) {
            isDeleted = bool;
        } else if (deleted != null) {
            isDeleted = "1".equals(deleted.toString());
        }
        if (userId != null && isDeleted) {
            this.strictUpdateFill(metaObject, "deleteBy", Long.class, userId);
        }
    }

    /**
     * 安全获取当前用户 ID
     * <p>
     * 当前用户信息提供者为空时，返回 null。
     * 当前用户信息提供者不为空时，调用其 getCurrentUserId 方法获取用户 ID。
     * 当前用户信息提供者调用 getCurrentUserId 方法时抛出异常时，记录 debug 级别日志并返回 null。
     *
     * @return 当前用户 ID，可能为 null
     */
    private Long getCurrentUserIdSafely() {
        if (currentUserProvider == null) {
            return null;
        }

        try {
            return currentUserProvider.getCurrentUserId();
        } catch (Exception e) {
            log.debug("Failed to resolve current user id for audit fill: {}", e.getMessage());
            return null;
        }
    }
}
