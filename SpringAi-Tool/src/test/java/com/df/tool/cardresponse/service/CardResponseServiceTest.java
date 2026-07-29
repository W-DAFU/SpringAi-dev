package com.df.tool.cardresponse.service;

import com.df.tool.cardresponse.domain.CardChatRequest;
import com.df.tool.cardresponse.prompt.CardPromptProvider;
import com.df.tool.cardresponse.tool.JobCardTool;
import com.df.tool.cardresponse.tool.ResumeCardTool;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CardResponseServiceTest {

    @Test
    void chatUsesResumePromptAndResumeTool() {
        OpenAiChatModel openAiChatModel = mockChatModel("resume card");
        CardResponseService service = new CardResponseService(
                openAiChatModel, new ResumeCardTool(), new JobCardTool(), new CardPromptProvider());

        String result = service.chat(new CardChatRequest("resume", "build my resume"));

        assertThat(result).isEqualTo("resume card");
        Prompt prompt = capturePrompt(openAiChatModel);
        assertThat(prompt.getContents()).contains("简历信息卡片助手");
        assertThat(((ToolCallingChatOptions) prompt.getOptions()).getToolCallbacks()).hasSize(1);
    }

    @Test
    void chatUsesJobPromptAndJobTool() {
        OpenAiChatModel openAiChatModel = mockChatModel("job card");
        CardResponseService service = new CardResponseService(
                openAiChatModel, new ResumeCardTool(), new JobCardTool(), new CardPromptProvider());

        String result = service.chat(new CardChatRequest("job", "build a job post"));

        assertThat(result).isEqualTo("job card");
        Prompt prompt = capturePrompt(openAiChatModel);
        assertThat(prompt.getContents()).contains("岗位信息卡片助手");
        assertThat(((ToolCallingChatOptions) prompt.getOptions()).getToolCallbacks()).hasSize(1);
    }

    @Test
    void chatRejectsUnsupportedUserType() {
        CardResponseService service = new CardResponseService(
                mockChatModel("unused"), new ResumeCardTool(), new JobCardTool(), new CardPromptProvider());

        assertThatThrownBy(() -> service.chat(new CardChatRequest("unknown", "message")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported userType");
    }

    private OpenAiChatModel mockChatModel(String content) {
        OpenAiChatModel openAiChatModel = mock(OpenAiChatModel.class);
        when(openAiChatModel.getOptions())
                .thenReturn(OpenAiChatOptions.builder().model("test-model").build());
        when(openAiChatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(content)))));
        return openAiChatModel;
    }

    private Prompt capturePrompt(OpenAiChatModel openAiChatModel) {
        org.mockito.ArgumentCaptor<Prompt> promptCaptor = org.mockito.ArgumentCaptor.forClass(Prompt.class);
        verify(openAiChatModel).call(promptCaptor.capture());
        return promptCaptor.getValue();
    }
}
