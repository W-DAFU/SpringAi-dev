package com.voice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voice.commons.domain.vo.ChatStreamBlockVo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatStreamBlockParserTest {

    @Test
    void waitsForNewlineBeforeReturningTextBlock() throws Exception {
        ChatStreamBlockParser parser = new ChatStreamBlockParser(new ObjectMapper());

        List<ChatStreamBlockVo> first = parser.accept("{\"type\":\"text\",\"text\":\"1. 华为儿童");
        List<ChatStreamBlockVo> second = parser.accept("手表5活力版\\n\\n- 核心功能：定位、防水。\"}\n");

        assertThat(first).isEmpty();
        assertThat(second).singleElement()
                .satisfies(block -> {
                    assertThat(block.getCardType()).isEqualTo("text");
                    assertThat(block.getAnswerText()).isEqualTo("1. 华为儿童手表5活力版\n\n- 核心功能：定位、防水。");
                });
    }

    @Test
    void parsesMultipleCompletedBlocksFromOneChunk() throws Exception {
        ChatStreamBlockParser parser = new ChatStreamBlockParser(new ObjectMapper());

        List<ChatStreamBlockVo> blocks = parser.accept("""
                {"type":"product_card","query":"华为儿童手表5活力版"}
                {"type":"text","text":"2. 小天才Q3A"}
                """);

        assertThat(blocks).hasSize(2);
        assertThat(blocks.get(0).getCardType()).isEqualTo("product_card");
        assertThat(blocks.get(0).getAnswerText()).isEqualTo("华为儿童手表5活力版");
        assertThat(blocks.get(1).getCardType()).isEqualTo("text");
        assertThat(blocks.get(1).getAnswerText()).isEqualTo("2. 小天才Q3A");
    }

    @Test
    void flushesLastLineWhenStreamEndsWithoutNewline() throws Exception {
        ChatStreamBlockParser parser = new ChatStreamBlockParser(new ObjectMapper());

        assertThat(parser.accept("{\"type\":\"text\",\"text\":\"最后一段\"}")).isEmpty();

        assertThat(parser.finish()).hasValueSatisfying(block -> {
            assertThat(block.getCardType()).isEqualTo("text");
            assertThat(block.getAnswerText()).isEqualTo("最后一段");
        });
    }

}
