package com.voice.service;

import com.voice.commons.domain.bo.ChatMessageBo;
import com.voice.commons.domain.vo.ChatStreamBlockVo;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.tts.TextToSpeechModel;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatStreamBlockAssemblerTest {

    @Test
    void enrichesTextBlockWithTtsAudioWhenEnabled() {
        TextToSpeechModel textToSpeechModel = mock(TextToSpeechModel.class);
        ChatStreamBlockAssembler assembler = new ChatStreamBlockAssembler(textToSpeechModel);
        ChatMessageBo chat = new ChatMessageBo();
        chat.setSessionId("session-1");
        chat.setMessageType(ChatMessageBo.MessageType.TEXT);
        chat.setTtsEnabled(true);
        ChatStreamBlockVo block = new ChatStreamBlockVo();
        block.setCardType("text");
        block.setAnswerText("你好");

        when(textToSpeechModel.call("你好")).thenReturn(new byte[]{1, 2, 3});

        ChatStreamBlockVo result = assembler.enrich(chat, "用户输入", block);

        assertThat(result.getSessionId()).isEqualTo("session-1");
        assertThat(result.getMessageType()).isEqualTo(ChatMessageBo.MessageType.TEXT);
        assertThat(result.getInputText()).isEqualTo("用户输入");
        assertThat(result.getAnswerAudioFormat()).isEqualTo("mp3");
        assertThat(result.getAnswerAudioBase64()).isEqualTo(Base64.getEncoder().encodeToString(new byte[]{1, 2, 3}));
    }

    @Test
    void doesNotGenerateTtsForProductCardBlock() {
        TextToSpeechModel textToSpeechModel = mock(TextToSpeechModel.class);
        ChatStreamBlockAssembler assembler = new ChatStreamBlockAssembler(textToSpeechModel);
        ChatMessageBo chat = new ChatMessageBo();
        chat.setTtsEnabled(true);
        ChatStreamBlockVo block = new ChatStreamBlockVo();
        block.setCardType("product_card");
        block.setAnswerText("华为儿童手表5活力版");

        ChatStreamBlockVo result = assembler.enrich(chat, "用户输入", block);

        assertThat(result.getAnswerAudioBase64()).isNull();
        verify(textToSpeechModel, never()).call("华为儿童手表5活力版");
    }

}
