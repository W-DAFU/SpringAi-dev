package com.realtimevideo.domain.vo;

public record TextToSpeechResponse(
        String audioFormat,
        String audioBase64
) {
}
