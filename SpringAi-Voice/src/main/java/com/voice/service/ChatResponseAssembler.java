package com.voice.service;

import com.voice.commons.domain.bo.ChatMessageBo;
import com.voice.commons.domain.vo.ChatMessageVo;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * 普通聊天响应组装服务。
 * <p>
 * 该服务复用原控制器中的 ChatMessageVo 组装逻辑，并保留可选 TTS 处理。
 */
@Service
public class ChatResponseAssembler {

    /**
     * 文字转语音模型。
     * 当请求参数 ttsEnabled=true 时，用它把 AI 文本回复转换成音频。
     */
    private final TextToSpeechModel textToSpeechModel;

    public ChatResponseAssembler(TextToSpeechModel textToSpeechModel) {
        this.textToSpeechModel = textToSpeechModel;
    }

    /**
     * 组装普通聊天接口返回对象。
     * <p>
     * 返回对象会保留会话 ID、消息类型、实际送入模型的文本和 AI 文本回复。
     * 如果请求启用了 TTS，则同时生成音频格式和 Base64 音频内容。
     *
     * @param chat 聊天请求参数
     * @param inputText 实际送入聊天模型的文本
     * @param answerText AI 聊天模型返回的文本回复
     * @return 聊天消息返回对象
     */
    public ChatMessageVo toChatMessageVo(ChatMessageBo chat, String inputText, String answerText) {
        ChatMessageVo vo = new ChatMessageVo();
        vo.setSessionId(chat.getSessionId());
        vo.setMessageType(chat.getMessageType());
        vo.setInputText(inputText);
        vo.setAnswerText(answerText);

        if (Boolean.TRUE.equals(chat.getTtsEnabled())) {
            byte[] audioBytes = textToSpeechModel.call(answerText);
            vo.setAnswerAudioFormat("mp3");
            vo.setAnswerAudioBase64(Base64.getEncoder().encodeToString(audioBytes));
        }

        return vo;
    }

}
