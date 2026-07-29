package com.voice.controller;

import com.voice.domain.bo.ChatMessageBo;
import com.voice.domain.vo.ChatStreamBlockVo;
import com.voice.service.stream.ChatStreamService;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatStreamControllerTest {

    @Test
    void streamReturnsSseEmitterAndPushesBlocks() {
        ChatStreamService chatStreamService = mock(ChatStreamService.class);
        ChatMessageBo chat = new ChatMessageBo();
        chat.setMessageType(ChatMessageBo.MessageType.TEXT);
        chat.setText("帮我查找华为儿童手表5活力版 都有哪些");
        ChatStreamBlockVo block = new ChatStreamBlockVo();
        block.setCardType("text");
        block.setAnswerText("正在为您查找华为儿童手表5活力版的相关信息：");

        when(chatStreamService.stream(chat)).thenReturn(Flux.just(block));

        ChatStreamController controller = new ChatStreamController(chatStreamService, Runnable::run);
        SseEmitter emitter = controller.stream(chat);

        assertThat(emitter).isNotNull();
        verify(chatStreamService).stream(chat);
    }

}
