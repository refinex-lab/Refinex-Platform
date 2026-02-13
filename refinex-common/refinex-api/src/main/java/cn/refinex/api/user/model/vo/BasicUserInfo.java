package cn.refinex.api.user.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户基础信息 VO
 * <p>
 * 用于列表展示、评论区等公开场景，仅包含非敏感字段。
 *
 * @author refinex
 */
@Data
public class BasicUserInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像 URL
     */
    private String profilePhotoUrl;
}
