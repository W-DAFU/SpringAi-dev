package com.df.tool.cardresponse.service;

import com.df.tool.cardresponse.domain.CardChatRequest;
import com.df.tool.cardresponse.domain.CardUserType;
import com.df.tool.cardresponse.prompt.CardPromptProvider;
import com.df.tool.cardresponse.tool.JobCardTool;
import com.df.tool.cardresponse.tool.ResumeCardTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

/**
 * 卡片返回案例服务。
 */
@RequiredArgsConstructor
@Service
public class CardResponseService {

    private final OpenAiChatModel openAiChatModel;
    private final ResumeCardTool resumeCardTool;
    private final JobCardTool jobCardTool;
    private final CardPromptProvider cardPromptProvider;

    /**
     * 根据请求中的 userType 选择对应提示词和工具，再调用模型生成卡片回答。
     */
    public String chat(CardChatRequest request) {
        CardUserType userType = CardUserType.from(request.userType());
        String systemPrompt = cardPromptProvider.getPrompt(userType);
        Object tool = selectTool(userType);

        return ChatClient.create(openAiChatModel)
                .prompt()
                .system(systemPrompt)
                .user(request.message())
                .tools(tool)
                .call()
                .content();
    }

    /**
     * userType 和工具一一对应，避免模型拿到不属于当前场景的工具。
     */
    private Object selectTool(CardUserType userType) {
        return switch (userType) {
            case RESUME -> resumeCardTool;
            case JOB -> jobCardTool;
        };
    }
}
