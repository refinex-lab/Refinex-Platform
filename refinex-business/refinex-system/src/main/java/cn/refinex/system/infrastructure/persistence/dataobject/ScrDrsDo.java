package cn.refinex.system.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据资源定义 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scr_drs")
public class ScrDrsDo extends BaseEntity {

    /**
     * 数据资源编码
     */
    private String drsCode;

    /**
     * 数据资源名称
     */
    private String drsName;

    /**
     * 所属组织ID(平台级为0)
     */
    private Long ownerEstabId;

    /**
     * 数据归属 0平台 1租户
     */
    private Integer dataOwnerType;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
