package com.df.tool.cardresponse.service;

import com.df.tool.cardresponse.domain.CardChatRequest;
import com.df.tool.cardresponse.domain.CardChatResponse;
import com.df.tool.cardresponse.domain.CardSessionPayload;
import com.df.tool.cardresponse.domain.CardUserType;
import com.df.tool.cardresponse.prompt.CardPromptProvider;
import com.df.tool.cardresponse.session.CardSessionCache;
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
    private final CardSessionCache cardSessionCache;

    /**
     * 根据 userType 选择提示词和工具，调用模型后再组装完整协议。
     */
    public CardChatResponse chat(CardChatRequest request) {
        CardUserType userType = CardUserType.from(request.userType());
        String systemPrompt = cardPromptProvider.getPrompt(userType);
        Object tool = selectTool(userType);

        cardSessionCache.delete(request.userId());
        try {
            String aiMessage = ChatClient.create(openAiChatModel)
                    .prompt()
                    .system(systemPrompt + "\n当前用户ID：" + request.userId() + "\n调用工具时必须传入这个用户ID。")
                    .user(request.message())
                    .tools(tool)
                    .call()
                    .content();

            CardSessionPayload payload = cardSessionCache.get(request.userId())
                    .orElseGet(() -> new CardSessionPayload(userType.messageType(), "{}"));
            return new CardChatResponse(payload.messageType(), payload.results(), aiMessage);
        } finally {
            cardSessionCache.delete(request.userId());
        }
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
