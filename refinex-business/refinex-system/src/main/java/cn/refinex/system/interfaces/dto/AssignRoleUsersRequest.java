package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色用户授权请求
 *
 * @author refinex
 */
@Data
public class AssignRoleUsersRequest {

    /**
     * 用户ID列表
     */
    private List<@Positive(message = "用户ID必须大于0") Long> userIds = new ArrayList<>();
}
