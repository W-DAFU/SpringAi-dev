package com.voice.service.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voice.domain.vo.ChatStreamBlockVo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatStreamBlockParserTest {

    @Test
    void productCardBlockUsesProductServiceResultAsAnswerText() throws Exception {
        ChatStreamBlockParser parser = new ChatStreamBlockParser(new ObjectMapper(), new StreamProductCardService());

        List<ChatStreamBlockVo> blocks = parser.accept("""
                {"type":"product_card","query":"华为儿童手表5活力版"}
                """);

        assertThat(blocks).singleElement()
                .satisfies(block -> {
                    assertThat(block.getCardType()).isEqualTo("product_card");
                    assertThat(block.getAnswerText()).contains("\"query\":\"华为儿童手表5活力版\"");
                    assertThat(block.getAnswerText()).contains("\"source\":\"mock\"");
                });
    }

    @Test
    void textBlockStillUsesTextFieldAsAnswerText() throws Exception {
        ChatStreamBlockParser parser = new ChatStreamBlockParser(new ObjectMapper(), new StreamProductCardService());

        List<ChatStreamBlockVo> blocks = parser.accept("""
                {"type":"text","text":"1. 华为儿童手表5活力版\\n\\n- 核心功能：定位、防水。"}
                """);

        assertThat(blocks).singleElement()
                .satisfies(block -> {
                    assertThat(block.getCardType()).isEqualTo("text");
                    assertThat(block.getAnswerText()).isEqualTo("1. 华为儿童手表5活力版\n\n- 核心功能：定位、防水。");
                });
    }

}
