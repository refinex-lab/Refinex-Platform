package cn.refinex.system.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 企业地址 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("def_estab_address")
public class DefEstabAddressDo extends BaseEntity {

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 地址类型 1注册 2办公 3收货
     */
    private Integer addrType;

    /**
     * 国家代码
     */
    private String countryCode;

    /**
     * 省份代码
     */
    private String provinceCode;

    /**
     * 城市代码
     */
    private String cityCode;

    /**
     * 区县代码
     */
    private String districtCode;

    /**
     * 省份名称
     */
    private String provinceName;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 区县名称
     */
    private String districtName;

    /**
     * 地址1
     */
    private String addressLine1;

    /**
     * 地址2
     */
    private String addressLine2;

    /**
     * 邮编
     */
    private String postalCode;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 是否默认 1是 0否
     */
    private Integer isDefault;

    /**
     * 备注
     */
    private String remark;
}
