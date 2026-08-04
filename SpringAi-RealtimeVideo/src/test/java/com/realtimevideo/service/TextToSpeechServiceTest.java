package com.realtimevideo.service;

import com.realtimevideo.domain.bo.TextToSpeechRequest;
import com.realtimevideo.domain.vo.TextToSpeechResponse;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TextToSpeechServiceTest {

    @Test
    void synthesizeReturnsBase64AudioFromSpringAiTextToSpeechModel() {
        TextToSpeechModel model = mock(TextToSpeechModel.class);
        when(model.call("请靠近一点")).thenReturn(new byte[]{1, 2, 3});
        TextToSpeechService service = new TextToSpeechService(model);

        TextToSpeechResponse response = service.synthesize(new TextToSpeechRequest(" 请靠近一点 "));

        assertThat(response.audioFormat()).isEqualTo("mp3");
        assertThat(response.audioBase64()).isEqualTo("AQID");
    }

    @Test
    void synthesizeRejectsBlankText() {
        TextToSpeechModel model = mock(TextToSpeechModel.class);
        TextToSpeechService service = new TextToSpeechService(model);

        assertThatThrownBy(() -> service.synthesize(new TextToSpeechRequest(" ")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("文本不能为空");
    }
}
