package cn.refinex.ai.application.service;

import cn.refinex.ai.application.command.TtsCommand;
import cn.refinex.ai.domain.error.AiErrorCode;
import cn.refinex.ai.domain.model.entity.UsageLogEntity;
import cn.refinex.ai.domain.model.enums.RequestType;
import cn.refinex.ai.domain.repository.AiRepository;
import cn.refinex.ai.infrastructure.ai.SpeechModelRouter;
import cn.refinex.base.exception.BizException;
import cn.refinex.file.api.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.UUID;

/**
 * 音频应用服务
 * <p>
 * 处理 TTS（文字转语音）逻辑，独立于对话服务。
 *
 * @author refinex
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AudioApplicationService {

    private final SpeechModelRouter speechModelRouter;
    private final FileService fileService;
    private final AiRepository aiRepository;

    /**
     * 文字转语音
     * <p>
     * 同步调用 TTS 模型，生成音频上传 OSS，返回 CDN URL。
     *
     * @param command TTS 命令
     * @return 音频 CDN URL
     */
    public String textToSpeech(TtsCommand command) {
        long startTime = System.currentTimeMillis();
        try {
            TextToSpeechModel speechModel = command.getModelId() != null
                    ? speechModelRouter.resolve(command.getEstabId(), command.getModelId())
                    : speechModelRouter.resolveDefault(command.getEstabId());

            OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                    .voice(command.getVoice() != null ? command.getVoice() : "alloy")
                    .speed(command.getSpeed() != null ? command.getSpeed() : 1.0)
                    .build();

            TextToSpeechPrompt prompt = new TextToSpeechPrompt(command.getText(), options);
            TextToSpeechResponse response = speechModel.call(prompt);

            byte[] audioBytes = response.getResult().getOutput();
            if (audioBytes.length == 0) {
                throw new BizException(AiErrorCode.TTS_FAILED);
            }

            String path = String.join("/", "ai-tts", String.valueOf(command.getEstabId()), UUID.randomUUID() + ".mp3");
            String audioUrl = fileService.upload(path, new ByteArrayInputStream(audioBytes));

            recordTtsUsageLog(command, (int) (System.currentTimeMillis() - startTime), true, null);
            return audioUrl;
        } catch (BizException e) {
            recordTtsUsageLog(command, (int) (System.currentTimeMillis() - startTime), false, e.getMessage());
            throw e;
        } catch (Exception e) {
            recordTtsUsageLog(command, (int) (System.currentTimeMillis() - startTime), false, e.getMessage());
            log.error("TTS 调用失败, estabId={}", command.getEstabId(), e);
            throw new BizException(AiErrorCode.TTS_FAILED);
        }
    }

    /**
     * 记录 TTS 调用日志
     *
     * @param command      TTS 命令
     * @param durationMs   耗时（毫秒）
     * @param success      是否成功
     * @param errorMessage 错误信息
     */
    private void recordTtsUsageLog(TtsCommand command, int durationMs, boolean success, String errorMessage) {
        try {
            UsageLogEntity usageLog = new UsageLogEntity();
            usageLog.setEstabId(command.getEstabId());
            usageLog.setUserId(command.getUserId());
            usageLog.setModelId(command.getModelId());
            usageLog.setRequestType(RequestType.TTS.getCode());
            usageLog.setDurationMs(durationMs);
            usageLog.setSuccess(success ? 1 : 0);
            usageLog.setErrorMessage(errorMessage);
            aiRepository.insertUsageLog(usageLog);
        } catch (Exception e) {
            log.error("记录TTS调用日志失败", e);
        }
    }
}
