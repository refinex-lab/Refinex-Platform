package cn.refinex.api.user.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户管理企业归属 DTO
 *
 * @author refinex
 */
@Data
public class UserManageEstabDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 是否管理员
     */
    private Boolean admin;

    /**
     * 是否当前活跃企业
     */
    private Boolean current;
}
