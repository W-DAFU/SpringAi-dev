package com.voice.service;

import com.voice.commons.domain.bo.ChatMessageBo;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 流式聊天服务。
 * <p>
 * 该服务和普通聊天服务保持独立，只共用输入解析逻辑。
 * 返回值直接使用 Flux<String> 表示模型生成的文本分片。
 */
@Service
public class ChatStreamService {

    /**
     * Spring AI 聊天客户端。
     * 用于把用户输入发送给聊天模型，并获取流式文本回复。
     */
    private final ChatClient chatClient;

    /**
     * 聊天输入解析器。
     * 普通接口和流式接口共用该逻辑，保证 TEXT/AUDIO 的入参校验和转换规则一致。
     */
    private final ChatInputResolver chatInputResolver;

    public ChatStreamService(ChatClient.Builder chatClientBuilder, ChatInputResolver chatInputResolver) {
        this.chatClient = chatClientBuilder.build();
        this.chatInputResolver = chatInputResolver;
    }

    /**
     * 流式聊天。
     * <p>
     * 先复用公共输入解析逻辑得到实际输入文本，再调用 Spring AI stream API，
     * 逐段返回模型生成的文本内容。
     *
     * @param chat 聊天请求参数
     * @return AI 回复文本分片流
     */
    public Flux<String> stream(ChatMessageBo chat) {
        String inputText = chatInputResolver.resolveInputText(chat);
        return chatClient.prompt()
                .user(inputText)
                .stream()
                .content();
    }

}
