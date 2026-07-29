package com.voice.service;

import com.voice.commons.domain.bo.ChatMessageBo;
import com.voice.commons.domain.vo.ChatStreamBlockVo;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * 流式聊天 block 组装服务。
 * <p>
 * 负责补齐 ChatMessageVo 公共字段，并在 text block 上按需生成 TTS 音频。
 */
@Service
public class ChatStreamBlockAssembler {

    /**
     * 文字转语音模型。
     */
    private final TextToSpeechModel textToSpeechModel;

    public ChatStreamBlockAssembler(TextToSpeechModel textToSpeechModel) {
        this.textToSpeechModel = textToSpeechModel;
    }

    /**
     * 补充和普通 ChatMessageVo 一致的公共返回字段，并按需生成 TTS。
     *
     * @param chat 聊天请求参数
     * @param inputText 实际送入聊天模型的文本
     * @param block 已解析完成的业务 block
     * @return 可直接返回给前端的流式 block
     */
    public ChatStreamBlockVo enrich(ChatMessageBo chat, String inputText, ChatStreamBlockVo block) {
        block.setSessionId(chat.getSessionId());
        block.setMessageType(chat.getMessageType());
        block.setInputText(inputText);

        if ("text".equals(block.getCardType()) && Boolean.TRUE.equals(chat.getTtsEnabled())) {
            byte[] audioBytes = textToSpeechModel.call(block.getAnswerText());
            block.setAnswerAudioFormat("mp3");
            block.setAnswerAudioBase64(Base64.getEncoder().encodeToString(audioBytes));
        }

        return block;
    }

}
