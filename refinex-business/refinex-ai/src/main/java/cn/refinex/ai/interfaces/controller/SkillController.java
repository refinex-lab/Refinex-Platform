package cn.refinex.ai.interfaces.controller;

import cn.refinex.ai.application.command.CreateSkillCommand;
import cn.refinex.ai.application.command.QuerySkillListCommand;
import cn.refinex.ai.application.command.UpdateSkillCommand;
import cn.refinex.ai.application.dto.SkillDTO;
import cn.refinex.ai.application.service.AiApplicationService;
import cn.refinex.ai.infrastructure.config.ReactiveLoginUserHolder;
import cn.refinex.ai.interfaces.assembler.AiApiAssembler;
import cn.refinex.ai.interfaces.dto.SkillCreateRequest;
import cn.refinex.ai.interfaces.dto.SkillListQuery;
import cn.refinex.ai.interfaces.dto.SkillUpdateRequest;
import cn.refinex.ai.interfaces.vo.SkillVO;
import cn.refinex.base.response.PageResponse;
import cn.refinex.web.vo.PageResult;
import cn.refinex.web.vo.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * AI技能管理
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
public class SkillController {

    private final AiApplicationService aiApplicationService;
    private final AiApiAssembler aiApiAssembler;

    /**
     * 查询技能分页列表
     *
     * @param query    查询参数
     * @param exchange 当前请求上下文
     * @return 技能分页列表
     */
    @GetMapping
    public Mono<PageResult<SkillVO>> listSkills(@Valid SkillListQuery query,
                                                ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            ReactiveLoginUserHolder.initFromExchange(exchange);
            try {
                QuerySkillListCommand command = aiApiAssembler.toQuerySkillListCommand(query);
                PageResponse<SkillDTO> skills = aiApplicationService.listSkills(command);
                return PageResult.success(
                        aiApiAssembler.toSkillVoList(skills.getData()),
                        skills.getTotal(),
                        skills.getCurrentPage(),
                        skills.getPageSize()
                );
            } finally {
                ReactiveLoginUserHolder.clear();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询全部技能（不分页，用于下拉选择）
     *
     * @param status   状态（0-禁用，1-启用）
     * @param exchange 当前请求上下文
     * @return 全部技能列表
     */
    @GetMapping("/all")
    public Mono<Result<List<SkillVO>>> listAllSkills(@RequestParam(required = false) Integer status,
                                                     ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            ReactiveLoginUserHolder.initFromExchange(exchange);
            try {
                List<SkillDTO> skills = aiApplicationService.listAllSkills(status);
                return Result.success(aiApiAssembler.toSkillVoList(skills));
            } finally {
                ReactiveLoginUserHolder.clear();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询技能详情（含toolIds）
     *
     * @param skillId 技能ID
     * @return 技能详情
     */
    @GetMapping("/{skillId}")
    public Mono<Result<SkillVO>> getSkill(@PathVariable @Positive(message = "技能ID必须大于0") Long skillId) {
        return Mono.fromCallable(() -> {
            SkillDTO skill = aiApplicationService.getSkill(skillId);
            return Result.success(aiApiAssembler.toSkillVo(skill));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 创建技能
     *
     * @param request  创建技能请求参数
     * @param exchange 当前请求上下文
     * @return 创建的技能详情
     */
    @PostMapping
    public Mono<Result<SkillVO>> createSkill(@Valid @RequestBody SkillCreateRequest request,
                                             ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            ReactiveLoginUserHolder.initFromExchange(exchange);
            try {
                CreateSkillCommand command = aiApiAssembler.toCreateSkillCommand(request);
                SkillDTO created = aiApplicationService.createSkill(command);
                return Result.success(aiApiAssembler.toSkillVo(created));
            } finally {
                ReactiveLoginUserHolder.clear();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 更新技能
     *
     * @param skillId 技能ID
     * @param request 更新技能请求参数
     * @return 更新后的技能详情
     */
    @PutMapping("/{skillId}")
    public Mono<Result<SkillVO>> updateSkill(
            @PathVariable @Positive(message = "技能ID必须大于0") Long skillId,
            @Valid @RequestBody SkillUpdateRequest request) {
        return Mono.fromCallable(() -> {
            UpdateSkillCommand command = aiApiAssembler.toUpdateSkillCommand(request);
            command.setSkillId(skillId);
            SkillDTO updated = aiApplicationService.updateSkill(command);
            return Result.success(aiApiAssembler.toSkillVo(updated));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 删除技能（级联删除SkillTool）
     *
     * @param skillId 技能ID
     */
    @DeleteMapping("/{skillId}")
    public Mono<Result<Void>> deleteSkill(@PathVariable @Positive(message = "技能ID必须大于0") Long skillId) {
        return Mono.fromCallable(() -> {
            aiApplicationService.deleteSkill(skillId);
            return Result.<Void>success();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
