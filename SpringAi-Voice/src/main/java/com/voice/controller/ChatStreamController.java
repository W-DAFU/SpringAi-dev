package com.voice.controller;

import com.voice.domain.bo.ChatMessageBo;
import com.voice.service.stream.ChatStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

    /**
     * SSE 写出需要异步执行，避免阻塞 Servlet 请求线程。
     */
    private final Executor streamExecutor;

    @Autowired
    public ChatStreamController(ChatStreamService chatStreamService) {
        this(chatStreamService, Executors.newCachedThreadPool());
    }

    ChatStreamController(ChatStreamService chatStreamService, Executor streamExecutor) {
        this.chatStreamService = chatStreamService;
        this.streamExecutor = streamExecutor;
    }

    /**
     * 流式聊天接口。
     * <p>
     * 请求格式必须是 multipart/form-data。
     * 文字聊天传 messageType=TEXT 和 text；
     * 语音聊天传 messageType=AUDIO 和 audioFile。
     * 返回值是 text/event-stream 业务 block，不套用普通 R 响应包装。
     *
     * @param chat 聊天请求参数
     * @return SSE 发射器
     */
    @PostMapping(
            value = "chat/stream",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter stream(@ModelAttribute ChatMessageBo chat) {
        SseEmitter emitter = new SseEmitter(0L);

        streamExecutor.execute(() -> {
            try {
                send(emitter, "start", Map.of("started", true));
                chatStreamService.stream(chat)
                        .doOnNext(block -> sendUnchecked(emitter, "block", block))
                        .blockLast();
                send(emitter, "done", Map.of("finished", true));
                emitter.complete();
            } catch (Exception exception) {
                try {
                    send(emitter, "error", Map.of("message", exception.getMessage()));
                    send(emitter, "done", Map.of("finished", false));
                    emitter.complete();
                } catch (Exception sendException) {
                    emitter.completeWithError(sendException);
                }
            }
        });

        return emitter;
    }

    /**
     * 发送一个 SSE 事件。
     */
    private void send(SseEmitter emitter, String eventName, Object data) throws IOException {
        emitter.send(SseEmitter.event()
                .name(eventName)
                .data(data));
    }

    /**
     * 在 Reactor 回调中发送 SSE 事件。
     */
    private void sendUnchecked(SseEmitter emitter, String eventName, Object data) {
        try {
            send(emitter, eventName, data);
        } catch (IOException exception) {
            throw new IllegalStateException("发送 SSE 事件失败", exception);
        }
    }

}
