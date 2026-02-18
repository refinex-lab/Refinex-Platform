package cn.refinex.user.interfaces.controller;

import cn.refinex.api.user.context.CurrentUserProvider;
import cn.refinex.api.user.model.vo.UserInfo;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.MultiResponse;
import cn.refinex.base.response.SingleResponse;
import cn.refinex.user.application.command.QueryUserInfoCommand;
import cn.refinex.user.application.command.UpdateUserProfileCommand;
import cn.refinex.user.application.dto.UserEstabDTO;
import cn.refinex.user.application.dto.UserInfoDTO;
import cn.refinex.user.application.service.UserApplicationService;
import cn.refinex.user.domain.error.UserErrorCode;
import cn.refinex.user.interfaces.assembler.UserApiAssembler;
import cn.refinex.user.interfaces.dto.UserProfileUpdateRequest;
import cn.refinex.user.interfaces.vo.UserEstabVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户前台接口
 *
 * @author refinex
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserApplicationService userApplicationService;
    private final UserApiAssembler userApiAssembler;
    private final CurrentUserProvider currentUserProvider;

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/me/info")
    public SingleResponse<UserInfo> currentUserInfo() {
        QueryUserInfoCommand command = new QueryUserInfoCommand();
        command.setUserId(requireCurrentUserId());
        command.setEstabId(currentUserProvider.getCurrentEstabId());

        UserInfoDTO userInfoDto = userApplicationService.queryUserInfo(command);
        return SingleResponse.of(userApiAssembler.toUserInfo(userInfoDto));
    }

    /**
     * 获取当前登录用户所属企业列表
     *
     * @return 企业列表
     */
    @GetMapping("/me/estabs")
    public MultiResponse<UserEstabVO> currentUserEstabs() {
        Long userId = requireCurrentUserId();
        Long currentEstabId = currentUserProvider.getCurrentEstabId();
        List<UserEstabDTO> estabs = userApplicationService.listUserEstabs(userId, currentEstabId);
        return MultiResponse.of(toEstabVoList(estabs));
    }

    /**
     * 更新当前登录用户资料
     *
     * @param request 更新请求
     * @return 用户信息
     */
    @PutMapping("/me/profile")
    public SingleResponse<UserInfo> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        UpdateUserProfileCommand command = new UpdateUserProfileCommand();
        command.setUserId(requireCurrentUserId());
        command.setEstabId(currentUserProvider.getCurrentEstabId());
        command.setDisplayName(request.getDisplayName());
        command.setNickname(request.getNickname());
        command.setAvatarUrl(request.getAvatarUrl());
        command.setGender(request.getGender());
        command.setBirthday(request.getBirthday());

        UserInfoDTO userInfoDto = userApplicationService.updateUserProfile(command);
        return SingleResponse.of(userApiAssembler.toUserInfo(userInfoDto));
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    private Long requireCurrentUserId() {
        Long userId = currentUserProvider.getCurrentUserId();
        if (userId == null) {
            throw new BizException(UserErrorCode.USER_NOT_FOUND);
        }
        return userId;
    }

    /**
     * 将用户企业DTO列表转换为用户企业VO列表
     *
     * @param estabs 用户企业DTO列表
     * @return 用户企业VO列表
     */
    private List<UserEstabVO> toEstabVoList(List<UserEstabDTO> estabs) {
        List<UserEstabVO> result = new ArrayList<>();
        for (UserEstabDTO dto : estabs) {
            UserEstabVO vo = new UserEstabVO();
            vo.setEstabId(dto.getEstabId());
            vo.setEstabCode(dto.getEstabCode());
            vo.setEstabName(dto.getEstabName());
            vo.setEstabShortName(dto.getEstabShortName());
            vo.setLogoUrl(dto.getLogoUrl());
            vo.setEstabType(dto.getEstabType());
            vo.setAdmin(dto.getAdmin());
            vo.setCurrent(dto.getCurrent());
            result.add(vo);
        }
        return result;
    }
}
