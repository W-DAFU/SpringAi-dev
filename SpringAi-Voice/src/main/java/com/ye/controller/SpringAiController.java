package com.ye.controller;

import com.ye.domain.bo.ChatMessageBo;
import com.ye.domain.vo.ChatMessageVo;
import com.ye.domain.vo.R;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;

/**
 * Spring AI 聊天接口控制器。
 * <p>
 * 当前控制器提供统一聊天入口，支持文字聊天、语音转文字聊天，
 * 以及可选的 AI 回复文字转语音。
 */
@RestController
@RequestMapping("/api")
public class SpringAiController {

    /**
     * Spring AI 聊天客户端。
     * 用于把用户输入发送给聊天模型，并获取文本回复。
     */
    private final ChatClient chatClient;

    /**
     * 语音转文字模型。
     * 当用户上传语音文件时，用它把语音内容识别成文本。
     */
    private final TranscriptionModel transcriptionModel;

    /**
     * 文字转语音模型。
     * 当请求参数 ttsEnabled=true 时，用它把 AI 文本回复转换成音频。
     */
    private final TextToSpeechModel textToSpeechModel;

    /**
     * 构造方法注入 Spring AI 相关模型。
     *
     * @param chatClientBuilder  聊天客户端构建器
     * @param transcriptionModel 语音转文字模型
     * @param textToSpeechModel  文字转语音模型
     */
    public SpringAiController(ChatClient.Builder chatClientBuilder,
                              TranscriptionModel transcriptionModel,
                              TextToSpeechModel textToSpeechModel) {
        this.chatClient = chatClientBuilder.build();
        this.transcriptionModel = transcriptionModel;
        this.textToSpeechModel = textToSpeechModel;
    }

    /**
     * 统一聊天接口。
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
        String inputText = resolveInputText(chat);
        String answerText = chatClient.prompt()
                .user(inputText)
                .call()
                .content();

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

        return R.ok(vo);
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
    private String resolveInputText(ChatMessageBo chat) {
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
