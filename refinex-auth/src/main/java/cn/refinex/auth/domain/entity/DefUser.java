package cn.refinex.auth.domain.entity;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户主档
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("def_user")
public class DefUser extends BaseEntity {

    private String userCode;

    private String username;

    private String displayName;

    private String nickname;

    private String avatarUrl;

    private Integer gender;

    private LocalDate birthday;

    private Integer userType;

    private Integer status;

    private Long primaryEstabId;

    private String primaryPhone;

    private Integer phoneVerified;

    private String primaryEmail;

    private Integer emailVerified;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private Integer loginFailCount;

    private LocalDateTime lockUntil;
}
