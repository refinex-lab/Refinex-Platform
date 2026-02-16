package cn.refinex.auth.domain.entity;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据资源接口
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scr_drs_interface")
public class ScrDrsInterface extends BaseEntity {

    private Long drsId;

    private String interfaceCode;

    private String interfaceName;

    private String httpMethod;

    private String pathPattern;

    private String permissionKey;

    private Integer status;

    private Integer sort;
}
