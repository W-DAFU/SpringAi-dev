package com.voice.controller;

import com.voice.commons.domain.bo.ChatMessageBo;
import com.voice.service.ChatStreamService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Spring AI 流式聊天接口控制器。
 * <p>
 * 当前控制器提供独立的流式输出入口，复用文字/语音输入解析逻辑，
 * 但不复用普通接口的 R 响应包装，避免阻塞流式分片输出。
 */
@RestController
@RequestMapping("/api")
public class ChatStreamController {

    /**
     * 流式聊天服务。
     */
    private final ChatStreamService chatStreamService;

    public ChatStreamController(ChatStreamService chatStreamService) {
        this.chatStreamService = chatStreamService;
    }

    /**
     * 流式聊天接口。
     * <p>
     * 请求格式必须是 multipart/form-data。
     * 文字聊天传 messageType=TEXT 和 text；
     * 语音聊天传 messageType=AUDIO 和 audioFile。
     * 返回值是 text/event-stream 文本分片，不套用普通 R 响应包装。
     *
     * @param chat 聊天请求参数
     * @return AI 回复文本分片流
     */
    @PostMapping(
            value = "chat/stream",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<String> stream(@ModelAttribute ChatMessageBo chat) {
        return chatStreamService.stream(chat);
    }

}
