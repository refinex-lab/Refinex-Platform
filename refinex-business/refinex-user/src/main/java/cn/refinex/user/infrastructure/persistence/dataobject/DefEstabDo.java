package cn.refinex.user.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 组织 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("def_estab")
public class DefEstabDo extends BaseEntity {

    /**
     * 企业代码
     */
    private String estabCode;

    /**
     * 企业名称
     */
    private String estabName;

    /**
     * 企业简称
     */
    private String estabShortName;

    /**
     * 企业类型
     */
    private Integer estabType;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 行业代码
     */
    private String industryCode;

    /**
     * 企业规模范围
     */
    private String sizeRange;

    /**
     * 企业所有者用户ID
     */
    private Long ownerUserId;

    /**
     * 联系人姓名
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
     * 网站Url
     */
    private String websiteUrl;

    /**
     * 企业logo Url
     */
    private String logoUrl;

    /**
     * 备注
     */
    private String remark;
}
