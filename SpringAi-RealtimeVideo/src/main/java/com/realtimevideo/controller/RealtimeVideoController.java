package com.realtimevideo.controller;

import com.realtimevideo.domain.bo.TextToSpeechRequest;
import com.realtimevideo.domain.bo.VisualAssistRequest;
import com.realtimevideo.domain.vo.R;
import com.realtimevideo.domain.vo.SpeechToTextResponse;
import com.realtimevideo.domain.vo.TextToSpeechResponse;
import com.realtimevideo.domain.vo.VisualAssistResponse;
import com.realtimevideo.service.SpeechToTextService;
import com.realtimevideo.service.TextToSpeechService;
import com.realtimevideo.service.VisualAssistService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/realtime-video")
public class RealtimeVideoController {

    private final VisualAssistService visualAssistService;
    private final SpeechToTextService speechToTextService;
    private final TextToSpeechService textToSpeechService;

    public RealtimeVideoController(
            VisualAssistService visualAssistService,
            SpeechToTextService speechToTextService,
            TextToSpeechService textToSpeechService
    ) {
        this.visualAssistService = visualAssistService;
        this.speechToTextService = speechToTextService;
        this.textToSpeechService = textToSpeechService;
    }

    @PostMapping("/assist")
    public R<VisualAssistResponse> assist(@Valid @RequestBody VisualAssistRequest request) {
        return R.ok(visualAssistService.assist(request));
    }

    @PostMapping(value = "/speech-to-text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SpeechToTextResponse> speechToText(@RequestPart("audioFile") MultipartFile audioFile) {
        return R.ok(speechToTextService.transcribe(audioFile));
    }

    @PostMapping("/text-to-speech")
    public R<TextToSpeechResponse> textToSpeech(@Valid @RequestBody TextToSpeechRequest request) {
        return R.ok(textToSpeechService.synthesize(request));
    }
}
