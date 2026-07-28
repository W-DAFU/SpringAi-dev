package com.voice.controller;

import com.voice.commons.domain.bo.ChatMessageBo;
import com.voice.service.ChatStreamService;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatStreamControllerTest {

    @Test
    void streamReturnsChunksFromStreamService() {
        ChatStreamService chatStreamService = mock(ChatStreamService.class);
        ChatStreamController controller = new ChatStreamController(chatStreamService);
        ChatMessageBo chat = new ChatMessageBo();
        chat.setMessageType(ChatMessageBo.MessageType.TEXT);
        chat.setText("hello");

        when(chatStreamService.stream(chat)).thenReturn(Flux.just("你", "好"));

        List<String> chunks = controller.stream(chat).collectList().block();

        assertThat(chunks).containsExactly("你", "好");
        verify(chatStreamService).stream(chat);
    }

}
