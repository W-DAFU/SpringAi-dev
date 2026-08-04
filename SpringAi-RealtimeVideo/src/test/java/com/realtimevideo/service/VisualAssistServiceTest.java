package com.realtimevideo.service;

import com.realtimevideo.config.RealtimeVideoProperties;
import com.realtimevideo.domain.bo.VisualAssistRequest;
import com.realtimevideo.domain.vo.VisualAssistResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VisualAssistServiceTest {

    @Test
    void assistDelegatesToSpringAiClientWithTextImageAndHint() {
        RealtimeVideoProperties properties = new RealtimeVideoProperties();
        CapturingSpringAiVisualChatClient client = new CapturingSpringAiVisualChatClient();
        VisualAssistService service = new VisualAssistService(properties, client);

        VisualAssistResponse response = service.assist(new VisualAssistRequest(
                "请帮我看看这个设备为什么没反应",
                "data:image/jpeg;base64,abcd",
                "镜头里是设备正面的指示灯"
        ));

        assertThat(client.instructions).contains("实时视觉对话助手");
        assertThat(client.userText).isEqualTo("请帮我看看这个设备为什么没反应");
        assertThat(client.imageDataUrl).startsWith("data:image/jpeg");
        assertThat(client.userHint).isEqualTo("镜头里是设备正面的指示灯");
        assertThat(response.answer()).isEqualTo("请把镜头靠近设备指示灯。");
    }

    @Test
    void assistRejectsBlankUserText() {
        RealtimeVideoProperties properties = new RealtimeVideoProperties();
        VisualAssistService service = new VisualAssistService(
                properties,
                (instructions, userText, imageDataUrl, userHint) -> "unused"
        );

        assertThatThrownBy(() -> service.assist(new VisualAssistRequest(" ", null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("用户问题不能为空");
    }

    private static class CapturingSpringAiVisualChatClient implements SpringAiVisualChatClient {
        private String instructions;
        private String userText;
        private String imageDataUrl;
        private String userHint;

        @Override
        public String complete(String instructions, String userText, String imageDataUrl, String userHint) {
            this.instructions = instructions;
            this.userText = userText;
            this.imageDataUrl = imageDataUrl;
            this.userHint = userHint;
            return "请把镜头靠近设备指示灯。";
        }
    }
}
