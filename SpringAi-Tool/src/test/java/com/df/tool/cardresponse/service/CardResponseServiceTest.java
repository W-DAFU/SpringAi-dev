package com.df.tool.cardresponse.service;

import com.df.tool.cardresponse.domain.CardChatRequest;
import com.df.tool.cardresponse.domain.CardChatResponse;
import com.df.tool.cardresponse.domain.CardSessionPayload;
import com.df.tool.cardresponse.prompt.CardPromptProvider;
import com.df.tool.cardresponse.session.CardSessionCache;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CardResponseServiceTest {

    @Test
    void chatUsesResumePromptAndResumeToolThenBuildsProtocolFromCache() {
        OpenAiChatModel openAiChatModel = mockChatModel("已根据简历数据生成自然语言说明。");
        CardSessionCache cardSessionCache = mock(CardSessionCache.class);
        when(cardSessionCache.get("u1001"))
                .thenReturn(Optional.of(new CardSessionPayload("简历", "{\"name\":\"张三\"}")));
        CardResponseService service = new CardResponseService(
                openAiChatModel,
                new ResumeCardTool(cardSessionCache),
                new JobCardTool(cardSessionCache),
                new CardPromptProvider(),
                cardSessionCache);

        CardChatResponse result = service.chat(new CardChatRequest("u1001", "resume", "生成简历卡片"));

        assertThat(result.messageType()).isEqualTo("简历");
        assertThat(result.results()).isEqualTo("{\"name\":\"张三\"}");
        assertThat(result.message()).isEqualTo("已根据简历数据生成自然语言说明。");
        Prompt prompt = capturePrompt(openAiChatModel);
        assertThat(prompt.getContents()).contains("简历信息卡片助手");
        assertThat(prompt.getContents()).contains("u1001");
        assertThat(((ToolCallingChatOptions) prompt.getOptions()).getToolCallbacks()).hasSize(1);
        verify(cardSessionCache, atLeastOnce()).delete("u1001");
    }

    @Test
    void chatUsesJobPromptAndJobToolThenBuildsProtocolFromCache() {
        OpenAiChatModel openAiChatModel = mockChatModel("已根据岗位数据生成自然语言说明。");
        CardSessionCache cardSessionCache = mock(CardSessionCache.class);
        when(cardSessionCache.get("u2001"))
                .thenReturn(Optional.of(new CardSessionPayload("岗位", "{\"title\":\"Java后端\"}")));
        CardResponseService service = new CardResponseService(
                openAiChatModel,
                new ResumeCardTool(cardSessionCache),
                new JobCardTool(cardSessionCache),
                new CardPromptProvider(),
                cardSessionCache);

        CardChatResponse result = service.chat(new CardChatRequest("u2001", "job", "生成岗位卡片"));

        assertThat(result.messageType()).isEqualTo("岗位");
        assertThat(result.results()).isEqualTo("{\"title\":\"Java后端\"}");
        assertThat(result.message()).isEqualTo("已根据岗位数据生成自然语言说明。");
        Prompt prompt = capturePrompt(openAiChatModel);
        assertThat(prompt.getContents()).contains("岗位信息卡片助手");
        assertThat(prompt.getContents()).contains("u2001");
        assertThat(((ToolCallingChatOptions) prompt.getOptions()).getToolCallbacks()).hasSize(1);
        verify(cardSessionCache, atLeastOnce()).delete("u2001");
    }

    @Test
    void chatRejectsUnsupportedUserType() {
        CardSessionCache cardSessionCache = mock(CardSessionCache.class);
        CardResponseService service = new CardResponseService(
                mockChatModel("unused"),
                new ResumeCardTool(cardSessionCache),
                new JobCardTool(cardSessionCache),
                new CardPromptProvider(),
                cardSessionCache);

        assertThatThrownBy(() -> service.chat(new CardChatRequest("u3001", "unknown", "message")))
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
