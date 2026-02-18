package cn.refinex.user.interfaces.controller;

import cn.refinex.api.user.model.dto.EstabResolveRequest;
import cn.refinex.api.user.model.dto.UserAuthSubjectDTO;
import cn.refinex.api.user.model.dto.UserAuthSubjectQuery;
import cn.refinex.api.user.model.dto.UserInfoQuery;
import cn.refinex.api.user.model.dto.UserLoginFailureCommand;
import cn.refinex.api.user.model.dto.UserLoginSuccessCommand;
import cn.refinex.api.user.model.dto.UserRegisterCommand;
import cn.refinex.api.user.model.dto.UserRegisterResult;
import cn.refinex.api.user.model.vo.UserInfo;
import cn.refinex.base.response.SingleResponse;
import cn.refinex.user.application.dto.AuthSubjectDTO;
import cn.refinex.user.application.dto.RegisterUserResultDTO;
import cn.refinex.user.application.dto.UserInfoDTO;
import cn.refinex.user.application.service.UserApplicationService;
import cn.refinex.user.interfaces.assembler.UserApiAssembler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户内部接口
 *
 * @author refinex
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserApplicationService userApplicationService;
    private final UserApiAssembler userApiAssembler;

    /**
     * 注册用户
     *
     * @param command 注册命令
     * @return 注册结果
     */
    @PostMapping("/register")
    public SingleResponse<UserRegisterResult> register(@Valid @RequestBody UserRegisterCommand command) {
        RegisterUserResultDTO resultDto = userApplicationService.register(userApiAssembler.toRegisterCommand(command));
        return SingleResponse.of(userApiAssembler.toRegisterResult(resultDto));
    }

    /**
     * 解析企业ID
     *
     * @param request 解析请求
     * @return 企业ID
     */
    @PostMapping("/estab/resolve")
    public SingleResponse<Long> resolveEstab(@Valid @RequestBody EstabResolveRequest request) {
        Long estabId = userApplicationService.resolveEstabId(userApiAssembler.toResolveEstabCommand(request));
        return SingleResponse.of(estabId);
    }

    /**
     * 查询认证主题
     *
     * @param query 查询条件
     * @return 认证主题
     */
    @PostMapping("/auth/subject")
    public SingleResponse<UserAuthSubjectDTO> authSubject(@Valid @RequestBody UserAuthSubjectQuery query) {
        AuthSubjectDTO subjectDto = userApplicationService.queryAuthSubject(userApiAssembler.toQueryAuthSubjectCommand(query));
        return SingleResponse.of(subjectDto == null ? null : userApiAssembler.toAuthSubject(subjectDto));
    }

    /**
     * 标记登录成功
     *
     * @param command 登录成功命令
     * @return 操作结果
     */
    @PostMapping("/auth/login/success")
    public SingleResponse<Void> loginSuccess(@Valid @RequestBody UserLoginSuccessCommand command) {
        userApplicationService.markLoginSuccess(userApiAssembler.toUpdateLoginSuccessCommand(command));
        return SingleResponse.of(null);
    }

    /**
     * 标记登录失败
     *
     * @param command 登录失败命令
     * @return 操作结果
     */
    @PostMapping("/auth/login/failure")
    public SingleResponse<Void> loginFailure(@Valid @RequestBody UserLoginFailureCommand command) {
        userApplicationService.markLoginFailure(userApiAssembler.toUpdateLoginFailureCommand(command));
        return SingleResponse.of(null);
    }

    /**
     * 查询用户信息
     *
     * @param query 查询条件
     * @return 用户信息
     */
    @PostMapping("/info")
    public SingleResponse<UserInfo> info(@Valid @RequestBody UserInfoQuery query) {
        UserInfoDTO userInfoDto = userApplicationService.queryUserInfo(userApiAssembler.toQueryUserInfoCommand(query));
        return SingleResponse.of(userApiAssembler.toUserInfo(userInfoDto));
    }
}
