package com.realtimevideo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.util.Base64;

@Component
public class ChatClientVisualChatClient implements SpringAiVisualChatClient {

    private final ChatClient chatClient;

    public ChatClientVisualChatClient(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String complete(String instructions, String userText, String imageDataUrl, String userHint) {
        String promptText = buildPromptText(userText, userHint);
        if (imageDataUrl == null || imageDataUrl.isBlank()) {
            return chatClient.prompt()
                    .system(instructions)
                    .user(promptText)
                    .call()
                    .content();
        }

        ImagePayload imagePayload = parseImagePayload(imageDataUrl);
        return chatClient.prompt()
                .system(instructions)
                .user(user -> user
                        .text(promptText)
                        .media(imagePayload.mimeType(), new ByteArrayResource(imagePayload.bytes())))
                .call()
                .content();
    }

    private static String buildPromptText(String userText, String userHint) {
        if (userHint == null || userHint.isBlank()) {
            return userText;
        }
        return userText + "\n\n用户补充提示：" + userHint.trim();
    }

    private static ImagePayload parseImagePayload(String imageDataUrl) {
        int commaIndex = imageDataUrl.indexOf(',');
        if (!imageDataUrl.startsWith("data:image/") || commaIndex < 0) {
            throw new IllegalArgumentException("图片必须是 data:image/*;base64 格式");
        }
        String metadata = imageDataUrl.substring(5, commaIndex);
        String mimeTypeText = metadata.split(";")[0];
        byte[] bytes = Base64.getDecoder().decode(imageDataUrl.substring(commaIndex + 1));
        return new ImagePayload(MimeTypeUtils.parseMimeType(mimeTypeText), bytes);
    }

    private record ImagePayload(MimeType mimeType, byte[] bytes) {
    }
}
