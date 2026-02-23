package cn.refinex.ai.interfaces.controller;

import cn.refinex.ai.application.command.TtsCommand;
import cn.refinex.ai.application.service.AudioApplicationService;
import cn.refinex.ai.infrastructure.config.ReactiveLoginUserHolder;
import cn.refinex.ai.interfaces.dto.TtsRequest;
import cn.refinex.web.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 音频管理
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/audio")
@RequiredArgsConstructor
public class AudioController {

    private final AudioApplicationService audioApplicationService;

    /**
     * 文字转语音
     *
     * @param request  TTS 请求
     * @param exchange 当前请求上下文
     * @return 音频 CDN URL
     */
    @PostMapping("/tts")
    public Mono<Result<String>> tts(@Valid @RequestBody TtsRequest request,
                                    ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            ReactiveLoginUserHolder.initFromExchange(exchange);
            try {
                TtsCommand command = new TtsCommand();
                command.setText(request.getText());
                command.setModelId(request.getModelId());
                command.setVoice(request.getVoice());
                command.setSpeed(request.getSpeed());
                command.setEstabId(ReactiveLoginUserHolder.getEstabId());
                command.setUserId(ReactiveLoginUserHolder.getUserId());
                String audioUrl = audioApplicationService.textToSpeech(command);
                return Result.success(audioUrl);
            } finally {
                ReactiveLoginUserHolder.clear();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
