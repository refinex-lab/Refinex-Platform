package cn.refinex.auth.domain.entity;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 组织/租户
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("def_estab")
public class DefEstab extends BaseEntity {

    private String estabCode;

    private String estabName;

    private String estabShortName;

    private Integer estabType;

    private Integer status;

    private String industryCode;

    private String sizeRange;

    private Long ownerUserId;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String websiteUrl;

    private String logoUrl;

    private String remark;
}
