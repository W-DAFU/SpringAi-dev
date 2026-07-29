package com.voice.commons;

import com.voice.domain.bo.ChatMessageBo;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * 聊天输入解析服务。
 * <p>
 * 普通接口和流式接口共用该服务，把不同类型的用户输入统一转换成
 * 可以发送给聊天模型的文本。
 */
@Service
public class ChatInputResolver {

    /**
     * 语音转文字模型。
     * 当用户上传语音文件时，用它把语音内容识别成文本。
     */
    private final TranscriptionModel transcriptionModel;

    public ChatInputResolver(TranscriptionModel transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }

    /**
     * 解析本次请求实际要发送给聊天模型的文本。
     * <p>
     * 文字消息直接读取 text；
     * 语音消息先调用语音转文字模型，再返回识别后的文本。
     *
     * @param chat 聊天请求参数
     * @return 实际发送给聊天模型的文本
     */
    public String resolveInputText(ChatMessageBo chat) {
        if (chat.getMessageType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "消息类型不能为空。可用值：TEXT、AUDIO");
        }

        return switch (chat.getMessageType()) {
            case TEXT -> resolveTextMessage(chat);
            case AUDIO -> resolveAudioMessage(chat);
        };
    }

    /**
     * 解析文字消息。
     *
     * @param chat 聊天请求参数
     * @return 用户输入的文字内容
     */
    private String resolveTextMessage(ChatMessageBo chat) {
        if (chat.getText() == null || chat.getText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文本不能为空");
        }
        return chat.getText();
    }

    /**
     * 解析语音消息。
     * <p>
     * 该方法会校验 audioFile 是否存在，并调用语音转文字模型得到识别文本。
     *
     * @param chat 聊天请求参数
     * @return 语音识别后的文字内容
     */
    private String resolveAudioMessage(ChatMessageBo chat) {
        if (chat.getAudioFile() == null || chat.getAudioFile().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "音频不能为空");
        }

        return transcriptionModel.call(new AudioTranscriptionPrompt(chat.getAudioFile().getResource()))
                .getResult()
                .getOutput();
    }

}
