package com.realtimevideo.service;

import com.realtimevideo.config.RealtimeVideoProperties;
import com.realtimevideo.domain.bo.VisualAssistRequest;
import com.realtimevideo.domain.vo.VisualAssistResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VisualAssistService {

    private final RealtimeVideoProperties properties;
    private final SpringAiVisualChatClient springAiVisualChatClient;

    public VisualAssistService(
            RealtimeVideoProperties properties,
            SpringAiVisualChatClient springAiVisualChatClient
    ) {
        this.properties = properties;
        this.springAiVisualChatClient = springAiVisualChatClient;
    }

    public VisualAssistResponse assist(VisualAssistRequest request) {
        if (!hasText(request.userText())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户问题不能为空");
        }
        String answer = springAiVisualChatClient.complete(
                properties.getInstructions(),
                request.userText().trim(),
                trimToNull(request.imageDataUrl()),
                trimToNull(request.userHint())
        );
        return new VisualAssistResponse(answer);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
