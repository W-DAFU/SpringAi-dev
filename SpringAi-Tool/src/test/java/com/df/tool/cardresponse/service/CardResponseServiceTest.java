package com.df.tool.cardresponse.service;

import com.df.tool.cardresponse.domain.CardChatRequest;
import com.df.tool.cardresponse.domain.CardChatResponse;
import com.df.tool.cardresponse.domain.CardSessionPayload;
import com.df.tool.cardresponse.prompt.CardPromptProvider;
import com.df.tool.cardresponse.session.CardSessionCache;
import com.df.tool.cardresponse.tool.JobSeekerCardTool;
import com.df.tool.cardresponse.tool.RecruiterCardTool;
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
    void jobSeekerUsesJobSeekerPromptAndToolsThenReturnsJobProtocol() {
        OpenAiChatModel openAiChatModel = mockChatModel("已经为你找到一批匹配的后端岗位，可以重点关注技术栈和薪资范围。");
        CardSessionCache cardSessionCache = mock(CardSessionCache.class);
        when(cardSessionCache.get("u1001"))
                .thenReturn(Optional.of(new CardSessionPayload("岗位", "{\"title\":\"Java后端开发工程师\"}")));
        CardResponseService service = new CardResponseService(
                openAiChatModel,
                new JobSeekerCardTool(cardSessionCache),
                new RecruiterCardTool(cardSessionCache),
                new CardPromptProvider(),
                cardSessionCache);

        CardChatResponse result = service.chat(new CardChatRequest("u1001", "job_seeker", "帮我找合适的岗位"));

        assertThat(result.messageType()).isEqualTo("岗位");
        assertThat(result.results()).isEqualTo("{\"title\":\"Java后端开发工程师\"}");
        assertThat(result.message()).isEqualTo("已经为你找到一批匹配的后端岗位，可以重点关注技术栈和薪资范围。");
        assertThat(result.message()).doesNotContain("#", "*", "-", "\n");
        Prompt prompt = capturePrompt(openAiChatModel);
        assertThat(prompt.getContents()).contains("求职者服务助手");
        assertThat(prompt.getContents()).contains("u1001");
        assertThat(((ToolCallingChatOptions) prompt.getOptions()).getToolCallbacks()).hasSize(2);
        verify(cardSessionCache, atLeastOnce()).delete("u1001");
    }

    @Test
    void recruiterUsesRecruiterPromptAndToolsThenReturnsResumeProtocol() {
        OpenAiChatModel openAiChatModel = mockChatModel("已经为你筛选到候选人简历，可以优先看技能匹配度和工作年限。");
        CardSessionCache cardSessionCache = mock(CardSessionCache.class);
        when(cardSessionCache.get("u2001"))
                .thenReturn(Optional.of(new CardSessionPayload("简历", "{\"name\":\"张三\"}")));
        CardResponseService service = new CardResponseService(
                openAiChatModel,
                new JobSeekerCardTool(cardSessionCache),
                new RecruiterCardTool(cardSessionCache),
                new CardPromptProvider(),
                cardSessionCache);

        CardChatResponse result = service.chat(new CardChatRequest("u2001", "recruiter", "帮我找 Java 候选人"));

        assertThat(result.messageType()).isEqualTo("简历");
        assertThat(result.results()).isEqualTo("{\"name\":\"张三\"}");
        assertThat(result.message()).isEqualTo("已经为你筛选到候选人简历，可以优先看技能匹配度和工作年限。");
        assertThat(result.message()).doesNotContain("#", "*", "-", "\n");
        Prompt prompt = capturePrompt(openAiChatModel);
        assertThat(prompt.getContents()).contains("招聘者服务助手");
        assertThat(prompt.getContents()).contains("u2001");
        assertThat(((ToolCallingChatOptions) prompt.getOptions()).getToolCallbacks()).hasSize(3);
        verify(cardSessionCache, atLeastOnce()).delete("u2001");
    }

    @Test
    void keepsOldResumeAliasAsJobSeekerForExistingClients() {
        OpenAiChatModel openAiChatModel = mockChatModel("已经为你找到岗位信息。");
        CardSessionCache cardSessionCache = mock(CardSessionCache.class);
        when(cardSessionCache.get("u3001"))
                .thenReturn(Optional.of(new CardSessionPayload("岗位", "{\"title\":\"Java后端开发工程师\"}")));
        CardResponseService service = new CardResponseService(
                openAiChatModel,
                new JobSeekerCardTool(cardSessionCache),
                new RecruiterCardTool(cardSessionCache),
                new CardPromptProvider(),
                cardSessionCache);

        CardChatResponse result = service.chat(new CardChatRequest("u3001", "resume", "帮我找岗位"));

        assertThat(result.messageType()).isEqualTo("岗位");
        assertThat(capturePrompt(openAiChatModel).getContents()).contains("求职者服务助手");
    }

    @Test
    void chatRejectsUnsupportedUserType() {
        CardSessionCache cardSessionCache = mock(CardSessionCache.class);
        CardResponseService service = new CardResponseService(
                mockChatModel("unused"),
                new JobSeekerCardTool(cardSessionCache),
                new RecruiterCardTool(cardSessionCache),
                new CardPromptProvider(),
                cardSessionCache);

        assertThatThrownBy(() -> service.chat(new CardChatRequest("u4001", "unknown", "message")))
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
