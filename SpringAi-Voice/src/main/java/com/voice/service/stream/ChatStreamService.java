package com.voice.service.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voice.commons.ChatInputResolver;
import com.voice.domain.bo.ChatMessageBo;
import com.voice.domain.vo.ChatStreamBlockVo;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 流式聊天服务。
 * <p>
 * 该服务和普通聊天服务保持独立，只共用输入解析逻辑。
 * 模型原始输出使用 NDJSON 协议，一行 JSON 表示一个完整业务 block。
 */
@Service
public class ChatStreamService {

    /**
     * Spring AI 聊天客户端。
     * 用于把用户输入发送给聊天模型，并获取流式文本回复。
     */
    private final ChatClient chatClient;

    /**
     * JSON 解析器。
     * 用于解析模型输出的 NDJSON block。
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 聊天输入解析器。
     * 普通接口和流式接口共用该逻辑，保证 TEXT/AUDIO 的入参校验和转换规则一致。
     */
    private final ChatInputResolver chatInputResolver;

    /**
     * 流式 block 组装器。
     * 负责补齐公共字段，并按需为 text block 生成语音。
     */
    private final ChatStreamBlockAssembler chatStreamBlockAssembler;

    /**
     * 商品卡片查询服务。
     * 当前按用户要求传入 ChatStreamBlockParser，在解析 product_card 时查询商品 JSON。
     */
    private final StreamProductCardService streamProductCardService;

    public ChatStreamService(ChatClient.Builder chatClientBuilder,
                             ChatInputResolver chatInputResolver,
                             ChatStreamBlockAssembler chatStreamBlockAssembler,
                             StreamProductCardService streamProductCardService) {
        this.chatClient = chatClientBuilder.build();
        this.chatInputResolver = chatInputResolver;
        this.chatStreamBlockAssembler = chatStreamBlockAssembler;
        this.streamProductCardService = streamProductCardService;
    }

    /**
     * 流式聊天。
     * <p>
     * 先复用公共输入解析逻辑得到实际输入文本，再调用 Spring AI stream API。
     * 模型原始输出会先进入 NDJSON 解析器，只有遇到真实换行并成功解析 JSON 后，
     * 才向前端返回完整业务 block。
     *
     * @param chat 聊天请求参数
     * @return AI 回复业务 block 流
     */
    public Flux<ChatStreamBlockVo> stream(ChatMessageBo chat) {
        String inputText = chatInputResolver.resolveInputText(chat);
        ChatStreamBlockParser parser = new ChatStreamBlockParser(objectMapper, streamProductCardService);

        Flux<ChatStreamBlockVo> parsedBlocks = chatClient.prompt()
                .system(STREAM_SYSTEM_PROMPT)
                .user(inputText)
                .stream()
                .content()
                .flatMapIterable(chunk -> parseChunk(parser, chunk))
                .concatWith(Mono.defer(() -> flushParser(parser)));

        return parsedBlocks.map(block -> chatStreamBlockAssembler.enrich(chat, inputText, block));
    }

    /**
     * 解析模型输出增量。
     */
    private Iterable<ChatStreamBlockVo> parseChunk(ChatStreamBlockParser parser, String chunk) {
        try {
            return parser.accept(chunk);
        } catch (Exception exception) {
            throw new IllegalStateException("解析流式 block 失败", exception);
        }
    }

    /**
     * 模型流结束时 flush 最后一行。
     */
    private Mono<ChatStreamBlockVo> flushParser(ChatStreamBlockParser parser) {
        try {
            return Mono.justOrEmpty(parser.finish());
        } catch (Exception exception) {
            return Mono.error(new IllegalStateException("解析流式 block 失败", exception));
        }
    }

    /**
     * 流式输出协议提示词。
     * <p>
     * 要求模型只输出 NDJSON：一行一个完整 JSON block。
     * 字段内部换行必须写成 \\n，不能输出真实换行。
     */
    private static final String STREAM_SYSTEM_PROMPT = """
            你是一个商品推荐聊天助手。你必须严格按后端流式协议输出，不要输出 Markdown 代码块，不要输出解释文字。

            你只能输出 NDJSON，每一行必须是一个完整 JSON 对象。
            不同 block 之间使用真实换行分隔。
            JSON 字符串字段内部的换行必须写成 \\n，不能输出未转义的真实换行。

            允许的格式只有：
            {"type":"text","text":"要展示给用户的文本，支持 Markdown 列表；字段内部换行必须写成 \\n"}
            {"type":"product_card","query":"商品搜索关键词"}

            允许的 type 只有：
            1. text：用于展示给用户的说明文字，字段名必须是 text。
            2. product_card：用于插入商品卡片，字段名必须是 query，只输出商品搜索关键词，不要编造价格、图片、销量。

            性能要求：
            1. 第一行必须立即输出一个很短的 text block，例如：
            {"type":"text","text":"正在查询相关信息。"}
            2. 第一行不要等待完整推荐理由，不要超过 30 个中文字符。
            3. 后续再按商品顺序输出 text 和 product_card。
            4. 每个 text block 保持简短，避免把多个商品说明合并到同一个 text block。

            输出顺序要表达真实展示顺序，例如：
            {"type":"text","text":"正在查询相关信息。"}
            {"type":"text","text":"1. 华为儿童手表5活力版\\n\\n- 核心功能：定位、防水。"}
            {"type":"product_card","query":"华为儿童手表5活力版"}
            {"type":"text","text":"2. 小天才Q3A\\n\\n- 核心功能：AI定位、视频通话。"}
            {"type":"product_card","query":"小天才Q3A"}
            """;

}
