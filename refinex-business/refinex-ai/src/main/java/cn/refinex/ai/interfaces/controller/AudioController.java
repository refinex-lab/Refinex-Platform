package cn.refinex.ai.interfaces.controller;

import cn.refinex.ai.application.command.TtsCommand;
import cn.refinex.ai.application.service.AudioApplicationService;
import cn.refinex.ai.interfaces.dto.TtsRequest;
import cn.refinex.satoken.helper.LoginUserHelper;
import cn.refinex.web.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
     * @param request TTS 请求
     * @return 音频 CDN URL
     */
    @PostMapping("/tts")
    public Mono<Result<String>> tts(@Valid @RequestBody TtsRequest request) {
        return Mono.fromCallable(() -> {
            TtsCommand command = new TtsCommand();
            command.setText(request.getText());
            command.setModelId(request.getModelId());
            command.setVoice(request.getVoice());
            command.setSpeed(request.getSpeed());
            command.setEstabId(LoginUserHelper.getEstabId());
            command.setUserId(LoginUserHelper.getUserId());
            String audioUrl = audioApplicationService.textToSpeech(command);
            return Result.success(audioUrl);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
