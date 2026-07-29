package com.voice.controller;

import com.voice.domain.bo.ChatMessageBo;
import com.voice.domain.vo.ChatStreamBlockVo;
import com.voice.service.stream.ChatStreamService;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatStreamControllerTest {

    @Test
    void streamReturnsBlocksFromStreamService() {
        ChatStreamService chatStreamService = mock(ChatStreamService.class);
        ChatStreamController controller = new ChatStreamController(chatStreamService);
        ChatMessageBo chat = new ChatMessageBo();
        chat.setMessageType(ChatMessageBo.MessageType.TEXT);
        chat.setText("hello");
        ChatStreamBlockVo textBlock = new ChatStreamBlockVo();
        textBlock.setCardType("text");
        textBlock.setAnswerText("你好");
        ChatStreamBlockVo productBlock = new ChatStreamBlockVo();
        productBlock.setCardType("product_card");
        productBlock.setAnswerText("华为儿童手表5活力版");

        when(chatStreamService.stream(chat)).thenReturn(Flux.just(textBlock, productBlock));

        List<ChatStreamBlockVo> chunks = controller.stream(chat).collectList().block();

        assertThat(chunks).containsExactly(textBlock, productBlock);
        verify(chatStreamService).stream(chat);
    }

}
