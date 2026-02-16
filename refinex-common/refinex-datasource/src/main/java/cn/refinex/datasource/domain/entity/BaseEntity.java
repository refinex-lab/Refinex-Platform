package cn.refinex.datasource.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据库实体基类
 * <p>
 * 包含主键、逻辑删除、乐观锁及审计字段。
 * 建议所有 PO/DO 对象继承此类。
 *
 * @author refinex
 */
@Getter
@Setter
@ToString
public class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID (自增策略)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建人用户ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 更新人用户ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 删除人用户ID
     */
    @TableField(fill = FieldFill.UPDATE)
    private Long deleteBy;

    /**
     * 逻辑删除标识
     * 0: 未删除, 1: 已删除 (推荐使用 Integer 而非 Boolean 以适配更多 DB 场景)
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /**
     * 乐观锁版本号
     * 需配合 OptimisticLockerInnerInterceptor 使用
     */
    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer lockVersion;

    /**
     * 创建时间
     * 使用 LocalDateTime 替代 Date
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtModified;
}
