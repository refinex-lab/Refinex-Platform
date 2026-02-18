package cn.refinex.user.infrastructure.persistence.dataobject;

import lombok.Data;

/**
 * 用户-企业关系联表结果
 *
 * @author refinex
 */
@Data
public class UserEstabJoinDo {

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 企业编码
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
     * 企业Logo
     */
    private String logoUrl;

    /**
     * 企业类型
     */
    private Integer estabType;

    /**
     * 是否企业管理员（1是 0否）
     */
    private Integer isAdmin;
}
