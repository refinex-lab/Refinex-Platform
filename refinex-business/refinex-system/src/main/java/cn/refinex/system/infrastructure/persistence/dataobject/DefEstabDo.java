package cn.refinex.system.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业/组织 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("def_estab")
public class DefEstabDo extends BaseEntity {

    /**
     * 组织编码
     */
    private String estabCode;

    /**
     * 组织名称
     */
    private String estabName;

    /**
     * 组织简称
     */
    private String estabShortName;

    /**
     * 组织类型 0平台 1租户 2合作方
     */
    private Integer estabType;

    /**
     * 状态 1启用 2停用 3冻结
     */
    private Integer status;

    /**
     * 行业编码
     */
    private String industryCode;

    /**
     * 规模区间
     */
    private String sizeRange;

    /**
     * 主负责人用户ID
     */
    private Long ownerUserId;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 联系人邮箱
     */
    private String contactEmail;

    /**
     * 官网地址
     */
    private String websiteUrl;

    /**
     * Logo地址
     */
    private String logoUrl;

    /**
     * 备注
     */
    private String remark;
}
