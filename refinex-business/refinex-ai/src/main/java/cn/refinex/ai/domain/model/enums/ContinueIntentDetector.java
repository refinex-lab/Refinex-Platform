package cn.refinex.ai.domain.model.enums;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 续写意图检测器
 * <p>
 * 判断用户消息是否为 "继续生成" 意图，用于触发前缀续写（Prefix Continuation）流程。
 *
 * @author refinex
 */
public final class ContinueIntentDetector {

    private static final Set<String> EXACT_MATCHES = Set.of(
            "继续", "接着说", "接着写", "继续写", "继续生成", "接着生成",
            "continue", "go on", "keep going", "carry on"
    );

    private static final Pattern CONTINUE_PATTERN = Pattern.compile(
            "^(请?继续|接着|go\\s+on|continue|keep\\s+going)\\s*[。.!！]?$",
            Pattern.UNICODE_CASE
    );

    private ContinueIntentDetector() {
    }

    /**
     * 判断用户消息是否为续写意图
     *
     * @param message 用户消息
     * @return true 表示用户希望模型继续生成
     */
    public static boolean isContinueIntent(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }

        String trimmed = message.trim().toLowerCase();
        if (EXACT_MATCHES.contains(trimmed)) {
            return true;
        }

        return CONTINUE_PATTERN.matcher(trimmed).matches();
    }
}
