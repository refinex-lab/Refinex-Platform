package cn.refinex.system.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作定义 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("def_op")
public class DefOpDo extends BaseEntity {

    /**
     * 操作编码
     */
    private String opCode;

    /**
     * 操作名称
     */
    private String opName;

    /**
     * 操作说明
     */
    private String opDesc;

    /**
     * 是否内置 1是 0否
     */
    private Integer isBuiltin;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 排序(升序)
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;
}
