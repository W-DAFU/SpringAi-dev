package com.ye.domain.vo;

import com.ye.domain.bo.ChatMessageBo;
import lombok.Data;

/**
 * 聊天消息返回对象。
 * <p>
 * 该对象只用于封装接口响应数据，返回给前端聊天窗口展示。
 * 请求参数仍然由 ChatMessageBo 接收。
 */
@Data
public class ChatMessageVo {

    /**
     * 会话 ID。
     * 用于前端区分不同聊天会话，后续也可以用于服务端维护多轮对话上下文。
     */
    private String sessionId;

    /**
     * 本次用户输入的消息类型。
     * TEXT 表示文字输入，AUDIO 表示语音输入。
     */
    private ChatMessageBo.MessageType messageType;

    /**
     * 实际送入聊天模型的文字。
     * 如果用户输入的是文字，则等于请求参数 text；
     * 如果用户输入的是语音，则等于语音转文字后的结果。
     */
    private String inputText;

    /**
     * AI 聊天模型返回的文本回复。
     */
    private String answerText;

    /**
     * AI 回复语音的音频格式。
     * 只有请求参数 ttsEnabled=true 时才会有值，例如 mp3。
     */
    private String answerAudioFormat;

    /**
     * AI 回复语音的 Base64 内容。
     * 前端可以根据 answerAudioFormat 还原成音频播放。
     * 只有请求参数 ttsEnabled=true 时才会有值。
     */
    private String answerAudioBase64;

}
