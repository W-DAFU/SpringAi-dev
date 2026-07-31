package com.df.milvus.controller;


import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("api/")
public class EmbeddingController {


    @Autowired
    private EmbeddingModel embeddingModel;

    /**
     * 文字转向量编码集
     * @param message
     * @return
     */
    @GetMapping("/ai/embedding")
    public Map embed(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(message));
        Map map=new HashMap<>();
        map.put("message", message);
        map.put("embedding", embeddingResponse.getResult().getOutput());
        return map;
    }
}
