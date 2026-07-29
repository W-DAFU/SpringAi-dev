package com.df.tool.cardresponse.service;

import com.df.tool.cardresponse.domain.CardChatRequest;
import com.df.tool.cardresponse.domain.CardChatResponse;
import com.df.tool.cardresponse.domain.CardSessionPayload;
import com.df.tool.cardresponse.domain.CardUserType;
import com.df.tool.cardresponse.prompt.CardPromptProvider;
import com.df.tool.cardresponse.session.CardSessionCache;
import com.df.tool.cardresponse.tool.JobSeekerCardTool;
import com.df.tool.cardresponse.tool.RecruiterCardTool;
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
    private final JobSeekerCardTool jobSeekerCardTool;
    private final RecruiterCardTool recruiterCardTool;
    private final CardPromptProvider cardPromptProvider;
    private final CardSessionCache cardSessionCache;

    /**
     * 根据用户身份选择提示词和专属工具组。
     *
     * <p>流程：
     * 1. 先删除当前用户的历史临时缓存；
     * 2. 调用模型，模型按身份选择专属工具；
     * 3. 工具把协议数据写入 Redis，并把原始数据返回给模型；
     * 4. 后端读取 Redis 协议数据，加上模型自然语言，组装最终返回；
     * 5. 返回前删除本次临时缓存。</p>
     */
    public CardChatResponse chat(CardChatRequest request) {
        CardUserType userType = CardUserType.from(request.userType());
        String systemPrompt = cardPromptProvider.getPrompt(userType);
        Object toolGroup = selectToolGroup(userType);

        cardSessionCache.delete(request.userId());
        try {
            String aiMessage = ChatClient.create(openAiChatModel)
                    .prompt()
                    .system(systemPrompt + "\n当前用户ID：" + request.userId() + "\n调用工具时必须传入这个用户ID。")
                    .user(request.message())
                    .tools(toolGroup)
                    .call()
                    .content();

            CardSessionPayload payload = cardSessionCache.get(request.userId())
                    .orElseGet(() -> new CardSessionPayload(userType.defaultMessageType(), "{}"));
            return new CardChatResponse(payload.messageType(), payload.results(), cleanNaturalMessage(aiMessage));
        } finally {
            cardSessionCache.delete(request.userId());
        }
    }

    /**
     * 用户身份和工具组一一对应，避免模型拿到不属于当前身份的工具。
     */
    private Object selectToolGroup(CardUserType userType) {
        return switch (userType) {
            case JOB_SEEKER -> jobSeekerCardTool;
            case RECRUITER -> recruiterCardTool;
        };
    }

    /**
     * 兜底清理模型可能生成的 Markdown 标记，保证 message 是普通自然语言。
     */
    private String cleanNaturalMessage(String message) {
        if (message == null) {
            return "";
        }
        return message.replace("#", "")
                .replace("*", "")
                .replace("`", "")
                .replace("\r", " ")
                .replace("\n", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
