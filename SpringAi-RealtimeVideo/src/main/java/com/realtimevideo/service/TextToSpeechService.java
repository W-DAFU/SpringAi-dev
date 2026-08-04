package com.realtimevideo.service;

import com.realtimevideo.domain.bo.TextToSpeechRequest;
import com.realtimevideo.domain.vo.TextToSpeechResponse;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;

@Service
public class TextToSpeechService {

    private final TextToSpeechModel textToSpeechModel;

    public TextToSpeechService(TextToSpeechModel textToSpeechModel) {
        this.textToSpeechModel = textToSpeechModel;
    }

    public TextToSpeechResponse synthesize(TextToSpeechRequest request) {
        if (request == null || request.text() == null || request.text().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文本不能为空");
        }
        byte[] audioBytes = textToSpeechModel.call(request.text().trim());
        return new TextToSpeechResponse("mp3", Base64.getEncoder().encodeToString(audioBytes));
    }
}
