package com.voice.controller;

import com.voice.commons.domain.bo.ChatMessageBo;
import com.voice.commons.domain.vo.ChatMessageVo;
import com.voice.commons.domain.vo.R;
import com.voice.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring AI 普通聊天接口控制器。
 * <p>
 * 当前控制器保留原有一次性返回的聊天入口，支持文字聊天、语音转文字聊天，
 * 以及可选的 AI 回复文字转语音。具体模型调用和公共处理逻辑已经迁移到
 * {@link ChatService}，避免控制器继续承载业务编排。
 */
@RestController
@RequestMapping("/api")
public class SpringAiController {

    /**
     * 普通非流式聊天服务。
     */
    private final ChatService chatService;

    public SpringAiController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 统一普通聊天接口。
     * <p>
     * 请求格式必须是 multipart/form-data。
     * 文字聊天传 messageType=TEXT 和 text；
     * 语音聊天传 messageType=AUDIO 和 audioFile；
     * 如需把 AI 回复转成语音，额外传 ttsEnabled=true。
     *
     * @param chat 聊天请求参数
     * @return 统一返回格式，data 中封装 ChatMessageVo
     */
    @PostMapping(value = "chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<ChatMessageVo> chat(@ModelAttribute ChatMessageBo chat) {
        return R.ok(chatService.chat(chat));
    }

}
