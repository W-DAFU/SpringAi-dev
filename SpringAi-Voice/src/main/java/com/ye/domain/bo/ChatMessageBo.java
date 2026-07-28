package com.ye.domain.bo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ChatMessageBo {

    /**
     * 会话 ID，用于后续做多轮对话上下文。
     */
    private String sessionId;

    /**
     * 消息类型。
     * TEXT：本次请求传入文字内容。
     * AUDIO：本次请求传入语音文件，需要先语音转文字再进入聊天模型。
     */
    private MessageType messageType;

    /**
     * 用户输入的文字内容。
     * 当 messageType = TEXT 时使用。
     */
    private String text;

    /**
     * 用户上传的语音文件。
     * 当 messageType = AUDIO 时使用。
     *
     * 注意：MultipartFile 不能通过 @RequestBody JSON 接收，
     * 控制器需要使用 @ModelAttribute 或 @RequestPart 接收 multipart/form-data。
     */
    private MultipartFile audioFile;

    /**
     * 是否需要把 AI 回复转换成语音。
     * false：只返回文本。
     * true：返回文本，同时调用文字转语音生成音频。
     */
    private Boolean ttsEnabled = false;

    public enum MessageType {
        TEXT,
        AUDIO
    }

}
