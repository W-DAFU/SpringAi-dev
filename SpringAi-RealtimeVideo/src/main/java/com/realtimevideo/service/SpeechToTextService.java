package com.realtimevideo.service;

import com.realtimevideo.domain.vo.SpeechToTextResponse;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SpeechToTextService {

    private final TranscriptionModel transcriptionModel;

    public SpeechToTextService(TranscriptionModel transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }

    public SpeechToTextResponse transcribe(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "音频不能为空");
        }
        String text = transcriptionModel.call(new AudioTranscriptionPrompt(audioFile.getResource()))
                .getResult()
                .getOutput();
        return new SpeechToTextResponse(text);
    }
}
