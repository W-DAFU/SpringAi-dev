package com.voice.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流式聊天解析后的业务 block。
 * <p>
 * 该对象沿用普通聊天返回对象的结构：
 * answerText 承载当前 block 的文本内容或商品卡片 query；
 * answerAudioFormat 和 answerAudioBase64 承载可选的 TTS 结果；
 * cardType 用于区分当前 block 应该如何渲染。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatStreamBlockVo extends ChatMessageVo {

    /**
     * 卡片类型。
     * text：普通文本 block。
     * product_card：商品卡片 block。
     */
    private String cardType;

}
