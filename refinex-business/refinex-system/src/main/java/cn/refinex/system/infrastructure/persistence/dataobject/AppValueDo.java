package cn.refinex.system.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 值集明细 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_value")
public class AppValueDo extends BaseEntity {

    /**
     * 值集编码
     */
    private String setCode;

    /**
     * 值编码
     */
    private String valueCode;

    /**
     * 值名称
     */
    private String valueName;

    /**
     * 值描述
     */
    private String valueDesc;

    /**
     * 状态 1启用 0停用
     */
    private Integer status;

    /**
     * 是否默认 1是 0否
     */
    private Integer isDefault;

    /**
     * 排序
     */
    private Integer sort;
}
