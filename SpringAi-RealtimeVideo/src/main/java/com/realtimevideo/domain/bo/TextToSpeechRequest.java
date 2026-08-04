package com.realtimevideo.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TextToSpeechRequest(
        @NotBlank
        @Size(max = 4000)
        String text
) {
}
