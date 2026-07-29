package com.voice.service;

import com.voice.commons.ChatInputResolver;
import com.voice.domain.bo.ChatMessageBo;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatInputResolverTest {

    @Test
    void resolveInputTextReturnsTextMessageContent() {
        ChatInputResolver resolver = new ChatInputResolver(null);
        ChatMessageBo chat = new ChatMessageBo();
        chat.setMessageType(ChatMessageBo.MessageType.TEXT);
        chat.setText("hello spring ai");

        String inputText = resolver.resolveInputText(chat);

        assertThat(inputText).isEqualTo("hello spring ai");
    }

    @Test
    void resolveInputTextRejectsBlankTextMessage() {
        ChatInputResolver resolver = new ChatInputResolver(null);
        ChatMessageBo chat = new ChatMessageBo();
        chat.setMessageType(ChatMessageBo.MessageType.TEXT);
        chat.setText(" ");

        assertThatThrownBy(() -> resolver.resolveInputText(chat))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

}
