package com.voice.service.stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 流式商品卡片查询服务。
 * <p>
 * 当前先模拟外部商品接口查询结果，后续接入真实商品、审批或业务卡片接口时，
 * 只需要替换该服务的查询实现。
 */
@Service
public class StreamProductCardService {

    /**
     * JSON 工具。
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据模型输出的商品 query 查询卡片数据，并返回前端可直接解析的 JSON 字符串。
     *
     * @param query 商品搜索关键词
     * @return 商品卡片 JSON
     */
    public String searchProductJson(String query) {
        Map<String, Object> product = Map.of(
                "query", query,
                "name", query,
                "summary", "模拟商品卡片数据，后续可替换为真实接口返回结果。",
                "tags", new String[]{"AI推荐", "可渲染卡片"},
                "source", "mock"
        );

        try {
            return objectMapper.writeValueAsString(product);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("组装商品卡片 JSON 失败", exception);
        }
    }

}
