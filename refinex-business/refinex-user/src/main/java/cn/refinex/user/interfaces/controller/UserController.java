package cn.refinex.user.interfaces.controller;

import cn.refinex.api.user.context.CurrentUserProvider;
import cn.refinex.api.user.model.vo.UserInfo;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.exception.SystemException;
import cn.refinex.base.exception.code.BizErrorCode;
import cn.refinex.base.response.MultiResponse;
import cn.refinex.base.response.SingleResponse;
import cn.refinex.user.application.command.ChangePasswordCommand;
import cn.refinex.user.application.command.QueryUserInfoCommand;
import cn.refinex.user.application.command.UploadUserAvatarCommand;
import cn.refinex.user.application.command.UpdateUserProfileCommand;
import cn.refinex.user.application.dto.UserAccountDTO;
import cn.refinex.user.application.dto.UserEstabDTO;
import cn.refinex.user.application.dto.UserInfoDTO;
import cn.refinex.user.application.service.UserApplicationService;
import cn.refinex.user.domain.error.UserErrorCode;
import cn.refinex.user.interfaces.assembler.UserApiAssembler;
import cn.refinex.user.interfaces.dto.UserPasswordChangeRequest;
import cn.refinex.user.interfaces.dto.UserProfileUpdateRequest;
import cn.refinex.user.interfaces.vo.UserAccountVO;
import cn.refinex.user.interfaces.vo.UserEstabVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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

    private static final long MAX_AVATAR_SIZE_BYTES = 5L * 1024 * 1024;

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
        return MultiResponse.of(userApiAssembler.toUserEstabVoList(estabs));
    }

    /**
     * 获取当前用户账号信息
     *
     * @return 账号信息
     */
    @GetMapping("/me/account")
    public SingleResponse<UserAccountVO> currentUserAccount() {
        UserAccountDTO accountDto = userApplicationService.queryUserAccountInfo(requireCurrentUserId());
        return SingleResponse.of(userApiAssembler.toUserAccountVo(accountDto));
    }

    /**
     * 上传当前登录用户头像
     *
     * @param file 头像文件
     * @return 用户信息
     */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SingleResponse<UserInfo> uploadAvatar(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("头像文件不能为空", UserErrorCode.INVALID_PARAM);
        }
        if (file.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw new BizException("头像文件大小不能超过 5MB", UserErrorCode.INVALID_PARAM);
        }

        UploadUserAvatarCommand command = new UploadUserAvatarCommand();
        command.setUserId(requireCurrentUserId());
        command.setEstabId(currentUserProvider.getCurrentEstabId());
        command.setOriginalFilename(file.getOriginalFilename());
        command.setContentType(file.getContentType());
        command.setFileSize(file.getSize());

        try (InputStream inputStream = file.getInputStream()) {
            UserInfoDTO userInfoDto = userApplicationService.uploadUserAvatar(command, inputStream);
            return SingleResponse.of(userApiAssembler.toUserInfo(userInfoDto));
        } catch (IOException e) {
            throw new SystemException("头像文件读取失败", e, BizErrorCode.HTTP_SERVER_ERROR);
        }
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
     * 修改当前登录用户密码
     *
     * @param request 修改密码请求
     * @return 操作结果
     */
    @PostMapping("/me/password/change")
    public SingleResponse<Void> changePassword(@Valid @RequestBody UserPasswordChangeRequest request) {
        ChangePasswordCommand command = new ChangePasswordCommand();
        command.setUserId(requireCurrentUserId());
        command.setOldPassword(request.getOldPassword());
        command.setNewPassword(request.getNewPassword());
        userApplicationService.changePassword(command);
        return SingleResponse.of(null);
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

}
