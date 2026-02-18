package cn.refinex.user.interfaces.assembler;

import cn.refinex.api.user.model.dto.*;
import cn.refinex.api.user.model.vo.UserInfo;
import cn.refinex.user.application.command.*;
import cn.refinex.user.application.dto.AuthSubjectDTO;
import cn.refinex.user.application.dto.RegisterUserResultDTO;
import cn.refinex.user.application.dto.UserAccountDTO;
import cn.refinex.user.application.dto.UserEstabDTO;
import cn.refinex.user.application.dto.UserInfoDTO;
import cn.refinex.user.interfaces.vo.UserAccountVO;
import cn.refinex.user.interfaces.vo.UserEstabVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 用户接口装配器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface UserApiAssembler {

    /**
     * 用户注册参数转换
     *
     * @param command 用户注册参数
     * @return 注册用户命令
     */
    RegisterUserCommand toRegisterCommand(UserRegisterCommand command);

    /**
     * 注册结果转换
     *
     * @param result 注册结果
     * @return 用户注册结果
     */
    UserRegisterResult toRegisterResult(RegisterUserResultDTO result);

    /**
     * 查询认证主题参数转换
     *
     * @param query 查询认证主题参数
     * @return 查询认证主题命令
     */
    QueryAuthSubjectCommand toQueryAuthSubjectCommand(UserAuthSubjectQuery query);

    /**
     * 认证主题转换
     *
     * @param subjectDto 认证主题
     * @return 用户认证主题
     */
    UserAuthSubjectDTO toAuthSubject(AuthSubjectDTO subjectDto);

    /**
     * 解析建立请求参数转换
     *
     * @param request 解析建立请求参数
     * @return 解析建立命令
     */
    ResolveEstabCommand toResolveEstabCommand(EstabResolveRequest request);

    /**
     * 更新登录成功参数转换
     *
     * @param command 更新登录成功参数
     * @return 更新登录成功命令
     */
    UpdateLoginSuccessCommand toUpdateLoginSuccessCommand(UserLoginSuccessCommand command);

    /**
     * 更新登录失败参数转换
     *
     * @param command 更新登录失败参数
     * @return 更新登录失败命令
     */
    UpdateLoginFailureCommand toUpdateLoginFailureCommand(UserLoginFailureCommand command);

    /**
     * 重置密码参数转换
     *
     * @param command 重置密码参数
     * @return 重置密码命令
     */
    ResetPasswordCommand toResetPasswordCommand(UserResetPasswordCommand command);

    /**
     * 查询用户信息参数转换
     *
     * @param query 查询用户信息参数
     * @return 查询用户信息命令
     */
    QueryUserInfoCommand toQueryUserInfoCommand(UserInfoQuery query);

    /**
     * 用户信息转换
     *
     * @param userInfoDto 用户信息
     * @return 用户信息
     */
    UserInfo toUserInfo(UserInfoDTO userInfoDto);

    /**
     * 用户账号信息转换
     *
     * @param userAccountDto 用户账号信息DTO
     * @return 用户账号信息VO
     */
    UserAccountVO toUserAccountVo(UserAccountDTO userAccountDto);

    /**
     * 用户企业信息转换
     *
     * @param userEstabDtos 用户企业信息DTO列表
     * @return 用户企业信息VO列表
     */
    List<UserEstabVO> toUserEstabVoList(List<UserEstabDTO> userEstabDtos);
}
