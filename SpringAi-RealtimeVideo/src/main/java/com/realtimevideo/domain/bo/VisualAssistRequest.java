package com.realtimevideo.domain.bo;

import jakarta.validation.constraints.Size;

public record VisualAssistRequest(
        @Size(max = 4000)
        String userText,

        @Size(max = 1_500_000)
        String imageDataUrl,

        @Size(max = 300)
        String userHint
) {
}
