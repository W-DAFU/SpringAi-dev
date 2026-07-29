package com.voice.service.chat;

import com.voice.commons.ChatInputResolver;
import com.voice.commons.ChatResponseAssembler;
import com.voice.domain.bo.ChatMessageBo;
import com.voice.domain.vo.ChatMessageVo;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * 普通非流式聊天服务。
 * <p>
 * 该服务承接原控制器中的普通聊天编排逻辑，负责输入解析、聊天模型调用，
 * 以及普通响应对象组装。
 */
@Service
public class ChatService {

    /**
     * Spring AI 聊天客户端。
     * 用于把用户输入发送给聊天模型，并获取完整文本回复。
     */
    private final ChatClient chatClient;

    /**
     * 聊天输入解析器。
     * 负责把文字或语音请求统一解析为可发送给聊天模型的文本。
     */
    private final ChatInputResolver chatInputResolver;

    /**
     * 聊天响应组装器。
     * 负责生成 ChatMessageVo，并按需把 AI 回复文本转成语音。
     */
    private final ChatResponseAssembler chatResponseAssembler;

    public ChatService(ChatClient.Builder chatClientBuilder,
                       ChatInputResolver chatInputResolver,
                       ChatResponseAssembler chatResponseAssembler) {
        this.chatClient = chatClientBuilder.build();
        this.chatInputResolver = chatInputResolver;
        this.chatResponseAssembler = chatResponseAssembler;
    }

    /**
     * 普通非流式聊天。
     * <p>
     * 该方法保持原有 /api/chat 的业务行为：先解析输入，再调用聊天模型获取完整回复，
     * 最后组装包含输入文本、回复文本和可选回复音频的返回对象。
     *
     * @param chat 聊天请求参数
     * @return 聊天响应数据
     */
    public ChatMessageVo chat(ChatMessageBo chat) {
        String inputText = chatInputResolver.resolveInputText(chat);
        String answerText = chatClient.prompt()
                .user(inputText)
                .call()
                .content();

        return chatResponseAssembler.toChatMessageVo(chat, inputText, answerText);
    }

}
