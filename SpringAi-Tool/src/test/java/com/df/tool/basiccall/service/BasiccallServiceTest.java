package com.df.tool.basiccall.service;

import com.df.tool.basiccall.tool.BasiccallTool;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BasiccallServiceTest {

    @Test
    void chatMessageWithToolsSendsToolCallbacksToModel() {
        OpenAiChatModel openAiChatModel = mock(OpenAiChatModel.class);
        when(openAiChatModel.getOptions())
                .thenReturn(OpenAiChatOptions.builder().model("test-model").build());
        when(openAiChatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("tool answer")))));
        BasiccallService basiccallService = new BasiccallService(openAiChatModel, new BasiccallTool());

        String result = basiccallService.chatMessageWithTools("现在几点");

        assertThat(result).isEqualTo("tool answer");
        org.mockito.ArgumentCaptor<Prompt> promptCaptor = org.mockito.ArgumentCaptor.forClass(Prompt.class);
        verify(openAiChatModel, times(1)).call(promptCaptor.capture());
        assertThat(promptCaptor.getValue().getOptions())
                .isInstanceOf(ToolCallingChatOptions.class);
        ToolCallingChatOptions options = (ToolCallingChatOptions) promptCaptor.getValue().getOptions();
        assertThat(options.getToolCallbacks()).isNotEmpty();
    }

    @Test
    void basiccallToolProvidesCurrentTimeText() {
        BasiccallTool basiccallTool = new BasiccallTool();

        String result = basiccallTool.getCurrentTime("Asia/Shanghai");

        assertThat(result).contains("Asia/Shanghai");
    }
}
